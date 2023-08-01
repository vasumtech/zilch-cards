package com.zilch.payments.security.auth.config;

import static com.zilch.payments.TestFixture.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

import com.zilch.payments.security.auth.dto.ZilchUserDetails;
import com.zilch.payments.security.auth.exceptions.InvalidJwtTokenException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetailsService;

@ExtendWith(MockitoExtension.class)
class JwtTokenFilterTest {

  private static final String INVALID_TOKEN_HEADER = "Invalid token header";

  @Mock private JwtTokenUtil jwtTokenUtil;

  @Mock private HttpServletRequest request;

  @Mock private HttpServletResponse response;

  @Mock private FilterChain filterChain;

  @Mock private UserDetailsService userDetailsService;

  private JwtTokenFilter jwtTokenFilter;

  @BeforeEach
  void setUp() {
    this.jwtTokenFilter = new JwtTokenFilter(jwtTokenUtil, userDetailsService);
  }

  @DisplayName("test with no authorization header token")
  @Test
  void givenNoTokenInRequest_whenCallJwtFilter_thenThrowServletException()
      throws ServletException, IOException {
    when(jwtTokenUtil.resolveToken(request)).thenReturn(Optional.empty());
    doThrow(new ServletException(INVALID_TOKEN_HEADER))
        .when(filterChain)
        .doFilter(request, response);
    ServletException actualServletException =
        assertThrows(
            ServletException.class,
            () -> jwtTokenFilter.doFilterInternal(request, response, filterChain));
    verify(filterChain).doFilter(request, response);
    assertThat(actualServletException.getMessage(), is(equalTo(INVALID_TOKEN_HEADER)));
  }

  @DisplayName("test with invalid jwt token")
  @Test
  void givenInvalidTokenInRequest_whenCallJwtFilter_thenThrowServletException()
      throws ServletException, IOException {
    when(jwtTokenUtil.resolveToken(request)).thenReturn(Optional.of(INVALID_AUTHORIZATION_TOKEN));
    when(jwtTokenUtil.isValidToken(INVALID_AUTHORIZATION_TOKEN))
        .thenThrow(new InvalidJwtTokenException());
    jwtTokenFilter.doFilterInternal(request, response, filterChain);
    verifyNoInteractions(filterChain);
  }

  @DisplayName("test with expired jwt token")
  @Test
  void givenExpiredTokenInRequest_whenCallJwtFilter_thenThrowServletException()
      throws ServletException, IOException {
    when(jwtTokenUtil.resolveToken(request)).thenReturn(Optional.of(EXPIRED_AUTHORIZATION_TOKEN));
    when(jwtTokenUtil.isValidToken(EXPIRED_AUTHORIZATION_TOKEN))
        .thenThrow(new InvalidJwtTokenException());
    jwtTokenFilter.doFilterInternal(request, response, filterChain);
    verifyNoInteractions(filterChain);
  }

  @DisplayName("test with valid jwt token")
  @Test
  void givenValidTokenInRequest_whenCallJwtFilter_thenThrowServletException()
      throws ServletException, IOException {
    when(jwtTokenUtil.resolveToken(request))
        .thenReturn(Optional.of(VALID_AUTHORIZATION_TOKEN_WITH_FUTURE_EXP_DATE));
    when(jwtTokenUtil.isValidToken(VALID_AUTHORIZATION_TOKEN_WITH_FUTURE_EXP_DATE))
        .thenReturn(true);
    when(jwtTokenUtil.getUsernameFromToken(VALID_AUTHORIZATION_TOKEN_WITH_FUTURE_EXP_DATE))
        .thenReturn(ZILCH_TEST_USER_NAME);
    Collection<? extends GrantedAuthority> authorities =
        Set.of(new SimpleGrantedAuthority(ROLE_USER));
    ZilchUserDetails zilchUserDetails =
        new ZilchUserDetails(ZILCH_TEST_USER_ID, ZILCH_TEST_USER_NAME, "", authorities);
    when(userDetailsService.loadUserByUsername(ZILCH_TEST_USER_NAME)).thenReturn(zilchUserDetails);

    jwtTokenFilter.doFilterInternal(request, response, filterChain);

    verify(filterChain).doFilter(request, response);
  }
}
