package com.zilch.payments.security.auth.dto;

import java.util.Collection;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

@EqualsAndHashCode(callSuper = false)
@Getter
public class ZilchUserDetails extends User {

  private final Long userId;

  public ZilchUserDetails(
      Long userId,
      String username,
      String password,
      Collection<? extends GrantedAuthority> authorities) {
    super(username, password, authorities);
    this.userId = userId;
  }
}
