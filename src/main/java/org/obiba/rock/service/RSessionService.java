package org.obiba.rock.service;


import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import org.obiba.rock.domain.RServeSession;
import org.obiba.rock.model.RSession;
import org.obiba.rock.r.RRuntimeException;
import org.rosuda.REngine.Rserve.RConnection;
import org.rosuda.REngine.Rserve.RserveException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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
    private RServerService rServerService;

    private Map<String, RServeSession> rSessions = Maps.newConcurrentMap();

    public List<RSession> getRSessions() {
        return rSessions.values().stream().map(s -> (RSession) s).collect(Collectors.toList());
    }

    public RSession createRSession(String subject) {
        RServeSession rSession = new RServeSession(subject, newRConnection());
        rSessions.put(rSession.getId(), rSession);
        return rSession;
    }

    public RSession getRSession(String id) {
        return getRServeSession(id);
    }

    public void closeRSession(String id) {
        RServeSession rSession = rSessions.get(id);
        if (rSession == null) return;
        rSessions.remove(id);
        rSession.close();
    }

    public RServeSession getRServeSession(String id) {
        RServeSession rSession = rSessions.get(id);
        if (rSession == null) throw new RSessionNotFoundException(id);
        return rSession;
    }

    /**
     * Creates a new connection to R server.
     *
     * @return
     * @throws RserveException
     */
    private RConnection newRConnection() {
        RConnection conn;

        try {
            conn = new RConnection(getHost(), getPort());

            if (conn.needLogin()) {
                //conn.login(username, password);
            }

            if (!Strings.isNullOrEmpty(rServerService.getEncoding())) {
                conn.setStringEncoding(rServerService.getEncoding());
            }
        } catch (RserveException e) {
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
