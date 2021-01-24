/*
 * Copyright (c) 2020 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.rock.service;

import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.google.common.collect.Iterables;
import org.obiba.rock.RProperties;
import org.obiba.rock.domain.RPackage;
import org.obiba.rock.domain.RStringMatrix;
import org.obiba.rock.r.ROperationWithResult;
import org.obiba.rock.r.RScriptROperation;
import org.rosuda.REngine.REXP;
import org.rosuda.REngine.REXPMismatchException;
import org.rosuda.REngine.REXPRaw;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Helper class for R package management.
 */
@Component
public class RPackagesService {

  private static final Logger log = LoggerFactory.getLogger(RPackagesService.class);

  public static final String VERSION = "Version";

  public static final String AGGREGATE_METHODS = "AggregateMethods";

  public static final String ASSIGN_METHODS = "AssignMethods";

  public static final String OPTIONS = "Options";

  private static final String[] defaultFields = new String[]{"Title", "Description", "Author", "Maintainer",
      "Date/Publication", AGGREGATE_METHODS, ASSIGN_METHODS, OPTIONS};

  @Autowired
  private RProperties rProperties;

  @Autowired
  private RServerService rServerService;

  public REXPRaw getPackageDescriptionRaw(String name) {
    String cmd = String.format("packageDescription('%s')", name);
    ROperationWithResult rop = execute(cmd, true);
    return rop.getRawResult();
  }

  public RPackage getPackageDescription(String name) throws REXPMismatchException {
    String cmd = String.format("packageDescription('%s')", name);
    ROperationWithResult rop = execute(cmd);
    if (!rop.getResult().isLogical()) {
      Map<String[], String> fields = (Map) rop.getResult().asNativeJavaObject();
      RPackage rPackage = new RPackage();
      rPackage.setName(name);
      fields.keySet().stream().filter(k -> fields.get(k) != null).forEach(k -> rPackage.putField(fields.get(k), k[0]));
      return rPackage;
    }
    throw new NoSuchElementException("No package with name: " + name);
  }

  public void updateAllPackages() throws REXPMismatchException {
    // dump all R sessions
    rServerService.restart();
    // get R server own lib path
    String cmd = ".libPaths()";
    ROperationWithResult rop = execute(cmd);
    REXP rexp = rop.getResult();
    String libpath = rexp.asStrings()[0];
    // update all packages in own lib path
    String repos = Joiner.on("','").join(getDefaultRepos());
    cmd = String.format("update.packages(ask = FALSE, repos = c('%s'), instlib = '%s')", repos, libpath);
    execute(cmd);
    // fresh new start
    rServerService.restart();
  }

  public REXPRaw getInstalledPackagesRaw() {
    ROperationWithResult rop = getInstalledPackages(true);
    return rop.getRawResult();
  }

  public RStringMatrix getInstalledPackagesMatrix() {
    ROperationWithResult rop = getInstalledPackages(false);
    REXP rexp = rop.getResult();
    return new RStringMatrix(rexp);
  }

  private ROperationWithResult getInstalledPackages(boolean serialize) {
    return getInstalledPackages(new ArrayList<>(), serialize);
  }

  public void removePackage(String name) {
    try {
      checkAlphanumeric(name);
      String cmd = "remove.packages('" + name + "')";
      execute(cmd);
    } catch (Exception e) {
      log.warn("Cannot remove R package {}", name, e);
    }
  }

  /**
   * Try to load a R package and install it if not found.
   *
   * @param name
   */
  public void ensureCRANPackage(String name) {
    String cmd = String.format("if (!require(%s)) { %s }", name, getInstallPackagesCommand(name));
    execute(cmd);
  }

  /**
   * Install a R package from CRAN.
   *
   * @param name
   */
  public void installCRANPackage(String name) {
    installPackage(name, null, null);
  }

  /**
   * Install a R package from GitHub.
   *
   * @param name
   * @param ref
   */
  public void installGitHubPackage(String name, String ref) {
    installPackage(name, Strings.isNullOrEmpty(ref) ? "master" : ref, "obiba");
  }

  /**
   * Install a Bioconductor package.
   *
   * @param name
   */
  public void installBioconductorPackage(String name) {
    checkAlphanumeric(name);
    String cmd = String.format("BiocManager::install('%s', ask = FALSE, dependencies=TRUE)", name);
    execute(getInstallBiocManagerPackageCommand());
    execute(cmd);
    restartRServer();
  }

  /**
   * Install a package from CRAN of no ref is specified, or from GitHub if a ref is specified.
   *
   * @param name
   * @param ref
   * @param defaultName When installing from GitHub, the default organization name.
   * @return
   */
  public ROperationWithResult installPackage(String name, String ref, String defaultName) {
    String cmd;
    if (Strings.isNullOrEmpty(ref)) {
      checkAlphanumeric(name);
      cmd = getInstallPackagesCommand(name);
    } else {
      execute(getInstallDevtoolsPackageCommand());
      if (name.contains("/")) {
        String[] parts = name.split("/");
        checkAlphanumeric(parts[0]);
        checkAlphanumeric(parts[1]);
        cmd = getInstallGitHubCommand(parts[1], parts[0], ref);
      } else {
        checkAlphanumeric(name);
        cmd = getInstallGitHubCommand(name, defaultName, ref);
      }
    }
    ROperationWithResult rval = execute(cmd);
    restartRServer();
    return rval;
  }

  void restartRServer() {
    try {
      rServerService.stop();
      rServerService.start();
    } catch (Exception ex) {
      log.error("Error while restarting R server after package install: {}", ex.getMessage(), ex);
    }
  }

  List<String> getDefaultRepos() {
    return rProperties.getRepos().stream().map(String::trim).collect(Collectors.toList());
  }

  public ROperationWithResult execute(String rscript) {
    return execute(rscript, false);
  }

  public ROperationWithResult execute(String rscript, boolean serialize) {
    RScriptROperation rop = new RScriptROperation(rscript, serialize);
    return execute(rop);
  }

  public ROperationWithResult execute(ROperationWithResult rop) {
    rServerService.execute(rop);
    return rop;
  }

  private ROperationWithResult getInstalledPackages(Iterable<String> fields, boolean serialize) {
    Iterable<String> allFields = Iterables.concat(Arrays.asList(defaultFields), fields);
    String fieldStr = Joiner.on("','").join(allFields);
    String cmd = "installed.packages(fields=c('" + fieldStr + "'))";
    return execute(cmd, serialize);
  }

  /**
   * Simple security check: the provided name (package name or Github user/organization name) must be alphanumeric.
   *
   * @param name
   */
  private void checkAlphanumeric(String name) {
    if (!name.matches("[a-zA-Z0-9\\-\\\\.]+"))
      throw new IllegalArgumentException("Not a valid name: " + name);
  }

  private String getInstallPackagesCommand(String name) {
    String repos = Joiner.on("','").join(getDefaultRepos());
    return "install.packages('" + name + "', repos=c('" + repos + "'), dependencies=TRUE)";
  }

  private String getInstallDevtoolsPackageCommand() {
    return "if (!require('devtools', character.only=TRUE)) { " + getInstallPackagesCommand("devtools") + " }";
  }

  private String getInstallBiocManagerPackageCommand() {
    return "if (!require('BiocManager', character.only=TRUE)) { " + getInstallPackagesCommand("BiocManager") + " }";
  }

  private String getInstallGitHubCommand(String name, String username, String ref) {
    return String.format("devtools::install_github('%s/%s', ref='%s', dependencies=TRUE, upgrade=TRUE)", username, name, ref);
  }

}
