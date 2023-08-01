package com.zilch.payments.security.auth.services;

import com.zilch.payments.models.UserSigninRequest;

public interface UserAuthenticationService {

  String signin(UserSigninRequest authenticationRequest);
}
