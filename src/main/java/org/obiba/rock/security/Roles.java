package org.obiba.rock.security;

public interface Roles {
    // can do all
    String ROCK_ADMIN = "rock-administrator";

    // can manage the R server
    String ROCK_MANAGER = "rock-manager";

    // can create R sessions
    String ROCK_USER = "rock-user";
}
