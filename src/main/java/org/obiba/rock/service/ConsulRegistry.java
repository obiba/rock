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

import com.google.common.collect.Lists;
import com.google.common.net.HostAndPort;
import com.orbitz.consul.AgentClient;
import com.orbitz.consul.Consul;
import com.orbitz.consul.model.agent.ImmutableRegistration;
import com.orbitz.consul.model.agent.Registration;
import org.obiba.rock.ConsulProperties;
import org.obiba.rock.NodeProperties;
import org.obiba.rock.model.Registry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Perform the registering of the R service in Consul.
 */
@Component
public class ConsulRegistry implements Registry {

  private static final Logger log = LoggerFactory.getLogger(ConsulRegistry.class);

  private final NodeProperties nodeProperties;

  private final ConsulProperties consulProperties;

  private boolean registered;

  @Value("${server.port}")
  private Integer port;

  @Autowired
  public ConsulRegistry(NodeProperties nodeProperties, ConsulProperties consulProperties) {
    this.nodeProperties = nodeProperties;
    this.consulProperties = consulProperties;
  }

  public boolean isDefined() {
    return consulProperties.isDefined();
  }

  public boolean isRegistered() {
    return registered;
  }

  @Override
  public void register() {
    if (consulProperties.isDefined() && !registered && nodeProperties.hasServer()) {
      if (!nodeProperties.hasServer()) {
        log.warn("Cannot register in Consul without the node's server address configured");
        return;
      }
      try {
        List<String> tags = Lists.newArrayList(nodeProperties.getTags());
        if (!tags.contains(nodeProperties.getCluster()))
          tags.add(0, nodeProperties.getCluster());
        Registration service = ImmutableRegistration.builder()
            .id(nodeProperties.getId())
            .name(nodeProperties.getType())
            .port(port)
            .check(Registration.RegCheck.http(nodeProperties.getServer() + "/_check", consulProperties.getInterval()))
            .tags(tags)
            .build();

        AgentClient consulAgentClient = makeConsulAgentClient();
        consulAgentClient.register(service);
        registered = true;
        log.info("Service registered in Consul {}", consulProperties.getServer());
      } catch (Exception e) {
        registered = false;
        if (log.isDebugEnabled())
          log.warn("Unable to register service in Consul {}", consulProperties.getServer(), e);
        else
          log.warn("Unable to register service in Consul {}: {}", consulProperties.getServer(), e.getMessage());
      }
    }
  }

  @Override
  public void unregister() {
    if (consulProperties.isDefined() && registered) {
      try {
        makeConsulAgentClient().deregister(nodeProperties.getId());
        log.info("Service unregistered from Consul {}", consulProperties.getServer());
      } catch (Exception e) {
        if (log.isDebugEnabled())
          log.warn("Unable to unregistered service from Consul {}", consulProperties.getServer(), e);
        else
          log.warn("Unable to unregistered service from Consul {}: {}", consulProperties.getServer(), e.getMessage());
      }
      registered = false;
    }
  }

  //
  // Private methods
  //

  private AgentClient makeConsulAgentClient() {
    Consul.Builder builder = Consul.builder()
        .withHostAndPort(HostAndPort.fromString(consulProperties.getHostPort()))
        .withHttps(consulProperties.isHttps());
    if (consulProperties.hasToken())
      builder.withTokenAuth(consulProperties.getToken());
    Consul client = builder.build();
    return client.agentClient();
  }
}
