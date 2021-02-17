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

import org.obiba.rock.model.RServerInfo;
import org.obiba.rock.security.Roles;
import org.obiba.rock.service.RServerInfoService;
import org.obiba.rock.service.RServerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.PrintWriter;
import java.util.List;

@RestController
@RequestMapping("/rserver")
public class RServerController {

  @Autowired
  private RServerService rServerService;

  @Autowired
  private RServerInfoService rServerInfoService;

  /**
   * Get the R server state.
   *
   * @return
   */
  @GetMapping
  @ResponseBody
  public RServerInfo getRServerState(@AuthenticationPrincipal User user) {
    if (Roles.isAdmin(user) || Roles.isManager(user))
      return rServerInfoService.getRServerInfo();
    throw new AccessDeniedException("R server status access forbidden");
  }

  /**
   * Start the R server if not already running.
   *
   * @return The R server state
   */
  @PutMapping
  @ResponseBody
  public RServerInfo start(@AuthenticationPrincipal User user) {
    if (Roles.isAdmin(user) || Roles.isManager(user)) {
      if (!rServerService.isRunning()) {
        rServerService.start();
      }
      return rServerInfoService.getRServerInfo();
    }
    throw new AccessDeniedException("R server start forbidden");
  }

  /**
   * Stop the R server.
   *
   * @return The R server state
   */
  @DeleteMapping
  @ResponseBody
  public RServerInfo stop() {
    rServerService.stop();
    return rServerInfoService.getRServerInfo();
  }

  /**
   * Get the R server logs.
   *
   * @return
   */
  @GetMapping(value = "/_log", produces = "text/plain")
  public ResponseEntity<StreamingResponseBody> getRServerLog(@RequestParam(name = "limit", required = false, defaultValue = "1000") int limit) {
    StreamingResponseBody stream = out -> {
      List<String> rlog = rServerService.tailRserverLog(limit);
      try (PrintWriter writer = new PrintWriter(out)) {
        for (String line : rlog) {
          writer.println(line);
        }
      }
    };
    return ResponseEntity.ok().contentType(MediaType.TEXT_PLAIN).body(stream);
  }

  /**
   * Get the R server version object.
   *
   * @return
   */
  @GetMapping(value = "/_version", produces = "application/octet-stream")
  public ResponseEntity<?> getRServerVersion() {
    return ResponseEntity.ok().contentType(MediaType.APPLICATION_OCTET_STREAM).body(rServerService.getRserverVersionRaw().asBytes());
  }
}