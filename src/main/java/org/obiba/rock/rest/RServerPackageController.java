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

import org.obiba.rock.domain.RPackage;
import org.obiba.rock.security.Roles;
import org.obiba.rock.service.RPackagesService;
import org.rosuda.REngine.REXPMismatchException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;

@RestController
public class RServerPackageController {

    @Autowired
    private RPackagesService rPackagesService;

    /**
     * Get the installed R package description, as a R packageDescription object.
     *
     * @return
     */
    @GetMapping(produces = "application/octet-stream")
    @Secured({Roles.ROLE_ADMIN, Roles.ROLE_MANAGER})
    public ResponseEntity<?> getPackage(@PathVariable("name") String name) {
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_OCTET_STREAM).body(rPackagesService.getPackageDescriptionRaw(name).asBytes());
    }

    /**
     * Get the installed R package description, as a JSON object.
     *
     * @return
     */
    @GetMapping(value = "/rserver/package/{name}", produces = "application/json")
    @ResponseBody
    @Secured({Roles.ROLE_ADMIN, Roles.ROLE_MANAGER})
    public RPackage getPackageJSON(@PathVariable("name") String name) throws REXPMismatchException {
        return rPackagesService.getPackageDescription(name);
    }

    /**
     * Delete R package.
     *
     * @param name
     * @return
     */
    @DeleteMapping("/rserver/package/{name}")
    @Secured({Roles.ROLE_ADMIN, Roles.ROLE_MANAGER})
    public ResponseEntity<?> deletePackage(@PathVariable("name") String name) {
        rPackagesService.removePackage(name);
        return ResponseEntity.noContent().build();
    }
}