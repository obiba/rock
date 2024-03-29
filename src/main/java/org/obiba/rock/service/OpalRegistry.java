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
import okhttp3.*;
import org.obiba.rock.NodeProperties;
import org.obiba.rock.OpalProperties;
import org.obiba.rock.model.Registry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

/**
 * Perform the registering of the R service in a Opal server.
 */
@Component
public class OpalRegistry implements Registry {

  private static final Logger log = LoggerFactory.getLogger(OpalRegistry.class);

  private static final String APP_AUTH_HEADER = "X-App-Auth";

  private final NodeProperties nodeProperties;

  private final OpalProperties opalProperties;

  private boolean registered;

  @Autowired
  public OpalRegistry(NodeProperties nodeProperties, OpalProperties opalProperties) {
    this.nodeProperties = nodeProperties;
    this.opalProperties = opalProperties;
  }

  public boolean isDefined() {
    return opalProperties.isDefined();
  }

  public boolean isRegistered() {
    return registered;
  }

  @Override
  public void register() {
    if (opalProperties.isDefined() && !registered) {
      if (!nodeProperties.hasServer()) {
        log.warn("Cannot register in Opal without the node's server address configured");
        return;
      }
      try {
        OkHttpClient client = new OkHttpClient();
        RequestBody body = makeRequestBody();
        Request.Builder builder = new Request.Builder()
            .url(opalProperties.getServer() + "/ws/apps")
            .post(body);
        if (opalProperties.hasToken())
          builder.header(APP_AUTH_HEADER, opalProperties.getToken());
        try (Response response = client.newCall(builder.build()).execute()) {
          if (response.isSuccessful()) {
            registered = true;
            log.info("Service registered in Opal {}", opalProperties.getServer());
          } else {
            String resp = response.body() == null ? "" : response.body().string();
            registered = false;
            log.warn("Unable to register service in Opal {}: {}", opalProperties.getServer(), resp);
          }
        }
      } catch (Exception e) {
        registered = false;
        if (log.isDebugEnabled())
          log.warn("Unable to register service in Opal {}", opalProperties.getServer(), e);
        else
          log.warn("Unable to register service in Opal {}: {}", opalProperties.getServer(), e.getMessage());
      }
    }
  }

  @Override
  public void unregister() {
    if (opalProperties.isDefined() && registered) {
      try {
        OkHttpClient client = new OkHttpClient();
        RequestBody body = makeRequestBody();
        Request.Builder builder = new Request.Builder()
            .url(opalProperties.getServer() + "/ws/apps")
            .delete(body);
        if (opalProperties.hasToken())
          builder.header(APP_AUTH_HEADER, opalProperties.getToken());
        try (Response response = client.newCall(builder.build()).execute()) {
          if (response.isSuccessful()) {
            log.info("Service unregistered from Opal {}", opalProperties.getServer());
          } else {
            String resp = response.body() == null ? "" : response.body().string();
            log.warn("Unable to unregister service from Opal {}: {}", opalProperties.getServer(), resp);
          }
        }
      } catch (Exception e) {
        if (log.isDebugEnabled())
          log.warn("Unable to unregistered service from Opal {}", opalProperties.getServer(), e);
        else
          log.warn("Unable to unregistered service from Opal {}: {}", opalProperties.getServer(), e.getMessage());
      }
      registered = false;
    }
  }

  private RequestBody makeRequestBody() {
    return RequestBody.create(asOpalAppJSON(), MediaType.get("application/json"));
  }

  public String asOpalAppJSON() {
    String body = String.format("\"name\": \"%s\", \"type\": \"%s\", \"cluster\": \"%s\"", nodeProperties.getId(), nodeProperties.getType(), nodeProperties.getCluster());
    if (nodeProperties.getTags().isEmpty())
      body = String.format("%s,\"tags\": []", body);
    else
      body = String.format("%s,\"tags\": [\"%s\"]", body, Joiner.on("\", \"").join(nodeProperties.getTags()));
    if (nodeProperties.hasServer())
      body = String.format("%s,\"server\": \"%s\"", body, nodeProperties.getServer());
    return String.format("{ %s }", body);
  }

}
