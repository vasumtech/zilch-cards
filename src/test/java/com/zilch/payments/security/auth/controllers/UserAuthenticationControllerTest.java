package com.zilch.payments.security.auth.controllers;

import static com.zilch.payments.TestFixture.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.zilch.payments.models.UserSigninRequest;
import com.zilch.payments.models.UserSigninResponse;
import com.zilch.payments.security.auth.services.UserAuthenticationService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@ExtendWith(MockitoExtension.class)
class UserAuthenticationControllerTest {

  @Mock private UserAuthenticationService userAuthenticationService;

  @InjectMocks private UserAuthenticationController userAuthenticationController;

  @DisplayName("signin request for a valid user credentials")
  @Test
  void givenValidUserSigninRequest_whenLogin_thenReturnSuccessToken() {
    UserSigninRequest authenticationRequest =
        new UserSigninRequest(ZILCH_TEST_USER_NAME, ZILCH_TEST_USER_PASSWORD);
    when(userAuthenticationService.signin(authenticationRequest))
        .thenReturn(VALID_AUTHORIZATION_TOKEN_WITH_FUTURE_EXP_DATE);
    UserSigninResponse userSigninResponse =
        new UserSigninResponse(VALID_AUTHORIZATION_TOKEN_WITH_FUTURE_EXP_DATE);

    ResponseEntity<UserSigninResponse> responseEntity =
        userAuthenticationController.login(authenticationRequest);

    verify(userAuthenticationService).signin(authenticationRequest);
    assertThat(responseEntity.getStatusCode().value(), is(equalTo(HttpStatus.CREATED.value())));
    assertThat(responseEntity.getBody(), equalTo(userSigninResponse));
  }

  // Note: Already input validations (@Valid) covered as part of IT tests
}
