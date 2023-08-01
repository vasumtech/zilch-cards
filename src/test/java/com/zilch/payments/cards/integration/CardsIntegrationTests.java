package com.zilch.payments.cards.integration;

import static com.zilch.payments.cards.CardTestFixture.*;
import static io.restassured.RestAssured.given;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.CONFLICT;

import com.zilch.payments.cards.repositories.CardRepository;
import com.zilch.payments.models.AddCardRequest;
import com.zilch.payments.models.AddCardResponse;
import com.zilch.payments.security.auth.repositories.ZilchUserRepository;
import io.restassured.RestAssured;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class CardsIntegrationTests {

  @Autowired private CardRepository cardRepository;

  @Autowired private ZilchUserRepository zilchUserRepository;

  @LocalServerPort private int port;

  @BeforeEach
  void beforeEach() {
    RestAssured.port = port;
    cardRepository.deleteAll();
  }

  @DisplayName("Test save cards with no Authorization header")
  @Test
  void givenNoAuthorizationHeader_whenSaveCard_thenReturnAuthFailed() {
    AddCardRequest addCardRequest = createTestAddCardRequest();
    given()
        .contentType("application/json")
        .body(addCardRequest)
        .when()
        .post("/v1/cards")
        .then()
        .statusCode(HttpStatus.FORBIDDEN.value());
  }

  @DisplayName("Test save cards with an expired JWT token")
  @Test
  void givenExpiredToken_whenSaveCard_thenReturnAuthFailed() {
    AddCardRequest addCardRequest = createTestAddCardRequest();
    given()
        .header("Authorization", "Bearer " + EXPIRED_AUTHORIZATION_TOKEN)
        .contentType("application/json")
        .body(addCardRequest)
        .when()
        .post("/v1/cards")
        .then()
        .statusCode(HttpStatus.UNAUTHORIZED.value());
  }

  @DisplayName("Test save cards with an invalid JWT token")
  @Test
  void givenInvalidToken_whenSaveCard_thenReturnAuthFailed() {
    AddCardRequest addCardRequest = createTestAddCardRequest();
    given()
        .header("Authorization", "Bearer " + INVALID_AUTHORIZATION_TOKEN)
        .contentType("application/json")
        .body(addCardRequest)
        .when()
        .post("/v1/cards")
        .then()
        .statusCode(HttpStatus.UNAUTHORIZED.value());
  }

  @DisplayName("Test saveCard with null cardCompanyId")
  @Test
  void givenNullCardCompanyId_whenSaveCard_thenReturnBadRequest() {
    AddCardRequest addCardRequest = createTestAddCardRequest();
    addCardRequest.setCompanyId(null);
    given()
        .header("Authorization", "Bearer " + VALID_AUTHORIZATION_TOKEN_WITH_FUTURE_EXP_DATE)
        .contentType("application/json")
        .body(addCardRequest)
        .when()
        .post("/v1/cards")
        .then()
        .assertThat()
        .body(
            "errors[0].fieldName",
            is(equalTo("companyId")),
            "errors[0].message",
            is(equalTo("Field companyId must not be null")))
        .statusCode(HttpStatus.BAD_REQUEST.value())
        .extract()
        .response();
  }

  @DisplayName("Test saveCard with invalid cardIssuerId")
  @Test
  void givenInvalidForCardCompanyId_whenSaveCard_thenReturnBadRequest() {
    AddCardRequest addCardRequest = createTestAddCardRequest();
    addCardRequest.setCompanyId(-1);
    given()
        .header("Authorization", "Bearer " + VALID_AUTHORIZATION_TOKEN_WITH_FUTURE_EXP_DATE)
        .contentType("application/json")
        .body(addCardRequest)
        .when()
        .post("/v1/cards")
        .then()
        .assertThat()
        .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
        .extract()
        .response();
  }

  @DisplayName("Test saveCard with null CardNumber")
  @ParameterizedTest
  @MethodSource("provideInvalidCardNumbers")
  void givenInvalidCardNumbers_whenSaveCard_thenReturnBadRequest(
      String cardNumber, String expectedResponse) {
    AddCardRequest addCardRequest = createTestAddCardRequest();
    addCardRequest.setCardNumber(cardNumber);
    given()
        .header("Authorization", "Bearer " + VALID_AUTHORIZATION_TOKEN_WITH_FUTURE_EXP_DATE)
        .contentType("application/json")
        .body(addCardRequest)
        .when()
        .post("/v1/cards")
        .then()
        .assertThat()
        .body(
            "errors[0].fieldName",
            is(equalTo("cardNumber")),
            "errors[0].message",
            is(equalTo(expectedResponse)))
        .statusCode(HttpStatus.BAD_REQUEST.value())
        .extract()
        .response();
  }

  @DisplayName("Test saveCard with invalid titles")
  @ParameterizedTest
  @MethodSource("provideTitleArguments")
  void givenInvalidTitles_whenSaveCard_thenReturnBadRequest(String title, String expectedResponse) {
    AddCardRequest addCardRequest = createTestAddCardRequest();
    addCardRequest.setTitle(title);
    given()
        .header("Authorization", "Bearer " + VALID_AUTHORIZATION_TOKEN_WITH_FUTURE_EXP_DATE)
        .contentType("application/json")
        .body(addCardRequest)
        .when()
        .post("/v1/cards")
        .then()
        .assertThat()
        .body(
            "errors[0].fieldName",
            is(equalTo("title")),
            "errors[0].message",
            is(equalTo(expectedResponse)))
        .statusCode(HttpStatus.BAD_REQUEST.value())
        .extract()
        .response();
  }

  @DisplayName("Test saveCard with invalid nameOnCard")
  @ParameterizedTest
  @MethodSource("provideInvalidNameOnCard")
  void givenInvalidNameOnCard_whenSaveCard_thenReturnBadRequest(
      String nameOnCard, String expectedResponse) {
    AddCardRequest addCardRequest = createTestAddCardRequest();
    addCardRequest.setNameOnCard(nameOnCard);
    given()
        .header("Authorization", "Bearer " + VALID_AUTHORIZATION_TOKEN_WITH_FUTURE_EXP_DATE)
        .contentType("application/json")
        .body(addCardRequest)
        .when()
        .post("/v1/cards")
        .then()
        .assertThat()
        .body(
            "errors[0].fieldName",
            is(equalTo("nameOnCard")),
            "errors[0].message",
            is(equalTo(expectedResponse)))
        .statusCode(HttpStatus.BAD_REQUEST.value())
        .extract()
        .response();
  }

  @DisplayName("Test saveCard with incorrect validFrom Dates")
  @ParameterizedTest
  @MethodSource("provideIncorrectValidFromDates")
  void givenIncorrectValidFromDates_whenSaveCard_thenReturnBadRequest(
      String validFrom, String expectedResponse) {
    AddCardRequest addCardRequest = createTestAddCardRequest();
    addCardRequest.setValidFrom(validFrom);
    given()
        .header("Authorization", "Bearer " + VALID_AUTHORIZATION_TOKEN_WITH_FUTURE_EXP_DATE)
        .contentType("application/json")
        .body(addCardRequest)
        .when()
        .post("/v1/cards")
        .then()
        .assertThat()
        .body(
            "errors[0].fieldName",
            is(equalTo("validFrom")),
            "errors[0].message",
            is(equalTo(expectedResponse)))
        .statusCode(HttpStatus.BAD_REQUEST.value())
        .extract()
        .response();
  }

  @DisplayName("Test saveCard with future date of validFrom")
  @Test
  void givenFutureDateInValidFrom_whenSaveCard_thenReturnBadRequest() {
    String futureDate =
        YearMonth.now().plusMonths(1).format(DateTimeFormatter.ofPattern("MM/yyyy"));
    AddCardRequest addCardRequest = createTestAddCardRequest();
    addCardRequest.setValidFrom(futureDate);
    given()
        .header("Authorization", "Bearer " + VALID_AUTHORIZATION_TOKEN_WITH_FUTURE_EXP_DATE)
        .contentType("application/json")
        .body(addCardRequest)
        .when()
        .post("/v1/cards")
        .then()
        .assertThat()
        .body(
            "message",
            is(equalTo("Bad request : valid from date is a future date : " + futureDate)))
        .statusCode(BAD_REQUEST.value())
        .extract()
        .response();
  }

  @DisplayName("Test saveCard with invalid validUntil")
  @ParameterizedTest
  @MethodSource("provideIncorrectValidUntilDates")
  void givenNullValidUntil_whenSaveCard_thenReturnBadRequest(
      String validUntil, String expectedResponse) {
    AddCardRequest addCardRequest = createTestAddCardRequest();
    addCardRequest.setValidUntil(validUntil);
    given()
        .header("Authorization", "Bearer " + VALID_AUTHORIZATION_TOKEN_WITH_FUTURE_EXP_DATE)
        .contentType("application/json")
        .body(addCardRequest)
        .when()
        .post("/v1/cards")
        .then()
        .assertThat()
        .body(
            "errors[0].fieldName",
            is(equalTo("validUntil")),
            "errors[0].message",
            is(equalTo(expectedResponse)))
        .statusCode(HttpStatus.BAD_REQUEST.value())
        .extract()
        .response();
  }

  @DisplayName("Test saveCard with expired date for validUntil")
  @Test
  void givenExpiredDateForValidUntil_whenSaveCard_thenReturnBadRequest() {
    String expiredDate =
        YearMonth.now().minusMonths(1).format(DateTimeFormatter.ofPattern("MM/yyyy"));
    AddCardRequest addCardRequest = createTestAddCardRequest();
    addCardRequest.setValidUntil(expiredDate);
    given()
        .header("Authorization", "Bearer " + VALID_AUTHORIZATION_TOKEN_WITH_FUTURE_EXP_DATE)
        .contentType("application/json")
        .body(addCardRequest)
        .when()
        .post("/v1/cards")
        .then()
        .assertThat()
        .body(
            "message",
            is(equalTo("Bad request : valid until date already expired : " + expiredDate)))
        .statusCode(BAD_REQUEST.value())
        .extract()
        .response();
  }

  @DisplayName("Test saveCard with invalid pins")
  @ParameterizedTest
  @MethodSource("provideInvalidPinNumbers")
  void givenInvalidPins_whenSaveCard_thenReturnBadRequest(String pin, String expectedResponse) {
    AddCardRequest addCardRequest = createTestAddCardRequest();
    addCardRequest.setPin(pin);
    given()
        .header("Authorization", "Bearer " + VALID_AUTHORIZATION_TOKEN_WITH_FUTURE_EXP_DATE)
        .contentType("application/json")
        .body(addCardRequest)
        .when()
        .post("/v1/cards")
        .then()
        .assertThat()
        .body(
            "errors[0].fieldName",
            is(equalTo("pin")),
            "errors[0].message",
            is(equalTo(expectedResponse)))
        .statusCode(HttpStatus.BAD_REQUEST.value())
        .extract()
        .response();
  }

  @DisplayName("Save card details for a valid token and input request")
  @Test
  void givenValidAddCardRequest_whenSaveCard_thenReturnAddCardResponse() {
    AddCardRequest addCardRequest = createTestAddCardRequest();
    AddCardResponse actualResponse =
        given()
            .header("Authorization", "Bearer " + VALID_AUTHORIZATION_TOKEN_WITH_FUTURE_EXP_DATE)
            .contentType("application/json")
            .body(addCardRequest)
            .when()
            .post("/v1/cards")
            .then()
            .statusCode(HttpStatus.CREATED.value())
            .extract()
            .body()
            .as(AddCardResponse.class);
    AddCardResponse expectedResponse = createTestAddCardResponse();

    assertThat(actualResponse, is(equalTo(expectedResponse)));
  }

  @DisplayName("Don't Save duplicate card requests")
  @Test
  void givenDuplicateAddCardRequest_whenSaveCard_thenReturnAddCardResponse() {
    AddCardRequest addCardRequest = createTestAddCardRequest();
    given()
        .header("Authorization", "Bearer " + VALID_AUTHORIZATION_TOKEN_WITH_FUTURE_EXP_DATE)
        .contentType("application/json")
        .body(addCardRequest)
        .when()
        .post("/v1/cards");
    given()
        .header("Authorization", "Bearer " + VALID_AUTHORIZATION_TOKEN_WITH_FUTURE_EXP_DATE)
        .contentType("application/json")
        .body(addCardRequest)
        .when()
        .post("/v1/cards")
        .then()
        .contentType("application/json")
        .body("message", is(equalTo("Card already added for the user : zilchtestuser")))
        .statusCode(CONFLICT.value())
        .extract()
        .response();
  }

  private static Stream<Arguments> provideInvalidNameOnCard() {
    return Stream.of(
        Arguments.of(null, "Field nameOnCard must not be null"),
        Arguments.of("M", "Field nameOnCard length should be between 4 and 50 characters"),
        Arguments.of(
            "P YRTEERTQEWERTUISFGDTEERTECEROPUYFGTREYISSGKYWDFJFFFGG",
            "Field nameOnCard length should be between 4 and 50 characters"));
  }

  private static Stream<Arguments> provideIncorrectValidFromDates() {
    return Stream.of(
        Arguments.of(null, "Field validFrom must not be null"),
        Arguments.of("092022", "Field validFrom is incorrect"),
        Arguments.of("13/2022", "Field validFrom is incorrect"),
        Arguments.of("10/20", "Field validFrom is incorrect"));
  }

  private static Stream<Arguments> provideIncorrectValidUntilDates() {
    return Stream.of(
        Arguments.of("05/20", "Field validUntil is incorrect"),
        Arguments.of("13/2025", "Field validUntil is incorrect"),
        Arguments.of("092025", "Field validUntil is incorrect"),
        Arguments.of(null, "Field validUntil must not be null"));
  }

  private static Stream<Arguments> provideInvalidPinNumbers() {
    return Stream.of(
        Arguments.of(null, "Field pin must not be null"),
        Arguments.of("2", "Field pin should be of length 3 digits"),
        Arguments.of("9622", "Field pin should be of length 3 digits"));
  }

  private static Stream<Arguments> provideInvalidCardNumbers() {
    return Stream.of(
        Arguments.of(null, "Field cardNumber must not be null"),
        Arguments.of("4234 5678", "Field cardNumber is incorrect"),
        Arguments.of("4234567847854384", "Field cardNumber is incorrect"),
        Arguments.of("4678 5978 6431 9844 8", "Field cardNumber is incorrect"));
  }

  private static Stream<Arguments> provideTitleArguments() {
    return Stream.of(
        Arguments.of(null, "Field title must not be null"),
        Arguments.of("M", "Field title length should be between 2 and 5 characters"),
        Arguments.of("MrsMiss", "Field title length should be between 2 and 5 characters"));
  }
}
