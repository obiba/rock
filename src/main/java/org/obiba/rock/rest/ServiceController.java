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


import org.obiba.rock.NodeProperties;
import org.obiba.rock.service.RServerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ServiceController {

  @Autowired
  private NodeProperties nodeProperties;

  @Autowired
  private RServerService rServerService;

  @GetMapping(value = "/", produces = "application/json")
  ResponseEntity<String> home() {
    return info();
  }

  @GetMapping(value = "/_info", produces = "application/json")
  ResponseEntity<String> info() {
    String json = nodeProperties.asJSON();
    return ResponseEntity.ok(json);
  }

  @GetMapping("/_check")
  ResponseEntity<?> check() {
    if (rServerService.isAlive())
      return ResponseEntity.noContent().build();
    else
      return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
  }
}
