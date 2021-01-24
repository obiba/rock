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


import com.google.common.collect.Maps;
import org.obiba.rock.NodeProperties;
import org.obiba.rock.Resources;
import org.obiba.rock.model.RServerInfo;
import org.obiba.rock.model.RServerStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class RServerInfoService implements RServerInfo {

  @Autowired
  private RServerService rServerService;

  @Autowired
  private RSessionService rSessionService;

  @Autowired
  private NodeProperties nodeProperties;

  @Override
  public String getEncoding() {
    return Resources.getRserveEncoding();
  }

  @Override
  public String getId() {
    return nodeProperties.getId();
  }

  @Override
  public List<String> getTags() {
    return nodeProperties.getTags();
  }

  @Override
  public String getVersion() {
    return rServerService.getVersion();
  }

  @Override
  public RServerStatus getStatus() {
    return new RServerStatus() {
      @Override
      public boolean isRunning() {
        return rServerService.isRunning();
      }

      @Override
      public Map<String, Integer> getRSessionsCounts() {
        Map<String, Integer> counts = Maps.newHashMap();
        counts.put("total", rSessionService.getRSessionsCount());
        counts.put("busy", rSessionService.getBusyRSessionsCount());
        return counts;
      }
    };
  }
}
