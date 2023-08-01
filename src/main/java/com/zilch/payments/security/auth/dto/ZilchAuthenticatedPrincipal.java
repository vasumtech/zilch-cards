package com.zilch.payments.security.auth.dto;

import lombok.*;

@Data
@ToString
@AllArgsConstructor
@Builder
@Getter
public class ZilchAuthenticatedPrincipal {

  // IMPORTANT: Agree and add any other parameters required (to be loaded to the SecurityContext)

  private Long userId;

  private String userName;
}
