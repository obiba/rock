package org.obiba.rock.rest;


import org.obiba.rock.service.RServerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ServiceController {

    @Autowired
    private RServerService rServerService;

    @GetMapping("/_check")
    ResponseEntity<?> check() {
        if (rServerService.isAlive())
            return ResponseEntity.noContent().build();
        else
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
    }
}
