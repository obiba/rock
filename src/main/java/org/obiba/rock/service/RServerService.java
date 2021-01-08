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

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.net.HostAndPort;
import com.orbitz.consul.AgentClient;
import com.orbitz.consul.Consul;
import com.orbitz.consul.model.agent.ImmutableRegistration;
import com.orbitz.consul.model.agent.Registration;
import org.obiba.rock.ClusterProperties;
import org.obiba.rock.RProperties;
import org.obiba.rock.Resources;
import org.obiba.rock.model.RServerState;
import org.rosuda.REngine.Rserve.RConnection;
import org.rosuda.REngine.Rserve.RserveException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Service to manage RServer process.
 */
@Component
public class RServerService implements RServerState {

    private static final Logger log = LoggerFactory.getLogger(RServerService.class);

    @Autowired
    private RProperties rProperties;

    @Autowired
    private ClusterProperties clusterProperties;

    @Autowired
    private RSessionService rSessionService;

    private int rserveStatus = -1;

    private boolean registeredInConsul;

    @Override
    public Integer getPort() {
        return Resources.getRservePort();
    }

    @Override
    public String getEncoding() {
        return Resources.getRserveEncoding();
    }

    @Override
    public String getCluster() {
        return clusterProperties.getName();
    }

    @Override
    public boolean isRunning() {
        return rserveStatus == 0;
    }

    @PostConstruct
    public void start() {
        if (rserveStatus == 0) {
            log.error("RServerService is already running");
            return;
        }

        doStart();
        if (isRunning()) registerService();
    }

    @PreDestroy
    public void stop() {
        if (rserveStatus != 0) return;
        doStop();
        unregisterService();
    }

    /**
     * Creates a new connection to R server.
     *
     * @return
     */
    public RConnection newConnection() {
        try {
            return newRConnection();
        } catch (RserveException e) {
            log.error("Error while connecting to R: {}", e.getMessage());
            throw new RuntimeException(e);
        }
    }

    /**
     * Check the running status and the R server is functional.
     *
     * @return
     */
    public boolean isAlive() {
        return isRunning() && isRServerAlive();
    }

    @Scheduled(fixedDelay = 10 * 1000)
    public void autoRestart() {
        if (isRunning() && !isRServerAlive()) {
            log.info("Rserve died, restarting...");
            doStop();
            doStart();
        }
    }

    /*
    @Scheduled(fixedDelay = 60 * 1000)
    public void consulCheck() {
        if (clusterProperties.hasConsul() && registeredInConsul) {
            boolean isRunning = isRunning();
            boolean isAlive = isRunning && rServerIsAlive();
            State state = isRunning ? (isAlive ? State.PASS : State.WARN) : State.FAIL;
            String note = isRunning ? (isAlive ? "Ready" : "R server has died") : "Stopped";
            try {
                makeConsulAgentClient().check(clusterProperties.getId(), state, note);
            } catch (Exception e) {
                log.warn("Service cannot check in Consul {}", clusterProperties.getConsul().getServer(), e);
            }
        }
    }
    */

    //
    // Private methods
    //

    private boolean isRServerAlive() {
        try {
            RConnection conn = newRConnection();
            conn.close();
            return true;
        } catch (RserveException e) {
            return false;
        }
    }

    private void doStart() {
        log.info("Start RServerService with {}", rProperties.getExec());

        // fresh start, try to kill any remains of R server
        try {
            newRConnection().shutdown();
        } catch (Exception e) {
            // ignore
        }

        try {
            // launch the Rserve daemon and wait for it to complete
            Process rserve = buildRProcess().start();
            rserveStatus = rserve.waitFor();
            if (rserveStatus == 0) {
                log.info("R server started");
            } else {
                log.error("R server start failed with status: {}", rserveStatus);
                rserveStatus = -1;
            }
        } catch (Exception e) {
            log.warn("R server start failed", e);
            rserveStatus = -1;
        }
    }

    private void doStop() {
        try {
            log.info("Closing all R sessions...");
            rSessionService.closeAllRSessions();
            log.info("Shutting down R server...");
            newConnection().shutdown();
        } catch (Exception e) {
            // ignore
        }

        try {
            rserveStatus = -1;
            log.info("R server shut down");
            File workDir = getWorkingDirectory();
            for (File file : Objects.requireNonNull(workDir.listFiles())) {
                delete(file);
            }
        } catch (Exception e) {
            log.error("R server shutdown failed", e);
        }
    }

