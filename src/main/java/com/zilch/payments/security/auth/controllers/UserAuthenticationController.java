package com.zilch.payments.security.auth.controllers;

import com.zilch.payments.api.UserAuthenticationApiDelegate;
import com.zilch.payments.models.UserSigninRequest;
import com.zilch.payments.models.UserSigninResponse;
import com.zilch.payments.security.auth.services.UserAuthenticationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Slf4j
public class UserAuthenticationController implements UserAuthenticationApiDelegate {

  private final UserAuthenticationService userAuthenticationService;

  @Override
  public ResponseEntity<UserSigninResponse> login(
      @Valid @RequestBody UserSigninRequest authenticationRequest) {
    log.debug(" : user validated successfully : {}", authenticationRequest.getUserName());
    String token = userAuthenticationService.signin(authenticationRequest);
    return new ResponseEntity<>(new UserSigninResponse(token), HttpStatus.CREATED);
  }
}
