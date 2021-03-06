package com.github.njuro.jard.config.security.methods;

import java.util.Collections;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.AccessDecisionManager;
import org.springframework.security.access.method.MethodSecurityMetadataSource;
import org.springframework.security.access.vote.AffirmativeBased;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.method.configuration.GlobalMethodSecurityConfiguration;

/**
 * Registers {@link AuthorityVoter} as mechanism for granting access to endpoints.
 *
 * @see HasAuthorities
 */
@Configuration
@EnableGlobalMethodSecurity
public class MethodSecurityConfig extends GlobalMethodSecurityConfiguration {

  @Override
  protected MethodSecurityMetadataSource customMethodSecurityMetadataSource() {
    return new AuthorityMetadataSource();
  }

  @Override
  protected AccessDecisionManager accessDecisionManager() {
    return new AffirmativeBased(Collections.singletonList(new AuthorityVoter()));
  }
}
