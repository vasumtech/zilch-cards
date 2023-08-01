package com.zilch.payments.cards.components;

import java.time.LocalDate;

public interface CardInputDatesConverter {

  LocalDate parseValidFromDate(String validFromDateString);

  LocalDate parseValidUntilDate(String validUntilDateString);
}
