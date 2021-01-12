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
import org.obiba.rock.model.RSession;
import org.obiba.rock.security.Roles;
import org.obiba.rock.service.RSessionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/r/sessions")
public class RSessionsController {

    @Autowired
    private RSessionService rSessionService;

    /**
     * Get the list of R sessions.
     *
     * @return
     */
    @GetMapping
    List<RSession> getRSessions(HttpServletRequest request, @AuthenticationPrincipal User user, @RequestParam(name = "subject", required = false) String subject) {
        if (request.isUserInRole(Roles.ROCK_ADMIN) || request.isUserInRole(Roles.ROCK_MANAGER)) {
            // get all/filtered sessions
            return Strings.isNullOrEmpty(subject) ? rSessionService.getRSessions()
                    : rSessionService.getRSessions().stream().filter(s -> subject.equals(s.getSubject())).collect(Collectors.toList());
        } else {
            // get own sessions
            return rSessionService.getRSessions().stream().filter(s -> user.getUsername().equals(s.getSubject())).collect(Collectors.toList());
        }
    }

    /**
     * Close all R sessions.
     *
     * @return
     */
    @Secured({Roles.ROLE_ADMIN, Roles.ROLE_MANAGER})
    @DeleteMapping
    ResponseEntity<?> deleteRSessions() {
        rSessionService.closeAllRSessions();
        return ResponseEntity.noContent().build();
    }

    /**
     * Create a R session, owned by a subject.
     *
     * @param user
     * @param ucb
     * @return The R session object
     */
    @Secured({Roles.ROLE_ADMIN, Roles.ROLE_USER})
    @PostMapping
    ResponseEntity<?> createRSession(@AuthenticationPrincipal User user, UriComponentsBuilder ucb) {
        RSession rSession = rSessionService.createRSession(user);
        return ResponseEntity.created(ucb.path("/r/session/{id}").buildAndExpand(rSession.getId()).toUri()).body(rSession);
    }

}
