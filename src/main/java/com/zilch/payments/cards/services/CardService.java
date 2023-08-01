package com.zilch.payments.cards.services;

import com.zilch.payments.models.AddCardRequest;
import com.zilch.payments.models.AddCardResponse;
import com.zilch.payments.security.auth.dto.ZilchAuthenticatedPrincipal;

public interface CardService {
  AddCardResponse saveCard(
      AddCardRequest addCardRequest, ZilchAuthenticatedPrincipal zilchAuthenticatedPrincipal);
}
