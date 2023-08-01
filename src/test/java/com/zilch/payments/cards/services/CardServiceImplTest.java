package com.zilch.payments.cards.services;

import static com.zilch.payments.TestFixture.ZILCH_TEST_USER_ID;
import static com.zilch.payments.TestFixture.ZILCH_TEST_USER_NAME;
import static com.zilch.payments.cards.CardTestFixture.createCard;
import static com.zilch.payments.cards.CardTestFixture.createTestAddCardRequest;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

import com.zilch.payments.cards.exceptions.AccessDeniedException;
import com.zilch.payments.cards.exceptions.BadRequest;
import com.zilch.payments.cards.exceptions.CardAlreadyAddedException;
import com.zilch.payments.cards.mappers.CardMapper;
import com.zilch.payments.cards.models.Card;
import com.zilch.payments.cards.repositories.CardRepository;
import com.zilch.payments.models.AddCardRequest;
import com.zilch.payments.models.AddCardResponse;
import com.zilch.payments.security.auth.dto.ZilchAuthenticatedPrincipal;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class CardServiceImplTest {

  private static final String ENCODED_CARD_NUMBER =
      "$2a$10$v.IkZKyU9jIasOWvjchC7eMVupd.cNFT7k.cwOrBy5P6f7mi8yKuS";
  private static final String ENCODED_CARD_PIN =
      "$2a$10$IUALvTEZA.T8YfobQk5rmeP3ADDgyw6Q0L8uadX9pP8j1OGbcdimC";

  private static final String VALID_CARD_NUMBER = "4234 8678 4321 8731";
  private static final String VALID_PIN = "931";
  private static final String INCORRECT_VALID_FROM_DATE = "15/22";
  private static final String INCORRECT_VALID_UNTIL_DATE = "15/22";
  private static final String EXPIRED_VALID_UNTIL_DATE = "12/22";
  private static final String FUTURE_VALID_FROM_DATE =
      YearMonth.now().plusMonths(1).format(DateTimeFormatter.ofPattern("MM/yyyy"));

  private static final ZilchAuthenticatedPrincipal AUTHENTICATED_PRINCIPAL =
      new ZilchAuthenticatedPrincipal(ZILCH_TEST_USER_ID, ZILCH_TEST_USER_NAME);

  @Mock private CardRepository cardRepository;

  @Mock private CardMapper cardMapper;

  @Mock private PasswordEncoder passwordEncoder;

  @InjectMocks private CardServiceImpl cardService;

  @DisplayName("Test save card with an existing card from DB")
  @Test
  void givenDuplicateCardDetails_whenSaveCard_thenThrowCardAlreadyAddedException() {
    AddCardRequest addCardRequest = createTestAddCardRequest();
    Card card = createCard(addCardRequest);
    when(cardMapper.addCardRequestToCard(addCardRequest)).thenReturn(card);
    when(passwordEncoder.encode(VALID_CARD_NUMBER))
        .thenReturn("$2a$10$v.IkZKyU9jIasOWvjchC7eMVupd.cNFT7k.cwOrBy5P6f7mi8yKuS");
    when(passwordEncoder.encode(VALID_PIN))
        .thenReturn("$2a$10$IUALvTEZA.T8YfobQk5rmeP3ADDgyw6Q0L8uadX9pP8j1OGbcdimC");
    when(cardRepository.findById(ZILCH_TEST_USER_ID))
        .thenThrow(new CardAlreadyAddedException(ZILCH_TEST_USER_NAME));

    CardAlreadyAddedException cardAlreadyAddedException =
        assertThrows(
            CardAlreadyAddedException.class,
            () -> cardService.saveCard(addCardRequest, AUTHENTICATED_PRINCIPAL));

    verify(cardRepository, never()).save(any(Card.class));
    assertThat(
        cardAlreadyAddedException.getMessage(),
        is(equalTo("Card already added for the user : zilchtestuser")));
  }

  @DisplayName("Test save card with null user")
  @Test
  void givenNullUserName_whenSaveCard_thenThrowAccessDeniedException() {
    AddCardRequest addCardRequest = createTestAddCardRequest();

    AccessDeniedException accessDeniedException =
        assertThrows(AccessDeniedException.class, () -> cardService.saveCard(addCardRequest, null));

    verifyNoInteractions(cardRepository);
    assertThat(accessDeniedException.getMessage(), is(equalTo("Access denied")));
  }

  @DisplayName("Test save card with incorrect valid from date")
  @Test
  void givenIncorrectValidFromDate_whenSaveCard_thenThrowBadRequestException() {
    AddCardRequest addCardRequest = createTestAddCardRequest();
    addCardRequest.setValidFrom(INCORRECT_VALID_FROM_DATE);
    when(cardMapper.addCardRequestToCard(addCardRequest))
        .thenThrow(new BadRequest("valid from date is not correct: " + INCORRECT_VALID_FROM_DATE));

    BadRequest badRequest =
        assertThrows(
            BadRequest.class, () -> cardService.saveCard(addCardRequest, AUTHENTICATED_PRINCIPAL));

    verifyNoInteractions(cardRepository);
    assertThat(
        badRequest.getMessage(),
        is(equalTo("Bad request : valid from date is not correct: " + INCORRECT_VALID_FROM_DATE)));
  }

  @DisplayName("Test save card with future valid from date")
  @Test
  void givenFutureValidFromDate_whenSaveCard_thenThrowBadRequestException() {
    AddCardRequest addCardRequest = createTestAddCardRequest();
    addCardRequest.setValidFrom(FUTURE_VALID_FROM_DATE);
    when(cardMapper.addCardRequestToCard(addCardRequest))
        .thenThrow(new BadRequest("valid from date is a future date : " + FUTURE_VALID_FROM_DATE));

    BadRequest badRequest =
        assertThrows(
            BadRequest.class, () -> cardService.saveCard(addCardRequest, AUTHENTICATED_PRINCIPAL));

    verifyNoInteractions(cardRepository);
    assertThat(
        badRequest.getMessage(),
        is(equalTo("Bad request : valid from date is a future date : " + FUTURE_VALID_FROM_DATE)));
  }

  @DisplayName("Test save card with incorrect valid until date")
  @Test
  void givenIncorrectValidUntilDate_whenSaveCard_thenThrowBadRequestException() {
    AddCardRequest addCardRequest = createTestAddCardRequest();
    addCardRequest.setValidFrom(INCORRECT_VALID_UNTIL_DATE);
    when(cardMapper.addCardRequestToCard(addCardRequest))
        .thenThrow(
            new BadRequest("valid until date is not correct: " + INCORRECT_VALID_UNTIL_DATE));

    BadRequest badRequest =
        assertThrows(
            BadRequest.class, () -> cardService.saveCard(addCardRequest, AUTHENTICATED_PRINCIPAL));

    verifyNoInteractions(cardRepository);
    assertThat(
        badRequest.getMessage(),
        is(
            equalTo(
                "Bad request : valid until date is not correct: " + INCORRECT_VALID_UNTIL_DATE)));
  }

  @DisplayName("Test save card with expired card")
  @Test
  void givenExpiredValidUntilDate_whenSaveCard_thenThrowBadRequestException() {
    AddCardRequest addCardRequest = createTestAddCardRequest();
    addCardRequest.setValidFrom(EXPIRED_VALID_UNTIL_DATE);
    when(cardMapper.addCardRequestToCard(addCardRequest))
        .thenThrow(
            new BadRequest("valid until date already expired : " + EXPIRED_VALID_UNTIL_DATE));

    BadRequest badRequest =
        assertThrows(
            BadRequest.class, () -> cardService.saveCard(addCardRequest, AUTHENTICATED_PRINCIPAL));

    verifyNoInteractions(cardRepository);
    assertThat(
        badRequest.getMessage(),
        is(
            equalTo(
                "Bad request : valid until date already expired : " + EXPIRED_VALID_UNTIL_DATE)));
  }

  @DisplayName("Test save card details with valid AddCardRequest")
  @Test
  void givenValidAddCardRequest_whenSaveCard_thenReturnAddCardResponse() {
    AddCardRequest addCardRequest = createTestAddCardRequest();
    Card card = createCard(addCardRequest);
    when(cardRepository.findById(ZILCH_TEST_USER_ID)).thenReturn(Optional.empty());
    when(cardMapper.addCardRequestToCard(addCardRequest)).thenReturn(card);
    when(passwordEncoder.encode(VALID_CARD_NUMBER)).thenReturn(ENCODED_CARD_NUMBER);
    when(passwordEncoder.encode(VALID_PIN)).thenReturn(ENCODED_CARD_PIN);
    when(cardRepository.save(card)).thenReturn(card);

    AddCardResponse actualResponse = cardService.saveCard(addCardRequest, AUTHENTICATED_PRINCIPAL);

    verify(cardRepository).findById(ZILCH_TEST_USER_ID);
    verify(cardRepository).save(card);
    AddCardResponse expectedResponse =
        new AddCardResponse(addCardRequest.getTitle(), addCardRequest.getNameOnCard());
    assertThat(actualResponse, is(equalTo(expectedResponse)));
  }
}
