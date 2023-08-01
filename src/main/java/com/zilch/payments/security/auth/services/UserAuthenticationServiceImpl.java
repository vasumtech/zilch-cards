package com.zilch.payments.security.auth.services;

import com.zilch.payments.cards.exceptions.AccessDeniedException;
import com.zilch.payments.models.UserSigninRequest;
import com.zilch.payments.security.auth.config.JwtTokenUtil;
import com.zilch.payments.security.auth.exceptions.UserAuthenticationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserAuthenticationServiceImpl implements UserAuthenticationService {

  private final AuthenticationManager authenticationManager;

  private final JwtTokenUtil jwtTokenUtil;

  @Override
  public String signin(UserSigninRequest authenticationRequest) {
    isValidRequest(authenticationRequest);
    String userName = authenticationRequest.getUserName();
    try {
      UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken =
          new UsernamePasswordAuthenticationToken(userName, authenticationRequest.getPassword());
      Authentication authentication =
          authenticationManager.authenticate(usernamePasswordAuthenticationToken);
      log.debug(" : user authenticated successfully : {}", authenticationRequest.getUserName());
      return jwtTokenUtil.generateToken(authentication.getAuthorities(), userName);
    } catch (AuthenticationException e) {
      throw new UserAuthenticationException(userName);
    }
  }

  private void isValidRequest(UserSigninRequest authenticationRequest) {
    if (ObjectUtils.isEmpty(authenticationRequest)) {
      throw new AccessDeniedException();
    }
  }
}
