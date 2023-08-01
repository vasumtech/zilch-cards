package com.zilch.payments.security.auth.exceptions;

import java.io.Serial;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.UNAUTHORIZED, reason = "User authentication failed")
public class UserAuthenticationException extends AuthenticationException {

  @Serial private static final long serialVersionUID = -7016125053385246533L;

  public UserAuthenticationException(final String userid) {
    super(String.format("User authentication failed for : %s", userid));
  }
}
