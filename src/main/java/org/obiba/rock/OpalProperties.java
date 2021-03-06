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
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@ConfigurationProperties(value = "opal")
@Component
public class OpalProperties {

  private String server;

  private String token;

  public boolean isDefined() {
    return !Strings.isNullOrEmpty(server);
  }

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
