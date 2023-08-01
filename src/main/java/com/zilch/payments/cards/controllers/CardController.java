package com.zilch.payments.cards.controllers;

import com.zilch.payments.api.CardsApiDelegate;
import com.zilch.payments.cards.services.CardService;
import com.zilch.payments.models.AddCardRequest;
import com.zilch.payments.models.AddCardResponse;
import com.zilch.payments.security.auth.dto.ZilchAuthenticatedPrincipal;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
@RequiredArgsConstructor
public class CardController implements CardsApiDelegate {

  private final CardService cardService;

  @Override
  public ResponseEntity<AddCardResponse> addCard(@Valid final AddCardRequest addCardRequest) {
    log.debug(" : Entry : ");
    AddCardResponse zilchCardsResponse =
        cardService.saveCard(addCardRequest, getAuthenticatedPrincipal());
    return new ResponseEntity<>(zilchCardsResponse, HttpStatus.CREATED);
  }

  private ZilchAuthenticatedPrincipal getAuthenticatedPrincipal() {
    return (ZilchAuthenticatedPrincipal)
        SecurityContextHolder.getContext().getAuthentication().getPrincipal();
  }
}
