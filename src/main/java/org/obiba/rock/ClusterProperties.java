/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.rock;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@ConfigurationProperties(value = "cluster")
@Component
public class ClusterProperties {

    private String server = "http://localhost:6312";

    private int interval = 10;

    private String id = "rserver";

    private String name = "rock";

    private List<String> tags = Lists.newArrayList("default");

    private Consul consul;

    public void setId(String id) {
        if (!Strings.isNullOrEmpty(id))
            this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setServer(String publicAddress) {
        if (!Strings.isNullOrEmpty(publicAddress))
            this.server = publicAddress;
    }

    public String getServer() {
        return server;
    }

    public int getInterval() {
        return interval;
    }

    public void setInterval(int interval) {
        this.interval = interval;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        if (!Strings.isNullOrEmpty(name))
            this.name = name;
    }

    public void setTags(List<String> tags) {
        if (tags != null && !tags.isEmpty())
            this.tags = tags;
    }

    public List<String> getTags() {
        return tags;
    }

    public Consul getConsul() {
        return consul;
    }

    public void setConsul(Consul consul) {
        this.consul = consul;
    }

    public boolean hasConsul() {
        return consul != null;
    }

    public static class Consul {
        private String server;
        private String token;

        public String getServer() {
            return server;
        }

        public void setServer(String server) {
            this.server = server;
        }

        public boolean isHttps() {
            return !Strings.isNullOrEmpty(server) && server.startsWith("https://");
        }

        public String getHostPort() {
            return server.replaceAll("https://", "").replaceAll("http://", "");
        }

        public String getToken() {
            return token;
        }

        public void setToken(String token) {
            this.token = token;
        }

        public boolean hasToken() {
            return !Strings.isNullOrEmpty(token);
        }
    }

    public static class Opal {
        private String server;

        public String getServer() {
            return server;
        }

        public void setServer(String server) {
            this.server = server;
        }
    }
}
