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

import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@ConfigurationProperties(value = "node")
@Component
public class NodeProperties {

  private String server;

  private String id = "rserver";

  private String type = "rock";

  private String cluster = "default";

  private List<String> tags = Lists.newArrayList();

  public void setId(String id) {
    if (!Strings.isNullOrEmpty(id))
      this.id = id;
  }

  public String getId() {
    return id;
  }

  public void setServer(String publicAddress) {
    this.server = publicAddress;
  }

  public String getServer() {
    return server;
  }

  public boolean hasServer() {
    return !Strings.isNullOrEmpty(server);
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    if (!Strings.isNullOrEmpty(type))
      this.type = type;
  }

  public String getCluster() {
    return cluster;
  }

  public void setCluster(String cluster) {
    if (!Strings.isNullOrEmpty(cluster))
      this.cluster = cluster;
  }

  public void setTags(List<String> tags) {
    if (tags != null && !tags.isEmpty())
      this.tags = tags;
  }

  public List<String> getTags() {
    return tags;
  }

  public String asJSON() {
    String body = String.format("\"id\": \"%s\", \"type\": \"%s\", \"cluster\": \"%s\"", getId(), getType(), getCluster());
    if (tags.isEmpty())
      body = String.format("%s,\"tags\": []", body);
    else
      body = String.format("%s,\"tags\": [\"%s\"]", body, Joiner.on("\", \"").join(getTags()));
    if (hasServer())
      body = String.format("%s,\"server\": \"%s\"", body, getServer());
    return String.format("{ %s }", body);
  }
}
