package com.zilch.payments.cards.components;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.zilch.payments.cards.exceptions.BadRequest;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class CardInputDatesConverterImplTest {

  private static final String INCORRECT_VALID_FROM_DATE = "15/22";
  private static final String INCORRECT_VALID_UNTIL_DATE = "15/22";
  private static final String FUTURE_VALID_FROM_DATE =
      YearMonth.now().plusMonths(1).format(DateTimeFormatter.ofPattern("MM/yyyy"));
  private static final String EXPIRED_VALID_UNTIL_DATE =
      YearMonth.now().minusMonths(1).format(DateTimeFormatter.ofPattern("MM/yyyy"));

  CardInputDatesConverter cardInputDatesConverter = new CardInputDatesConverterImpl();

  @DisplayName("Test parsing valid from date with incorrect date")
  @Test
  void givenIncorrectValidFromDate_whenParseValidFromDate_thenThrowBadRequestException() {
    BadRequest badRequest =
        assertThrows(
            BadRequest.class,
            () -> cardInputDatesConverter.parseValidFromDate(INCORRECT_VALID_FROM_DATE));
    assertThat(
        badRequest.getMessage(),
        is(equalTo("Bad request : valid from date is not correct : " + INCORRECT_VALID_FROM_DATE)));
  }

  @DisplayName("Test parsing valid from date with future date")
  @Test
  void givenFutureValidFromDate_whenParseValidFromDate_thenThrowBadRequestException() {
    BadRequest badRequest =
        assertThrows(
            BadRequest.class,
            () -> cardInputDatesConverter.parseValidFromDate(FUTURE_VALID_FROM_DATE));
    assertThat(
        badRequest.getMessage(),
        is(equalTo("Bad request : valid from date is a future date : " + FUTURE_VALID_FROM_DATE)));
  }

  @DisplayName("Test parsing valid until date with incorrect date")
  @Test
  void givenIncorrectValidUntilDate_whenParseValidUntilDate_thenThrowBadRequestException() {
    BadRequest badRequest =
        assertThrows(
            BadRequest.class,
            () -> cardInputDatesConverter.parseValidUntilDate(INCORRECT_VALID_UNTIL_DATE));
    assertThat(
        badRequest.getMessage(),
        is(
            equalTo(
                "Bad request : valid until date is not correct : " + INCORRECT_VALID_UNTIL_DATE)));
  }

  @DisplayName("Test parsing valid until date with expired date")
  @Test
  void givenExpiredValidUntilDate_whenParseValidUntilDate_thenThrowBadRequestException() {
    BadRequest badRequest =
        assertThrows(
            BadRequest.class,
            () -> cardInputDatesConverter.parseValidUntilDate(EXPIRED_VALID_UNTIL_DATE));
    assertThat(
        badRequest.getMessage(),
        is(
            equalTo(
                "Bad request : valid until date already expired : " + EXPIRED_VALID_UNTIL_DATE)));
  }

  @DisplayName("Test parsing valid from date with correct date")
  @Test
  void givenCorrectValidFromDate_whenParseValidFromDate_thenReturnLocalDate() {
    LocalDate actualLocalDate = cardInputDatesConverter.parseValidFromDate("06/2022");
    assertThat(actualLocalDate, is(equalTo(LocalDate.of(2022, 6, 1))));
  }

  @DisplayName("Test parsing valid until date with correct date")
  @Test
  void givenCorrectValidUntilDate_whenParseValidUntilDate_thenReturnLocalDate() {
    YearMonth futureYearMonth = YearMonth.now().plusMonths(1);
    String futureDate = futureYearMonth.format(DateTimeFormatter.ofPattern("MM/yyyy"));
    LocalDate actualLocalDate = cardInputDatesConverter.parseValidUntilDate(futureDate);
    assertThat(
        actualLocalDate,
        is(
            equalTo(
                LocalDate.of(
                    futureYearMonth.getYear(),
                    futureYearMonth.getMonth(),
                    futureYearMonth.lengthOfMonth()))));
  }
}
