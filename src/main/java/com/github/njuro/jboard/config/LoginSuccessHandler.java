package com.github.njuro.jboard.config;

import com.github.njuro.jboard.models.User;
import com.github.njuro.jboard.services.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDateTime;

/**
 * Custom login success handler, which intercepts the request to update last login time and IP of {@link User} who
 * just authenticated, logs the event and redirects user to authenticated section.
 *
 * @author njuro
 * @see SecurityConfig
 */
@Component
@Slf4j
public class LoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final UserService userService;

    @Autowired
    public LoginSuccessHandler(UserService userService) {
        this.userService = userService;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        User user = (User) authentication.getPrincipal();
        user.setLastLoginIp(request.getRemoteAddr());
        user.setLastLogin(LocalDateTime.now());
        userService.saveUser(user);

        log.debug("User {} logged from IP {}", user.getUsername(), user.getLastLoginIp());

        // redirect to authenticated section
        setDefaultTargetUrl("/auth");
        super.onAuthenticationSuccess(request, response, authentication);
    }
}