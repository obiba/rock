/*
 * Copyright (c) 2014 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.rock.rest;

import org.obiba.rock.model.RServerState;
import org.obiba.rock.service.RServerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/rserver")
public class RServerController {

    @Autowired
    private RServerService rServerService;

    /**
     * Get the R server state.
     *
     * @return
     */
    @GetMapping
    @ResponseBody
    public RServerState getRServerState() {
        return rServerService;
    }

    /**
     * Start the R server if not already running.
     *
     * @return The R server state
     */
    @PutMapping
    @ResponseBody
    public RServerState start() {
        if (!rServerService.isRunning()) {
            rServerService.start();
        }
        return rServerService;
    }

    /**
     * Stop the R server.
     *
     * @return The R server state
     */
    @DeleteMapping
    @ResponseBody
    public RServerState stop() {
        rServerService.stop();
        return rServerService;
    }

}