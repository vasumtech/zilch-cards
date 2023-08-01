package com.zilch.payments.cards.exceptions;

import java.io.Serial;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.CONFLICT)
public class CardAlreadyAddedException extends RuntimeException {

  @Serial private static final long serialVersionUID = 6138445481991870226L;

  public CardAlreadyAddedException(final String userName) {
    super(String.format("Card already added for the user : %s", userName));
  }
}
