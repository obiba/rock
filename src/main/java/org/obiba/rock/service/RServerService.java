/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.rock.service;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import org.obiba.rock.RProperties;
import org.obiba.rock.Resources;
import org.obiba.rock.model.Registry;
import org.obiba.rock.r.ROperationWithResult;
import org.obiba.rock.r.RScriptROperation;
import org.obiba.rock.util.Tail;
import org.rosuda.REngine.REXP;
import org.rosuda.REngine.REXPRaw;
import org.rosuda.REngine.Rserve.RConnection;
import org.rosuda.REngine.Rserve.RserveException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Service to manage RServer process.
 */
@Component
public class RServerService {

  private static final Logger log = LoggerFactory.getLogger(RServerService.class);

  @Autowired
  private RProperties rProperties;

  @Autowired
  private RSessionService rSessionService;

  @Autowired
  private Set<Registry> registries;

  private int rserveStatus = -1;

  private String version = "?";

  @JsonIgnore
  public Integer getPort() {
    return Resources.getRservePort();
  }

  public String getVersion() {
    return version;
  }

  public boolean isRunning() {
    return rserveStatus == 0;
  }

  @PostConstruct
  public void start() {
    if (rserveStatus == 0) {
      log.error("RServerService is already running");
      return;
    }

    doStart();
    if (isRunning()) registerService();
  }

  @PreDestroy
  public void stop() {
    if (rserveStatus != 0) return;
    doStop();
    unregisterService();
  }

  /**
   * Creates a new connection to R server.
   *
   * @return
   */
  public RConnection newConnection() {
    try {
      return newRConnection();
    } catch (RserveException e) {
      log.error("Error while connecting to R: {}", e.getMessage());
      throw new RuntimeException(e);
    }
  }

  public ROperationWithResult execute(String rscript, boolean serialize) {
    RScriptROperation rop = new RScriptROperation(rscript, serialize);
    return execute(rop);
  }

  public ROperationWithResult execute(ROperationWithResult rop) {
    RConnection connection = newConnection();
    try {
      rop.doWithConnection(connection);
      return rop;
    } finally {
      connection.close();
    }
  }

  /**
   * Check the running status and the R server is functional.
   *
   * @return
   */
  @JsonIgnore
  public boolean isAlive() {
    return isRunning() && isRServerAlive();
  }

  /**
   * Soft restart, without loosing registered services.
   */
  public void restart() {
    doStop();
    doStart();
  }

  /**
   * Restart Rserve process after it died.
   */
  @Scheduled(fixedDelay = 10 * 1000)
  public void autoRestart() {
    if (isRunning() && !isRServerAlive()) {
      log.info("Rserve died, restarting...");
      restart();
    }
  }

  /**
   * Get the last lines of the R server log.
   *
   * @param limit
   * @return
   * @throws IOException
   */
  public List<String> tailRserverLog(int limit) throws IOException {
    return Tail.tailFile(getRserveLogFile().toPath(), limit);
  }

  /**
   * Get the version object of the R server.
   *
   * @return
   */
  @JsonIgnore
  public REXPRaw getRserverVersionRaw() {
    ROperationWithResult rop = execute("R.version", true);
    return rop.getRawResult();
  }

  /**
   * Case registering sync failed.
   */
  @Scheduled(fixedDelay = 10 * 1000)
  public void registryCheck() {
    // no-op if already registered
    registries.forEach(Registry::register);
  }

  //
  // Private methods
  //

  private boolean isRServerAlive() {
    try {
      RConnection conn = newRConnection();
      conn.close();
      return true;
    } catch (RserveException e) {
      return false;
    }
  }

  private void doStart() {
    log.info("Start RServerService with {}", rProperties.getExec());

    // fresh start, try to kill any remains of R server
    try {
      newRConnection().shutdown();
    } catch (Exception e) {
      // ignore
    }

    try {
      // launch the Rserve daemon and wait for it to complete
      Process rserve = buildRProcess().start();
      rserveStatus = rserve.waitFor();
      if (rserveStatus == 0) {
        log.info("R server started");
        initRVersion();
      } else {
        log.error("R server start failed with status: {}", rserveStatus);
        rserveStatus = -1;
      }
    } catch (Exception e) {
      log.warn("R server start failed", e);
      rserveStatus = -1;
    }
  }

