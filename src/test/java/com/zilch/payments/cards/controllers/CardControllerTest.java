package com.zilch.payments.cards.controllers;

import static com.zilch.payments.cards.CardTestFixture.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.zilch.payments.cards.exceptions.CardAlreadyAddedException;
import com.zilch.payments.cards.services.CardService;
import com.zilch.payments.models.AddCardRequest;
import com.zilch.payments.models.AddCardResponse;
import com.zilch.payments.security.auth.dto.ZilchAuthenticatedPrincipal;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

@ExtendWith(MockitoExtension.class)
class CardControllerTest {

  private static final ZilchAuthenticatedPrincipal AUTHENTICATED_PRINCIPAL =
      new ZilchAuthenticatedPrincipal(1L, ZILCH_TEST_USER_NAME);

  @Mock private CardService cardService;

  private CardController cardController;

  @BeforeEach
  void setUp() {
    this.cardController = new CardController(cardService);
    ZilchAuthenticatedPrincipal principal =
        new ZilchAuthenticatedPrincipal(1L, ZILCH_TEST_USER_NAME);
    UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken =
        new UsernamePasswordAuthenticationToken(
            principal, "", Set.of(new SimpleGrantedAuthority(ROLE_USER)));
    SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);
  }

  @DisplayName("Don't Save duplicate card requests")
  @Test
  void givenDuplicateAddCardRequest_whenSaveCard_thenReturnAddCardResponse() {
    AddCardRequest addCardRequest = createTestAddCardRequest();
    when(cardService.saveCard(addCardRequest, AUTHENTICATED_PRINCIPAL))
        .thenThrow(new CardAlreadyAddedException(ZILCH_TEST_USER_NAME));

    CardAlreadyAddedException cardAlreadyAddedException =
        assertThrows(CardAlreadyAddedException.class, () -> cardController.addCard(addCardRequest));

    verify(cardService).saveCard(addCardRequest, AUTHENTICATED_PRINCIPAL);
    assertThat(
        cardAlreadyAddedException.getMessage(),
        equalTo("Card already added for the user : zilchtestuser"));
  }

  @DisplayName("Save card details for a valid input request")
  @Test
  void givenValidAddCardRequest_whenSaveCard_thenReturnAddCardResponse() {
    AddCardRequest addCardRequest = createTestAddCardRequest();
    AddCardResponse addCardResponse = createTestAddCardResponse();
    when(cardService.saveCard(addCardRequest, AUTHENTICATED_PRINCIPAL)).thenReturn(addCardResponse);

    ResponseEntity<AddCardResponse> responseEntity = cardController.addCard(addCardRequest);

    verify(cardService).saveCard(addCardRequest, AUTHENTICATED_PRINCIPAL);
    assertThat(responseEntity.getStatusCode().value(), equalTo(HttpStatus.CREATED.value()));
    assertThat(responseEntity.getBody(), equalTo(addCardResponse));
  }
}
