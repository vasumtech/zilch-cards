package com.zilch.payments.security.auth.integration;

import static com.zilch.payments.TestFixture.ZILCH_TEST_USER_NAME;
import static com.zilch.payments.TestFixture.ZILCH_TEST_USER_PASSWORD;
import static com.zilch.payments.security.SecurityTestFixture.createUserSigninRequest;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

import com.zilch.payments.models.UserSigninRequest;
import com.zilch.payments.models.UserSigninResponse;
import com.zilch.payments.security.auth.repositories.ZilchUserRepository;
import io.restassured.RestAssured;
import io.restassured.filter.log.LogDetail;
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
class SecurityIntegrationTests {

  @LocalServerPort private int port;

  @Autowired private ZilchUserRepository zilchUserRepository;

  @BeforeEach
  public void beforeEach() {
    RestAssured.port = port;
  }

  @DisplayName("Test SignIn with invalid userNames")
  @ParameterizedTest
  @MethodSource("provideIncorrectUserNames")
  void givenNullUserName_whenSignIn_thenBadRequest(String userName, String expectedMessage) {
    UserSigninRequest userSigninRequest = new UserSigninRequest(userName, ZILCH_TEST_USER_PASSWORD);
    given()
        .contentType("application/json")
        .body(userSigninRequest)
        .when()
        .post("/v1/login")
        .then()
        .assertThat()
        .body(
            "errors[0].fieldName",
            is(equalTo("userName")),
            "errors[0].message",
            is(equalTo(expectedMessage)))
        .statusCode(HttpStatus.BAD_REQUEST.value())
        .extract()
        .response();
  }

  @DisplayName("Test SignIn with null password")
  @Test
  void givenNullPassword_whenSignIn_thenBadRequest() {
    UserSigninRequest userSigninRequest = new UserSigninRequest(ZILCH_TEST_USER_NAME, null);
    given()
        .contentType("application/json")
        .body(userSigninRequest)
        .when()
        .post("/v1/login")
        .then()
        .assertThat()
        .body(
            "errors[0].fieldName",
            is(equalTo("password")),
            "errors[0].message",
            is(equalTo("Field password must not be null")))
        .statusCode(HttpStatus.BAD_REQUEST.value())
        .extract()
        .response();
  }

  @DisplayName("Test SignIn with Invalid password")
  @Test
  void givenInvalidPassword_whenSignIn_thenAuthenticationFail() {
    UserSigninRequest userSigninRequest = new UserSigninRequest(ZILCH_TEST_USER_NAME, "Zilch@8876");
    given()
        .contentType("application/json")
        .body(userSigninRequest)
        .when()
        .post("/v1/login")
        .then()
        .log()
        .ifValidationFails(LogDetail.ALL, true)
        .assertThat()
        .body(
            "error",
            is(equalTo("Unauthorized")),
            "message",
            is(equalTo("User authentication failed")))
        .statusCode(HttpStatus.UNAUTHORIZED.value())
        .extract()
        .response();
  }

  @DisplayName("Test SignIn with valid details")
  @Test
  void givenValidUserNamePassword_whenSignIn_thenReturnSuccess() {
    UserSigninRequest userSigninRequest = createUserSigninRequest();
    UserSigninResponse userSigninResponse =
        given()
            .contentType("application/json")
            .body(userSigninRequest)
            .when()
            .post("/v1/login")
            .then()
            .statusCode(HttpStatus.CREATED.value())
            .extract()
            .body()
            .as(UserSigninResponse.class);
  }

  private static Stream<Arguments> provideIncorrectUserNames() {
    return Stream.of(
        Arguments.of(null, "Field userName must not be null"),
        Arguments.of("user", "Field userName length should be between 8 and 50 characters"),
        Arguments.of(
            "zilchuserzilchuserzilchuserzilchuserzilchuserzilchuserzilchuser",
            "Field userName length should be between 8 and 50 characters"));
  }
}
