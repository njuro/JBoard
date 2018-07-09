package com.github.njuro.controllers.security;

import com.github.njuro.models.User;
import com.github.njuro.models.dto.RegisterForm;
import com.github.njuro.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

@Controller
public class LoginController {

    private final UserService userService;

    private final PasswordEncoder passwordEncoder;

    @Autowired
    public LoginController(UserService userService, PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/auth/register")
    public String showRegisterForm() {
        return "register";
    }

    @PostMapping("/auth/register")
    public String registerUser(@Valid @ModelAttribute("registerForm") RegisterForm registerForm,
                               BindingResult result, RedirectAttributes redirectAttributes, HttpServletRequest request) {
        try {
            userService.loadUserByUsername(registerForm.getUsername());
            result.rejectValue("username", "username.exists", "Username is already registered");
        } catch (UsernameNotFoundException e) {
            // ok
        }

        if (userService.getUserByEmail(registerForm.getEmail()) != null) {
            result.rejectValue("email", "email.exists", "E-mail is already registered");
        }

        if (result.hasErrors()) {
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.registerForm", result);
            redirectAttributes.addFlashAttribute("registerForm", registerForm);
            return "redirect:/auth/register";
        }

        User user = userService.createUser(registerForm, passwordEncoder);
        user.setRegistrationIp(request.getRemoteAddr());
        userService.saveUser(user);

        return "redirect:/";
    }

    @GetMapping("/login")
    public String showLoginForm() {
        return "login";
    }


}
