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
import org.obiba.rock.model.Sessions;
import org.obiba.rock.model.System;
import org.obiba.rock.r.SystemInfoROperation;
import org.rosuda.REngine.RList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;

@Component
public class RServerInfoService {

  private static final Logger log = LoggerFactory.getLogger(RServerInfoService.class);

  @Autowired
  private RServerService rServerService;

  @Autowired
  private RSessionService rSessionService;

  @Autowired
  private NodeProperties nodeProperties;

  public RServerInfo getRServerInfo() {
    System sys = new System().withCores(-1).withFreeMemory(-1);

    if (rServerService.isRunning()) {
      try {
        File systemInfo = new File(Resources.getRServerHomeDir(), "conf" + File.separator + "system.info.R");
        SystemInfoROperation rop = new SystemInfoROperation(systemInfo.getAbsolutePath());
        rServerService.execute(rop);
        RList rval = rop.getResult().asList();
        for (String key : rval.keys()) {
          if ("cores".equals(key))
            sys.withCores(rval.at(key).asInteger());
          else if ("freeMemory".equals(key))
            sys.withFreeMemory(rval.at(key).asInteger());
          else
            sys.withAdditionalProperty(key, rval.at(key).asNativeJavaObject());
        }
      } catch (Exception e) {
        log.error("Failed to extract system info from R", e);
      }
    }

    return new RServerInfo()
        .withId(nodeProperties.getId())
        .withCluster(nodeProperties.getCluster())
        .withTags(nodeProperties.getTags())
        .withEncoding(Resources.getRserveEncoding())
        .withVersion(rServerService.getVersion())
        .withRunning(rServerService.isRunning())
        .withSessions(new Sessions()
            .withTotal(rSessionService.getRSessionsCount())
            .withBusy(rSessionService.getBusyRSessionsCount()))
        .withSystem(sys);
  }
}
