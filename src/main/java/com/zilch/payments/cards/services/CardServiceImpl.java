package com.zilch.payments.cards.services;

import com.zilch.payments.cards.events.dto.NewCardEvent;
import com.zilch.payments.cards.events.producers.NewCardEventPublisher;
import com.zilch.payments.cards.exceptions.AccessDeniedException;
import com.zilch.payments.cards.exceptions.CardAlreadyAddedException;
import com.zilch.payments.cards.mappers.CardMapper;
import com.zilch.payments.cards.models.Card;
import com.zilch.payments.cards.repositories.CardRepository;
import com.zilch.payments.models.AddCardRequest;
import com.zilch.payments.models.AddCardResponse;
import com.zilch.payments.security.auth.dto.ZilchAuthenticatedPrincipal;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class CardServiceImpl implements CardService {

  /* IMPORTANT: Currently, BCryptPasswordEncoder (i.e., one-way hash) is being injected here,
    however, we need to decide and inject the appropriate (one-way vs two-way) encoder
    after agreeing with the actual business requirements!
  */
  private final PasswordEncoder passwordEncoder;

  private final CardRepository cardRepository;

  private final CardMapper cardMapper;

  private final NewCardEventPublisher newCardEventPublisher;

  @Override
  @Transactional
  public AddCardResponse saveCard(
      AddCardRequest addCardRequest, ZilchAuthenticatedPrincipal zilchAuthenticatedPrincipal) {
    isValidUser(zilchAuthenticatedPrincipal);
    log.debug(" : Entry : {}", zilchAuthenticatedPrincipal.getUserName());
    Card card = convertToCard(addCardRequest, zilchAuthenticatedPrincipal.getUserId());
    Optional<Card> optCardFromDB = cardRepository.findById(zilchAuthenticatedPrincipal.getUserId());
    optCardFromDB.ifPresent(
        cardFromDB -> {
          throw new CardAlreadyAddedException(zilchAuthenticatedPrincipal.getUserName());
        });
    Card cardSaved = cardRepository.save(card);
    log.debug(" : Exit : {}", zilchAuthenticatedPrincipal.getUserName());
    publishNewCardEvent(addCardRequest, zilchAuthenticatedPrincipal.getUserId());
    return new AddCardResponse(cardSaved.getTitle(), cardSaved.getNameOnCard());
  }

  private void isValidUser(ZilchAuthenticatedPrincipal zilchAuthenticatedPrincipal) {
    if (ObjectUtils.isEmpty(zilchAuthenticatedPrincipal)) {
      throw new AccessDeniedException();
    }
  }

  private Card convertToCard(AddCardRequest addCardRequest, Long userId) {
    Card card = cardMapper.addCardRequestToCard(addCardRequest);
    card.setCardNumber(passwordEncoder.encode(addCardRequest.getCardNumber()));
    card.setPin(passwordEncoder.encode(addCardRequest.getPin()));
    card.setUserId(userId);
    return card;
  }

  private void publishNewCardEvent(AddCardRequest addCardRequest, long userId) {
    NewCardEvent newCardEvent = new NewCardEvent(userId, addCardRequest.getCompanyId(),
            addCardRequest.getCardNumber());
    newCardEventPublisher.publishNewCardEvent(newCardEvent);
  }
}
