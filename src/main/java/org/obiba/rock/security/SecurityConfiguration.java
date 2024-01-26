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
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.DelegatingPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.crypto.password.Pbkdf2PasswordEncoder;
import org.springframework.security.crypto.scrypt.SCryptPasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.www.BasicAuthenticationEntryPoint;

import java.security.SecureRandom;
import java.util.List;
import java.util.Map;

@Configuration
@EnableWebSecurity
public class SecurityConfiguration {

  private static final Logger log = LoggerFactory.getLogger(SecurityConfiguration.class);

  @Autowired
  private SecurityProperties securityProperties;

  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    configure(http);
    return http.build();
  }

  //
  // Private methods
  //

  private void configure(HttpSecurity http) throws Exception {
    if (!securityProperties.isEnabled())
      http
          .csrf(AbstractHttpConfigurer::disable)
          .authorizeHttpRequests((configurer) -> configurer.requestMatchers("/**").permitAll());
    else
      http
          .csrf(AbstractHttpConfigurer::disable)
          .formLogin(AbstractHttpConfigurer::disable)
          .authorizeHttpRequests((configurer) -> configurer
              .requestMatchers("/rserver/**").hasAnyRole(Roles.ROCK_ADMIN, Roles.ROCK_MANAGER)
              .requestMatchers("/r/sessions/**").hasAnyRole(Roles.ROCK_ADMIN, Roles.ROCK_MANAGER, Roles.ROCK_USER)
              .requestMatchers("/r/session/**").hasAnyRole(Roles.ROCK_ADMIN, Roles.ROCK_USER)
              .requestMatchers("/").permitAll()
              .requestMatchers("/_check").permitAll()
              .requestMatchers("/_info").permitAll()
              .anyRequest().denyAll())
          .httpBasic((configurer) -> configurer
              .realmName("RockRealm")
              .authenticationEntryPoint(getBasicAuthenticationEntryPoint()))
          .sessionManagement((configurer) -> configurer.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
  }

  private BasicAuthenticationEntryPoint getBasicAuthenticationEntryPoint() {
    return new CustomBasicAuthenticationEntryPoint();
  }

  private PasswordEncoder newPasswordEncoder() {
    Map<String, PasswordEncoder> encoders = Maps.newHashMap();
    encoders.put("noop", newNoOpPasswordEncoder());
    encoders.put("bcrypt", new BCryptPasswordEncoder(-1, new SecureRandom()));
    encoders.put("pbkdf2", Pbkdf2PasswordEncoder.defaultsForSpringSecurity_v5_8());
    encoders.put("scrypt", SCryptPasswordEncoder.defaultsForSpringSecurity_v5_8());
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
  public UserDetailsService userDetailsService() {
    InMemoryUserDetailsManager manager = new InMemoryUserDetailsManager();

    if (!securityProperties.isEnabled()) return manager;

    PasswordEncoder passwordEncoder = newPasswordEncoder();
    List<SecurityProperties.User> users = securityProperties.getUsers();
    users.forEach(u -> {
      log.debug(u.getId() + ":" + u.getSecret() + ":" + Joiner.on(";").join(u.getRoles()));
      String[] roles = new String[u.getRoles().size()];
      roles = u.getRoles().stream().map(String::toUpperCase).toList().toArray(roles);
      manager.createUser(User.withUsername(u.getId())
          .password(u.getSecret().startsWith("{") ? u.getSecret() : passwordEncoder.encode(u.getSecret()))
          .roles(roles)
          .build());
    });
    return manager;
  }

}
