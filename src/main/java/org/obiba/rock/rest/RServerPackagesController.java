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

import org.obiba.rock.domain.RStringMatrix;
import org.obiba.rock.security.Roles;
import org.obiba.rock.service.RPackagesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;

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
    @ResponseBody
    @Secured({Roles.ROLE_ADMIN, Roles.ROLE_MANAGER})
    public ResponseEntity<?> getPackages() {
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_OCTET_STREAM).body(rPackagesService.getInstalledPackagesRaw().asBytes());
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

}