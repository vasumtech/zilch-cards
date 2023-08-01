package com.zilch.payments.cards.components;

import com.zilch.payments.cards.exceptions.BadRequest;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import org.springframework.stereotype.Component;

@Component
public class CardInputDatesConverterImpl implements CardInputDatesConverter {

  private static final String MONTH_YEAR_FORMAT = "MM/yyyy";

  private static final DateTimeFormatter MONTH_YEAR_FORMATTER =
      DateTimeFormatter.ofPattern(MONTH_YEAR_FORMAT);

  @Override
  public LocalDate parseValidFromDate(String validFromDateString) {
    try {
      YearMonth validFromYearMonth = YearMonth.parse(validFromDateString, MONTH_YEAR_FORMATTER);
      if (YearMonth.now(ZoneId.systemDefault()).isBefore(validFromYearMonth)) {
        throw new BadRequest(
            String.format("valid from date is a future date : %s", validFromDateString));
      } else {
        return validFromYearMonth.atDay(1);
      }
    } catch (DateTimeException dateTimeException) {
      throw new BadRequest(
          String.format("valid from date is not correct : %s", validFromDateString));
    }
  }

  @Override
  public LocalDate parseValidUntilDate(String validUntilDateString) {
    try {
      YearMonth cardExpiryYearMonth = YearMonth.parse(validUntilDateString, MONTH_YEAR_FORMATTER);
      if (YearMonth.now(ZoneId.systemDefault()).isAfter(cardExpiryYearMonth)) {
        throw new BadRequest(
            String.format("valid until date already expired : %s", validUntilDateString));
      } else {
        return cardExpiryYearMonth.atDay(cardExpiryYearMonth.lengthOfMonth());
      }
    } catch (DateTimeException dateTimeException) {
      throw new BadRequest(
          String.format("valid until date is not correct : %s", validUntilDateString));
    }
  }
}
