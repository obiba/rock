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

import okhttp3.*;
import org.obiba.rock.NodeProperties;
import org.obiba.rock.OpalProperties;
import org.obiba.rock.model.Registry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Perform the registering of the R service in a Opal server.
 */
@Component
public class OpalRegistry implements Registry {

    private static final Logger log = LoggerFactory.getLogger(OpalRegistry.class);

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
            try {
                OkHttpClient client = new OkHttpClient();
                String json = String.format("{ name: '%s', type: '%s', server: '%s' }", nodeProperties.getId(), nodeProperties.getName(), nodeProperties.getServer());
                RequestBody body = RequestBody.create(MediaType.get("application/json"), json);
                Request request = new Request.Builder()
                        .url(opalProperties.getServer() + "/ws/apps")
                        .post(body)
                        .build();
                try (Response response = client.newCall(request).execute()) {
                    if (response.isSuccessful()) {
                        registered = true;
                        log.info("Service registered in Opal {}", opalProperties.getServer());
                    } else {
                        String resp = response.body().string();
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
                Request request = new Request.Builder()
                        .url(opalProperties.getServer() + "/ws/app/" + nodeProperties.getId())
                        .delete()
                        .build();
                try (Response response = client.newCall(request).execute()) {
                    if (response.isSuccessful()) {
                        log.info("Service unregistered from Opal {}", opalProperties.getServer());
                    } else {
                        String resp = response.body().string();
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

}
