package com.zilch.payments.security.auth.exceptions;

import java.io.Serial;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.UNAUTHORIZED, reason = "Invalid Jwt Token")
public class InvalidJwtTokenException extends RuntimeException {

  @Serial private static final long serialVersionUID = -7016125053385246533L;
}
