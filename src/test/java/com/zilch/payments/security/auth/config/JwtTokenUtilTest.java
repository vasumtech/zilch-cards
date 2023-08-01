package com.zilch.payments.security.auth.config;

import static com.zilch.payments.TestFixture.*;
import static io.jsonwebtoken.SignatureAlgorithm.HS256;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.zilch.payments.security.auth.exceptions.InvalidJwtTokenException;
import com.zilch.payments.security.auth.exceptions.UserAuthenticationException;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import org.apache.http.HttpHeaders;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

@ExtendWith(MockitoExtension.class)
class JwtTokenUtilTest {

  private static final long TOKEN_VALIDITY_IN_MILLISECONDS = 600000;

  @Mock private HttpServletRequest request;

  private JwtTokenUtil jwtTokenUtil;

  @BeforeEach
  void init() {
    this.jwtTokenUtil = new JwtTokenUtil(SECRET_KEY, TOKEN_VALIDITY_IN_MILLISECONDS);
  }

  @DisplayName("test generate token with Null Name In Authentication")
  @NullAndEmptySource
  @ParameterizedTest
  void givenNullOrEmptyNameInAuthentication_whenGenerateToken_thenInvalidAuthenticationException(
      String userName) {
    Set<SimpleGrantedAuthority> authorities = Set.of(new SimpleGrantedAuthority(ROLE_USER));
    assertThrows(
        UserAuthenticationException.class, () -> jwtTokenUtil.generateToken(authorities, userName));
  }

  @DisplayName("test generate token with Null Authorities In Authentication")
  @Test
  void givenNullAuthorities_whenGenerateToken_thenInvalidAuthenticationException() {
    assertThrows(
        UserAuthenticationException.class,
        () -> jwtTokenUtil.generateToken(null, ZILCH_TEST_USER_NAME));
  }

  @DisplayName("test generate token with Empty Authorities In Authentication")
  @Test
  void givenEmptyAuthorities_whenGenerateToken_thenInvalidAuthenticationException() {
    assertThrows(
        UserAuthenticationException.class,
        () -> jwtTokenUtil.generateToken(Collections.emptySet(), ZILCH_TEST_USER_NAME));
  }

  @DisplayName("test generate token with valid Authentication")
  @Test
  void givenValidAuthentication_whenGenerateToken_thenGetToken() {
    Set<GrantedAuthority> authorities = Set.of(new SimpleGrantedAuthority(ROLE_USER));
    JwtBuilder jwtBuilder = Jwts.builder();
    var mockedBuilder = mock(jwtBuilder.getClass(), RETURNS_DEEP_STUBS);
    try (var mockedStatic = mockStatic(Jwts.class)) {
      mockedStatic.when(Jwts::builder).thenReturn(mockedBuilder);
      when(Jwts.builder()
              .claim(USER_ROLES, authorities)
              .setSubject(ZILCH_TEST_USER_NAME)
              .setIssuedAt(any())
              .setExpiration(any())
              .signWith(any(), eq(HS256))
              .compact())
          .thenReturn(VALID_AUTHORIZATION_TOKEN_WITH_FUTURE_EXP_DATE);

      String actualToken = jwtTokenUtil.generateToken(authorities, ZILCH_TEST_USER_NAME);

      assertThat(actualToken, is(equalTo(VALID_AUTHORIZATION_TOKEN_WITH_FUTURE_EXP_DATE)));
    }
  }

  @DisplayName("test resolve token with null authorizationHeader")
  @ParameterizedTest
  @NullAndEmptySource
  void givenNullAuthorizationHeader_whenResolveToken_thenReturnEmpty(String tokenHeader) {
    when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn(tokenHeader);
    boolean actualValue = jwtTokenUtil.resolveToken(request).isEmpty();
    assertTrue(actualValue);
  }

  @DisplayName("test resolve token with authorizationHeader starting with Bearer without space")
  @ParameterizedTest
  @ValueSource(strings = {"Bearer", "NOTBearer "})
  void givenBearerWithoutSpaceInAuthorizationHeader_whenResolveToken_thenReturnEmpty(
      String tokenHeader) {
    when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn(tokenHeader);
    boolean actualValue = jwtTokenUtil.resolveToken(request).isEmpty();
    assertTrue(actualValue);
  }

  @DisplayName("test resolve token with valid authorizationHeader")
  @Test
  void givenValidBearerAuthorizationHeader_whenResolveToken_thenReturnEmpty() {
    when(request.getHeader(HttpHeaders.AUTHORIZATION))
        .thenReturn("Bearer " + VALID_AUTHORIZATION_TOKEN_WITH_FUTURE_EXP_DATE);
    Optional<String> token = jwtTokenUtil.resolveToken(request);
    assertTrue(token.isPresent());
    assertThat(token.get(), is(equalTo(VALID_AUTHORIZATION_TOKEN_WITH_FUTURE_EXP_DATE)));
  }

  @DisplayName("test isValidToken with invalid or expired jwt token")
  @ParameterizedTest
  @ValueSource(strings = {INVALID_AUTHORIZATION_TOKEN, EXPIRED_AUTHORIZATION_TOKEN})
  void givenInvalidExpiredJwtTokens_whenIsValidToken_thenThrowInvalidJwtTokenException(
      String token) {
    assertThrows(InvalidJwtTokenException.class, () -> jwtTokenUtil.isValidToken(token));
  }

  @DisplayName("test isValidToken with valid jwt token")
  @Test
  void givenValidJwtToken_whenIsValidToken_thenThrowInvalidJwtTokenException() {
    boolean isValidToken =
        jwtTokenUtil.isValidToken(VALID_AUTHORIZATION_TOKEN_WITH_FUTURE_EXP_DATE);
    assertTrue(isValidToken);
  }

  @DisplayName("test get Username with invalid or expired jwt tokens")
  @ParameterizedTest
  @ValueSource(strings = {INVALID_AUTHORIZATION_TOKEN, EXPIRED_AUTHORIZATION_TOKEN})
  void givenInvalidJwtToken_whenGetUsernameFromToken_thenThrowInvalidJwtTokenException(
      String token) {
    assertThrows(InvalidJwtTokenException.class, () -> jwtTokenUtil.getUsernameFromToken(token));
  }

  @DisplayName("test get Username with valid jwt token")
  @Test
  void givenValidJwtToken_whenGetUsernameFromToken_thenSuccess() {
    String actualUserName =
        jwtTokenUtil.getUsernameFromToken(VALID_AUTHORIZATION_TOKEN_WITH_FUTURE_EXP_DATE);
    assertThat(actualUserName, is(equalTo(ZILCH_TEST_USER_NAME)));
  }
}
