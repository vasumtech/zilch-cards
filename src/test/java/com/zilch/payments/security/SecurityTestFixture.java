package com.zilch.payments.security;

import com.zilch.payments.TestFixture;
import com.zilch.payments.models.UserSigninRequest;

public interface SecurityTestFixture extends TestFixture {

  static UserSigninRequest createUserSigninRequest() {
    return new UserSigninRequest(ZILCH_TEST_USER_NAME, ZILCH_TEST_USER_PASSWORD);
  }
}
