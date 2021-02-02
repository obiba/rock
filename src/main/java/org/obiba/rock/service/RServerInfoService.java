/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.rock.service;


import org.obiba.rock.NodeProperties;
import org.obiba.rock.Resources;
import org.obiba.rock.model.RServerInfo;
import org.obiba.rock.model.RServerStatus;
import org.obiba.rock.model.RSessionsCounts;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class RServerInfoService {

  @Autowired
  private RServerService rServerService;

  @Autowired
  private RSessionService rSessionService;

  @Autowired
  private NodeProperties nodeProperties;

  public RServerInfo getRServerInfo() {
    return new RServerInfo()
        .withId(nodeProperties.getId())
        .withEncoding(Resources.getRserveEncoding())
        .withVersion(rServerService.getVersion())
        .withTags(nodeProperties.getTags())
        .withrServerStatus(new RServerStatus()
            .withRunning(rServerService.isRunning())
            .withrSessionsCounts(new RSessionsCounts()
            .withAdditionalProperty("total", rSessionService.getRSessionsCount())
            .withAdditionalProperty("busy", rSessionService.getBusyRSessionsCount()))
        );
  }
}
