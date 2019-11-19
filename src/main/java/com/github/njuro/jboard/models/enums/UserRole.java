package com.github.njuro.jboard.models.enums;

import org.springframework.security.core.GrantedAuthority;

import java.util.Arrays;

/**
 * Enum representing user roles for authorizing purposes
 * <p>
 * Roles have following hierarchy:
 * ADMIN > MODERATOR > JANITOR > USER
 *
 * @author njuro
 */
public enum UserRole implements GrantedAuthority {
    USER(Roles.USER_ROLE), ADMIN(Roles.ADMIN_ROLE), MODERATOR(Roles.MODERATOR_ROLE), JANITOR(Roles.JANITOR_ROLE);

    private String authority;

    UserRole(String authority) {
        this.authority = authority;
    }

    @Override
    public String getAuthority() {
        return authority;
    }

    public static UserRole getByAuthority(String authority) {
        return Arrays.stream(UserRole.values()).filter(role -> role.getAuthority().equals(authority)).findAny().orElse(null);
    }

    public static class Roles {
        public static final String USER_ROLE = "ROLE_USER";
        public static final String ADMIN_ROLE = "ROLE_ADMIN";
        public static final String MODERATOR_ROLE = "ROLE_MODERATOR";
        public static final String JANITOR_ROLE = "ROLE_JANITOR";

        public static final String HIERARCHY = ADMIN_ROLE + " > " + MODERATOR_ROLE + "\n" +
                MODERATOR_ROLE + " > " + JANITOR_ROLE + "\n" +
                JANITOR_ROLE + " > " + USER_ROLE;
    }
}
