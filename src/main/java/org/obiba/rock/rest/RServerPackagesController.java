/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.rock.rest;

import com.google.common.base.Strings;
import org.obiba.rock.domain.RStringMatrix;
import org.obiba.rock.security.Roles;
import org.obiba.rock.service.RPackagesService;
import org.rosuda.REngine.REXPMismatchException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;

@RestController
@RequestMapping("/rserver/packages")
public class RServerPackagesController {

  @Autowired
  private RPackagesService rPackagesService;

  /**
   * Get the installed R packages, as a R matrix.
   *
   * @return
   */
  @GetMapping(produces = "application/octet-stream")
  @Secured({Roles.ROLE_ADMIN, Roles.ROLE_MANAGER})
  public ResponseEntity<?> getPackages() {
    return ResponseEntity.ok().contentType(MediaType.APPLICATION_OCTET_STREAM).body(rPackagesService.getInstalledPackagesRaw());
  }

  /**
   * Get the installed R packages, as a JSON object representing a matrix with: row names (package names), column names
   * (field names) and rows (package's list of values corresponding to each field).
   *
   * @return
   */
  @GetMapping(produces = "application/json")
  @ResponseBody
  @Secured({Roles.ROLE_ADMIN, Roles.ROLE_MANAGER})
  public RStringMatrix getPackagesJSON() {
    return rPackagesService.getInstalledPackagesMatrix();
  }

  /**
   * Update all the R packages and restart the R server.
   *
   * @return
   * @throws REXPMismatchException
   */
  @PutMapping
  public ResponseEntity<?> updateAllPackages() throws REXPMismatchException {
    rPackagesService.updateAllPackages();
    return ResponseEntity.ok().build();
  }

  /**
   * Install a R package from CRAN, GitHub or Bioconductor.
   *
   * @param name
   * @param ref
   * @param manager
   * @param ucb
   * @return
   */
  @PostMapping
  @Secured({Roles.ROLE_ADMIN, Roles.ROLE_MANAGER})
  public ResponseEntity<?> installPackage(@RequestParam(name = "name") String name,
                                          @RequestParam(name = "ref", required = false) String ref,
                                          @RequestParam(name = "manager", defaultValue = "cran") String manager,
                                          UriComponentsBuilder ucb) {
    if (Strings.isNullOrEmpty(manager) || "cran".equalsIgnoreCase(manager))
      rPackagesService.installCRANPackage(name);
    else if ("gh".equalsIgnoreCase(manager) || "github".equalsIgnoreCase(manager))
      rPackagesService.installGitHubPackage(name, ref);
    else if ("bioc".equalsIgnoreCase(manager) || "bioconductor".equalsIgnoreCase(manager))
      rPackagesService.installBioconductorPackage(name);
    return ResponseEntity.created(ucb.path("/rserver/package/{name}").buildAndExpand(name).toUri()).build();
  }

  /**
   * Delete specified R packages.
   *
   * @param names
   * @return
   */
  @DeleteMapping
  @Secured({Roles.ROLE_ADMIN, Roles.ROLE_MANAGER})
  public ResponseEntity<?> deletePackages(@RequestParam(name = "name") List<String> names) {
    names.forEach(n -> rPackagesService.removePackage(n));
    return ResponseEntity.noContent().build();
  }

  /**
   * Discover DataSHIELD packages and settings.
   *
   * @return
   */
  @GetMapping(value = "_datashield", produces = "application/octet-stream")
  @ResponseBody
  @Secured({Roles.ROLE_ADMIN, Roles.ROLE_MANAGER})
  public ResponseEntity<?> getDataSHIELDPackages() {
    return ResponseEntity.ok().contentType(MediaType.APPLICATION_OCTET_STREAM).body(rPackagesService.getDataSHIELDPackagesRaw());
  }

  /**
   * Discover DataSHIELD packages and settings.
   *
   * @return
   */
  @GetMapping(value = "_datashield", produces = "application/json")
  @ResponseBody
  @Secured({Roles.ROLE_ADMIN, Roles.ROLE_MANAGER})
  public String getDataSHIELDPackagesJSON() throws REXPMismatchException {
    return rPackagesService.getDataSHIELDPackagesJSON();
  }
}