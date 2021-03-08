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


import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import org.obiba.rock.NodeProperties;
import org.obiba.rock.RProperties;
import org.obiba.rock.Resources;
import org.obiba.rock.SecurityProperties;
import org.obiba.rock.domain.RServeSession;
import org.obiba.rock.model.RSession;
import org.obiba.rock.r.RRuntimeException;
import org.obiba.rock.security.Roles;
import org.rosuda.REngine.Rserve.RConnection;
import org.rosuda.REngine.Rserve.RserveException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Service to manage the R sessions.
 */
@Component
public class RSessionService {

  private static final Logger log = LoggerFactory.getLogger(RSessionService.class);

  @Autowired
  private RProperties rProperties;

  @Autowired
  private RServerService rServerService;

  @Autowired
  private NodeProperties nodeProperties;

  @Autowired
  private SecurityProperties securityProperties;

  private Map<String, RServeSession> rSessions = Maps.newConcurrentMap();

  public List<RSession> getRSessions() {
    return rSessions.values().stream().map(s -> (RSession) s).collect(Collectors.toList());
  }

  public int getRSessionsCount() {
    return rSessions.values().size();
  }

  public int getBusyRSessionsCount() {
    return (int) rSessions.values().stream().filter(RServeSession::getBusy).count();
  }

  public String getUsername(User user) {
    return user == null ? "?" : user.getUsername();
  }

  public RSession createRSession(User user) {
    RServeSession rSession = new RServeSession(getUsername(user), newRConnection(Roles.isAdmin(user)));
    rSessions.put(rSession.getId(), rSession);
    return rSession;
  }

  public RSession getRSession(String id) {
    return getRServeSession(id);
  }

  public void closeRSession(String id) {
    RServeSession rSession = rSessions.get(id);
    if (rSession == null) return;
    log.info("Closing R session: {}", id);
    rSessions.remove(id);
    rSession.close();
  }

  public void closeAllRSessions() {
    rSessions.keySet().forEach(this::closeRSession);
  }

  public RServeSession getRServeSession(String id) {
    RServeSession rSession = rSessions.get(id);
    if (rSession == null) throw new RSessionNotFoundException(id);
    return rSession;
  }

  @Scheduled(fixedDelay = 600 * 1000)
  public void checkExpiredSessions() {
    if (rProperties.getSessionTimeout() > 0)
      rSessions.values().stream()
          .filter(s -> s.hasExpired(rProperties.getSessionTimeout()))
          .forEach(s -> closeRSession(s.getId()));
  }

  /**
   * Creates a new connection to R server. Apply RAppArmor profile if defined and if user is not administrator.
   *
   * @param admin
   * @return
   * @throws RserveException
   */
  private RConnection newRConnection(boolean admin) {
    RConnection conn;

    try {
      conn = new RConnection(getHost(), getPort());

      if (conn.needLogin()) {
        //conn.login(username, password);
      }

      if (!Strings.isNullOrEmpty(Resources.getRserveEncoding())) {
        conn.setStringEncoding(Resources.getRserveEncoding());
      }

      if (!admin && securityProperties.withAppArmor()) {
        conn.eval(String.format("RAppArmor::aa_change_profile('%s')", securityProperties.getAppArmor().getProfile()));
      }

      conn.eval(String.format(".info <- jsonlite::fromJSON('%s')", nodeProperties.asJSON()));
    } catch (Exception e) {
      log.error("Error while connecting to R ({}:{}): {}", getHost(), getPort(), e.getMessage());
      throw new RRuntimeException(e);
    }

    return conn;
  }

  private String getHost() {
    return "127.0.0.1";
  }

  private int getPort() {
    return rServerService.getPort();
  }

}
