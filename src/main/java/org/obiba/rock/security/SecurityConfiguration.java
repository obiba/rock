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
import com.google.common.collect.Maps;
import org.obiba.rock.SecurityProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.authentication.configurers.provisioning.InMemoryUserDetailsManagerConfigurer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.DelegatingPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.crypto.scrypt.SCryptPasswordEncoder;
import org.springframework.security.web.authentication.www.BasicAuthenticationEntryPoint;

import java.security.SecureRandom;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Configuration
@EnableWebSecurity
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {

  private static final Logger log = LoggerFactory.getLogger(SecurityConfiguration.class);

  @Autowired
  private SecurityProperties securityProperties;

  @Override
  protected void configure(HttpSecurity http) throws Exception {
    if (!securityProperties.isEnabled())
      http.csrf().disable()
          .formLogin().disable()
          .authorizeRequests().antMatchers("/**").permitAll();
    else
      http.csrf().disable()
          .formLogin().disable()
          .authorizeRequests()
          .antMatchers("/rserver/**").hasAnyRole(Roles.ROCK_ADMIN, Roles.ROCK_MANAGER)
          .antMatchers("/r/sessions/**").hasAnyRole(Roles.ROCK_ADMIN, Roles.ROCK_MANAGER, Roles.ROCK_USER)
          .antMatchers("/r/session/**").hasAnyRole(Roles.ROCK_ADMIN, Roles.ROCK_USER)
          .antMatchers("/").permitAll()
          .antMatchers("/_check").permitAll()
          .antMatchers("/_info").permitAll()
          .anyRequest().denyAll()
          .and().httpBasic().realmName("RockRealm")
          .authenticationEntryPoint(getBasicAuthenticationEntryPoint())
          .and().sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);
  }

  private BasicAuthenticationEntryPoint getBasicAuthenticationEntryPoint() {
    return new CustomBasicAuthenticationEntryPoint();
  }

  private PasswordEncoder newPasswordEncoder() {
    Map<String, PasswordEncoder> encoders = Maps.newHashMap();
    encoders.put("noop", newNoOpPasswordEncoder());
    encoders.put("bcrypt", new BCryptPasswordEncoder(-1, new SecureRandom()));
    encoders.put("scrypt", new SCryptPasswordEncoder());
    return new DelegatingPasswordEncoder("noop", encoders);
  }

  private PasswordEncoder newNoOpPasswordEncoder() {
    return new PasswordEncoder() {
      @Override
      public String encode(CharSequence rawPassword) {
        return rawPassword.toString();
      }

      @Override
      public boolean matches(CharSequence rawPassword, String encodedPassword) {
        return rawPassword.toString().equals(encodedPassword);
      }
    };
  }

  @Bean
  public AuthenticationManager authenticationManagerBean() throws Exception {
    // FIXME required to prevent spring boot auto-config
    return super.authenticationManagerBean();
  }

  @Override
  protected void configure(AuthenticationManagerBuilder auth) throws Exception {
    if (!securityProperties.isEnabled()) return;

    PasswordEncoder passwordEncoder = newPasswordEncoder();
    List<SecurityProperties.User> users = securityProperties.getUsers();
    InMemoryUserDetailsManagerConfigurer<AuthenticationManagerBuilder> configurer = auth.inMemoryAuthentication()
        .passwordEncoder(passwordEncoder);
    users.forEach(u -> {
      log.debug(u.getId() + ":" + u.getSecret() + ":" + Joiner.on(";").join(u.getRoles()));
      String[] roles = new String[u.getRoles().size()];
      roles = u.getRoles().stream().map(String::toUpperCase).collect(Collectors.toList()).toArray(roles);
      configurer.withUser(u.getId())
          .password(u.getSecret().startsWith("{") ? u.getSecret() : passwordEncoder.encode(u.getSecret()))
          .roles(roles);
    });
  }


}
