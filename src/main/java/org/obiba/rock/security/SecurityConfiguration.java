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

import com.google.common.base.Joiner;
import org.obiba.rock.SecurityProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.authentication.configurers.provisioning.InMemoryUserDetailsManagerConfigurer;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.www.BasicAuthenticationEntryPoint;

import java.util.List;
import java.util.stream.Collectors;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(securedEnabled = true)
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {

  private static final Logger log = LoggerFactory.getLogger(SecurityConfiguration.class);

  @Autowired
  private SecurityProperties securityProperties;

  @Autowired
  private PasswordEncoder passwordEncoder;

  @Autowired
  private BasicAuthenticationEntryPoint authenticationEntryPoint;

  @Override
  protected void configure(HttpSecurity http) throws Exception {
    http.csrf().disable()
        .formLogin().disable()
        .authorizeRequests()
        .antMatchers("/rserver/**").hasAnyRole(Roles.ROCK_ADMIN, Roles.ROCK_MANAGER)
        .antMatchers("/r/sessions/**").hasAnyRole(Roles.ROCK_ADMIN, Roles.ROCK_MANAGER, Roles.ROCK_USER)
        .antMatchers("/r/session/**").hasAnyRole(Roles.ROCK_ADMIN, Roles.ROCK_USER)
        .antMatchers("/_check").anonymous()
        .anyRequest().denyAll()
        .and().httpBasic().realmName("RockRealm")
        .authenticationEntryPoint(authenticationEntryPoint)
        .and().sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);
  }

  @Bean
  public BasicAuthenticationEntryPoint getBasicAuthenticationEntryPoint() {
    return new CustomBasicAuthenticationEntryPoint();
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Bean
  public AuthenticationManager authenticationManagerBean() throws Exception {
    // FIXME required to prevent spring boot auto-config
    return super.authenticationManagerBean();
  }

  @Override
  protected void configure(AuthenticationManagerBuilder auth) throws Exception {
    List<SecurityProperties.User> users = securityProperties.getUsers();
    InMemoryUserDetailsManagerConfigurer<AuthenticationManagerBuilder> configurer = auth.inMemoryAuthentication()
        .passwordEncoder(passwordEncoder);
    users.forEach(u -> {
      log.debug(u.getId() + ":" + u.getSecret() + ":" + Joiner.on(";").join(u.getRoles()));
      String[] roles = new String[u.getRoles().size()];
      roles = u.getRoles().stream().map(String::toUpperCase).collect(Collectors.toList()).toArray(roles);
      configurer.withUser(u.getId())
          .password(securityProperties.isEncoded() ? u.getSecret() : passwordEncoder.encode(u.getSecret()))
          .roles(roles);
    });
  }


}
