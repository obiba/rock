package org.obiba.rock.security;

import org.springframework.security.core.userdetails.User;

public class Roles {
    // can do all
    public static final String ROCK_ADMIN = "ADMINISTRATOR";
    public static final String ROLE_ADMIN = "ROLE_" + ROCK_ADMIN;

    // can manage the R server and the R sessions
    public static final String ROCK_MANAGER = "MANAGER";
    public static final String ROLE_MANAGER = "ROLE_" + ROCK_MANAGER;

    // can create R sessions and use them
    public static final String ROCK_USER = "USER";
    public static final String ROLE_USER = "ROLE_" + ROCK_USER;

    public static boolean isAdmin(User user) {
        return user.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals(Roles.ROLE_ADMIN));
    }

    public static boolean isManager(User user) {
        return user.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals(Roles.ROLE_MANAGER));
    }
}
