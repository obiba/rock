package org.obiba.rock.rest;


import com.google.common.base.Strings;
import org.obiba.rock.model.RSession;
import org.obiba.rock.security.Roles;
import org.obiba.rock.service.RSessionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
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
        if (request.isUserInRole(Roles.ROCK_ADMIN)) {
            // get all/filtered sessions
            return Strings.isNullOrEmpty(subject) ? rSessionService.getRSessions()
                    : rSessionService.getRSessions().stream().filter(s -> subject.equals(s.getSubject())).collect(Collectors.toList());
        } else {
            // get own sessions
            return rSessionService.getRSessions().stream().filter(s -> user.getUsername().equals(s.getSubject())).collect(Collectors.toList());
        }
    }

    /**
     * Create a R session, owned by a subject.
     *
     * @param user
     * @param ucb
     * @return The R session object
     */
    @PostMapping
    ResponseEntity<?> createRSession(@AuthenticationPrincipal User user, UriComponentsBuilder ucb) {
        RSession rSession = rSessionService.createRSession(user.getUsername());
        return ResponseEntity.created(ucb.path("/r/session/{id}").buildAndExpand(rSession.getId()).toUri()).body(rSession);
    }

}
