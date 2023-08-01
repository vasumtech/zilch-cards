package com.zilch.payments.cards.exceptions;

import java.io.Serial;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.BAD_REQUEST)
public class BadRequest extends RuntimeException {

  @Serial private static final long serialVersionUID = -2423172401810950157L;

  public BadRequest(final String reason) {
    super(String.format("Bad request : %s", reason));
  }
}
