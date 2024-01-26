/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.rock.security;

import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.www.BasicAuthenticationEntryPoint;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

public class CustomBasicAuthenticationEntryPoint extends BasicAuthenticationEntryPoint {

  @Override
  public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authEx) throws IOException {
    response.addHeader("WWW-Authenticate", "Basic realm=\"" + getRealmName() + "\"");
    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    response.setContentType("application/json");
    PrintWriter writer = response.getWriter();
    writer.println("{\"status\": \"401\", \"key\": \"UnAuthorized\", \"message\": \"" + authEx.getMessage() + "\"}");
  }

  @Override
  public void afterPropertiesSet() {
    setRealmName("RockRealm");
    super.afterPropertiesSet();
  }
}
