package com.github.njuro.models.enums;

import org.springframework.security.core.GrantedAuthority;

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

    public static class Roles {
        public static final String USER_ROLE = "ROLE_USER";
        public static final String ADMIN_ROLE = "ROLE_ADMIN";
        public static final String MODERATOR_ROLE = "ROLE_MODERATOR";
        public static final String JANITOR_ROLE = "ROLE_JANITOR";
    }
}
