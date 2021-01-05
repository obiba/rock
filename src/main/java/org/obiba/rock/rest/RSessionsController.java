package org.obiba.rock.rest;


import com.google.common.base.Strings;
import org.obiba.rock.model.RSession;
import org.obiba.rock.service.RSessionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

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
    List<RSession> getRSessions(@RequestParam(name = "subject", required = false) String subject) {
        // TODO requires admin role
        return Strings.isNullOrEmpty(subject) ? rSessionService.getRSessions()
                : rSessionService.getRSessions().stream().filter(s -> subject.equals(s.getSubject())).collect(Collectors.toList());
    }

    /**
     * Create a R session, owned by a subject.
     *
     * @param subject
     * @param ucb
     * @return The R session object
     */
    @PostMapping
    ResponseEntity<?> createRSession(@RequestParam(name = "subject") String subject, UriComponentsBuilder ucb) {
        RSession rSession = rSessionService.createRSession(subject);
        return ResponseEntity.created(ucb.path("/r/session/{id}").buildAndExpand(rSession.getId()).toUri()).body(rSession);
    }

}
