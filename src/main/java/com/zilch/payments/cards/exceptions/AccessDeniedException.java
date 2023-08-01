package com.zilch.payments.cards.exceptions;

import java.io.Serial;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.FORBIDDEN)
public class AccessDeniedException extends RuntimeException {

  @Serial private static final long serialVersionUID = -2784807974145879405L;

  public AccessDeniedException() {
    super("Access denied");
  }
}
