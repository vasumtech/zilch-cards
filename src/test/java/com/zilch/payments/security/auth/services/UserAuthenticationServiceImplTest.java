package com.zilch.payments.security.auth.services;

import static com.zilch.payments.TestFixture.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

import com.zilch.payments.cards.exceptions.AccessDeniedException;
import com.zilch.payments.models.UserSigninRequest;
import com.zilch.payments.security.auth.config.JwtTokenUtil;
import com.zilch.payments.security.auth.exceptions.UserAuthenticationException;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

@ExtendWith(MockitoExtension.class)
class UserAuthenticationServiceImplTest {

  @Mock private AuthenticationManager authenticationManager;

  @Mock private JwtTokenUtil jwtTokenUtil;

  @InjectMocks private UserAuthenticationServiceImpl userAuthenticationServiceImpl;

  @DisplayName("Signin with null request")
  @Test
  void givenNullUserSigninRequest_whenSignin_thenAccessDenied() {
    AccessDeniedException accessDeniedException =
        assertThrows(AccessDeniedException.class, () -> userAuthenticationServiceImpl.signin(null));

    assertThat(accessDeniedException.getMessage(), is(equalTo("Access denied")));

    verify(authenticationManager, never()).authenticate(any());
    verify(jwtTokenUtil, never()).generateToken(any(), any());
  }

  @DisplayName("Signin with invalid credentials")
  @Test
  void givenInvalidCreentialsSigninRequest_whenSignin_thenUserAuthenticationException() {
    UserSigninRequest userSigninRequest =
        new UserSigninRequest(ZILCH_TEST_USER_NAME, ZILCH_TEST_USER_PASSWORD);
    UsernamePasswordAuthenticationToken requestToken =
        new UsernamePasswordAuthenticationToken(ZILCH_TEST_USER_NAME, ZILCH_TEST_USER_PASSWORD);
    when(authenticationManager.authenticate(requestToken))
        .thenThrow(
            new UserAuthenticationException(
                "User authentication failed for : " + ZILCH_TEST_USER_NAME));

    UserAuthenticationException userAuthenticationException =
        assertThrows(
            UserAuthenticationException.class,
            () -> userAuthenticationServiceImpl.signin(userSigninRequest));

    assertThat(
        userAuthenticationException.getMessage(),
        is(equalTo("User authentication failed for : " + ZILCH_TEST_USER_NAME)));
    verify(authenticationManager).authenticate(requestToken);
    verify(jwtTokenUtil, never()).generateToken(any(), any());
  }

  @DisplayName("Signin with valid request")
  @Test
  void givenValidUserSigninRequest_whenSignin_thenReturnToken() {
    UserSigninRequest userSigninRequest =
        new UserSigninRequest(ZILCH_TEST_USER_NAME, ZILCH_TEST_USER_PASSWORD);
    UsernamePasswordAuthenticationToken requestToken =
        new UsernamePasswordAuthenticationToken(ZILCH_TEST_USER_NAME, ZILCH_TEST_USER_PASSWORD);
    UsernamePasswordAuthenticationToken authenticatedToken =
        new UsernamePasswordAuthenticationToken(
            ZILCH_TEST_USER_NAME, "", Set.of(new SimpleGrantedAuthority(ROLE_USER)));
    when(authenticationManager.authenticate(requestToken)).thenReturn(authenticatedToken);
    when(jwtTokenUtil.generateToken(authenticatedToken.getAuthorities(), ZILCH_TEST_USER_NAME))
        .thenReturn(VALID_AUTHORIZATION_TOKEN_WITH_FUTURE_EXP_DATE);

    String actualToken = userAuthenticationServiceImpl.signin(userSigninRequest);

    assertThat(actualToken, is(equalTo(VALID_AUTHORIZATION_TOKEN_WITH_FUTURE_EXP_DATE)));

    verify(authenticationManager).authenticate(requestToken);
    verify(jwtTokenUtil).generateToken(authenticatedToken.getAuthorities(), ZILCH_TEST_USER_NAME);
  }
}
