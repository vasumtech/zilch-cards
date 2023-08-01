package com.zilch.payments.security.auth.services;

import com.zilch.payments.security.auth.dto.ZilchUserDetails;
import com.zilch.payments.security.auth.exceptions.UserAuthenticationException;
import com.zilch.payments.security.auth.repositories.ZilchUserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserDetailsServiceImpl implements UserDetailsService {

  private final ZilchUserRepository zilchUserRepository;

  @Override
  public UserDetails loadUserByUsername(String userName) {
    isValidUserName(userName);
    var zilchUserFromDb =
        zilchUserRepository
            .findByUserName(userName)
            .orElseThrow(() -> new UserAuthenticationException(userName));
    log.debug(" : user found : {}", userName);
    return new ZilchUserDetails(
        zilchUserFromDb.getUserId(),
        zilchUserFromDb.getUserName(),
        zilchUserFromDb.getPassword(),
        zilchUserFromDb.getRoles());
  }

  private void isValidUserName(String userName) {
    if (ObjectUtils.isEmpty(userName)) {
      throw new UserAuthenticationException(userName);
    }
  }
}
