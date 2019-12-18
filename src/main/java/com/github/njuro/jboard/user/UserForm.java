package com.github.njuro.jboard.user;

import static com.github.njuro.jboard.common.Constants.IP_PATTERN;
import static com.github.njuro.jboard.common.Constants.MAX_USERNAME_LENGTH;
import static com.github.njuro.jboard.common.Constants.MIN_PASSWORD_LENGTH;
import static com.github.njuro.jboard.common.Constants.MIN_USERNAME_LENGTH;

import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.Email;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import lombok.Data;

/** Data transfer object for "create new user" form */
@Data
public class UserForm {

  @Size(
      min = MIN_USERNAME_LENGTH,
      max = MAX_USERNAME_LENGTH,
      message =
          "Username must have between "
              + MIN_USERNAME_LENGTH
              + " and "
              + MAX_USERNAME_LENGTH
              + " chars")
  private String username;

  @Size(
      min = MIN_PASSWORD_LENGTH,
      message = "Password too short (must be at least " + MIN_PASSWORD_LENGTH + ") chars")
  private String password;

  private String passwordRepeated;

  @Email(message = "Invalid email")
  private String email;

  @Pattern(regexp = IP_PATTERN)
  private String registrationIp;

  @AssertTrue(message = "Passwords do not match")
  public boolean isPasswordMatching() {
    return password.equals(passwordRepeated);
  }

  public User toUser() {
    return User.builder()
        .username(username)
        .password(password)
        .email(email)
        .registrationIp(registrationIp)
        .role(UserRole.USER) // TODO set role as part of user form
        .authorities(UserRole.USER.getDefaultAuthorites())
        .enabled(true)
        .build();
  }
}