    private void registerService() {
        if (clusterProperties.hasConsul()) {
            ClusterProperties.Consul consulConfig = clusterProperties.getConsul();
            try {
                Registration service = ImmutableRegistration.builder()
                        .id(clusterProperties.getId())
                        .name(clusterProperties.getName())
                        .port(6312)
                        .check(Registration.RegCheck.http(clusterProperties.getServer() + "/_check", clusterProperties.getInterval()))
                        //.check(Registration.RegCheck.ttl(120L)) // registers with a TTL of 2 min
                        .tags(clusterProperties.getTags())
                        //.meta(Collections.singletonMap("version", "1.0.0"));
                        .build();

                AgentClient consulAgentClient = makeConsulAgentClient();
                consulAgentClient.register(service);
                registeredInConsul = true;
                log.info("Service registered in Consul {}", consulConfig.getServer());
            } catch (Exception e) {
                registeredInConsul = false;
                log.warn("Unable to register service in Consul {}", consulConfig.getServer(), e);
            }
        }
    }

    private void unregisterService() {
        if (clusterProperties.hasConsul() && registeredInConsul) {
            ClusterProperties.Consul consulConfig = clusterProperties.getConsul();
            try {
                makeConsulAgentClient().deregister(clusterProperties.getId());
                log.info("Service unregistered from Consul {}", consulConfig.getServer());
            } catch (Exception e) {
                log.warn("Unable to unregistered service from Consul {}", consulConfig.getServer(), e);
            }

        }
    }

    private AgentClient makeConsulAgentClient() {
        ClusterProperties.Consul consulConfig = clusterProperties.getConsul();
        Consul.Builder builder = Consul.builder()
                .withHostAndPort(HostAndPort.fromString(consulConfig.getHostPort()))
                .withHttps(consulConfig.isHttps());
        if (consulConfig.hasToken())
            builder.withTokenAuth(consulConfig.getToken());
        Consul client = builder.build();
        return client.agentClient();
    }

    /**
     * Create a new RConnection given the R server settings.
     *
     * @return
     * @throws RserveException
     */
    private RConnection newRConnection() throws RserveException {
        RConnection conn = new RConnection();

        if (conn.needLogin()) {
            Map<String, String> conf = Resources.getRservConf();
            if (conf.containsKey("auth") && conf.get("auth").equals("required") && conf.containsKey("pwdfile")) {
                Map<String, String> pwds = Resources.getUsernamePasswords(conf.get("pwdfile"));
                if (!pwds.isEmpty()) {
                    Map.Entry<String, String> entry = pwds.entrySet().iterator().next();
                    conn.login(entry.getKey(), entry.getValue());
                }
            }
        }

        conn.setStringEncoding(Resources.getRserveEncoding());

        return conn;
    }

    private ProcessBuilder buildRProcess() {
        List<String> args = getArguments();
        log.info("Starting R server: {}", Joiner.on(" ").join(args));
        ProcessBuilder pb = new ProcessBuilder(args);
        pb.directory(getWorkingDirectory());
        pb.redirectErrorStream(true);
        pb.redirectOutput(ProcessBuilder.Redirect.appendTo(getRserveLogFile()));
        return pb;
    }

    private List<String> getArguments() {
        StringBuffer rserveArgs = new StringBuffer("--vanilla");
        File workDir = getWorkingDirectory();
        rserveArgs.append(" --RS-workdir ").append(workDir.getAbsolutePath());

        File conf = Resources.getRservConfFile();
        if (conf.exists()) {
            rserveArgs.append(" --RS-conf ").append(conf.getAbsolutePath());
        }

        List<String> args = Lists.newArrayList(rProperties.getExec(), "-e", "library(Rserve) ; Rserve(args='" + rserveArgs + "')");

        return args;
    }

    private void delete(File file) {
        if (file.isDirectory()) {
            for (File f : Objects.requireNonNull(file.listFiles())) {
                delete(f);
            }
        }
        if (!file.isDirectory() || Objects.requireNonNull(file.list()).length == 0) {
            if (!file.delete()) {
                log.warn("Unable to delete file: " + file.getAbsolutePath());
            }
        }
    }

    private File getWorkingDirectory() {
        File dir = new File(Resources.getRServerHomeDir(), "work" + File.separator + "R");
        if (!dir.exists()) {
            if (!dir.mkdirs()) {
                log.error("Unable to create: {}", dir.getAbsolutePath());
            }
        }
        return dir;
    }

    private File getRserveLogFile() {
        File logFile = new File(Resources.getRServerHomeDir(), "logs" + File.separator + "Rserve.log");
        if (!logFile.getParentFile().exists()) {
            if (!logFile.getParentFile().mkdirs()) {
                log.error("Unable to create: {}", logFile.getParentFile().getAbsolutePath());
            }
        }
        return logFile;
    }

}