  private void doStop() {
    try {
      log.info("Closing all R sessions...");
      rSessionService.closeAllRSessions();
      log.info("Shutting down R server...");
      newConnection().shutdown();
    } catch (Exception e) {
      // ignore
    }

    try {
      rserveStatus = -1;
      log.info("R server shut down");
      File workDir = getWorkingDirectory();
      for (File file : Objects.requireNonNull(workDir.listFiles())) {
        delete(file);
      }
    } catch (Exception e) {
      log.error("R server shutdown failed", e);
    }
  }

  private void initRVersion() {
    try {
      ROperationWithResult rop = execute("paste0(R.version$major, '.', R.version$minor)", false);
      REXP rexp = rop.getResult();
      version = rexp.asString();
    } catch (Exception e) {
      version = "?";
    }
  }

  private void registerService() {
    registries.forEach(Registry::register);
  }

  private void unregisterService() {
    registries.forEach(Registry::unregister);
  }

  /**
   * Create a new RConnection given the R server settings.
   *
   * @return
   * @throws RserveException
   */
  private RConnection newRConnection() throws RserveException {
    RConnection conn = new RConnection();

    if (conn.needLogin()) {
      Map<String, String> conf = Resources.getRservConf();
      if (conf.containsKey("auth") && conf.get("auth").equals("required") && conf.containsKey("pwdfile")) {
        Map<String, String> pwds = Resources.getUsernamePasswords(conf.get("pwdfile"));
        if (!pwds.isEmpty()) {
          Map.Entry<String, String> entry = pwds.entrySet().iterator().next();
          conn.login(entry.getKey(), entry.getValue());
        }
      }
    }

    conn.setStringEncoding(Resources.getRserveEncoding());

    return conn;
  }

  private ProcessBuilder buildRProcess() {
    List<String> args = getArguments();
    log.info("Starting R server: {}", Joiner.on(" ").join(args));
    ProcessBuilder pb = new ProcessBuilder(args);
    pb.directory(getWorkingDirectory());
    pb.redirectErrorStream(true);
    pb.redirectOutput(ProcessBuilder.Redirect.appendTo(getRserveLogFile()));
    return pb;
  }

  private List<String> getArguments() {
    StringBuffer rserveArgs = new StringBuffer("--vanilla");
    File workDir = getWorkingDirectory();
    rserveArgs.append(" --RS-workdir ").append(workDir.getAbsolutePath());

    File conf = Resources.getRservConfFile();
    if (conf.exists()) {
      rserveArgs.append(" --RS-conf ").append(conf.getAbsolutePath());
    }

    List<String> args = Lists.newArrayList(rProperties.getExec(), "-e", "library(Rserve) ; Rserve(args='" + rserveArgs + "')");

    return args;
  }

  private void delete(File file) {
    if (file.isDirectory()) {
      for (File f : Objects.requireNonNull(file.listFiles())) {
        delete(f);
      }
    }
    if (!file.isDirectory() || Objects.requireNonNull(file.list()).length == 0) {
      if (!file.delete()) {
        log.warn("Unable to delete file: " + file.getAbsolutePath());
      }
    }
  }

  private File getWorkingDirectory() {
    File dir = new File(Resources.getRServerHomeDir(), "work" + File.separator + "R");
    if (!dir.exists()) {
      if (!dir.mkdirs()) {
        log.error("Unable to create: {}", dir.getAbsolutePath());
      }
    }
    return dir;
  }

  private File getRserveLogFile() {
    File logFile = new File(Resources.getRServerHomeDir(), "logs" + File.separator + "Rserve.log");
    if (!logFile.getParentFile().exists()) {
      if (!logFile.getParentFile().mkdirs()) {
        log.error("Unable to create: {}", logFile.getParentFile().getAbsolutePath());
      }
    }
    return logFile;
  }
}
