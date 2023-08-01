package com.zilch.payments.cards;

import com.zilch.payments.TestFixture;
import com.zilch.payments.cards.models.Card;
import com.zilch.payments.models.AddCardRequest;
import com.zilch.payments.models.AddCardResponse;
import java.time.LocalDate;
import java.time.OffsetDateTime;

public interface CardTestFixture extends TestFixture {

  static AddCardRequest createTestAddCardRequest() {
    return new AddCardRequest(
        1, "4234 8678 4321 8731", "Mr", "J KEVIN", "06/2021", "05/2026", "931");
  }

  static AddCardResponse createTestAddCardResponse() {
    return new AddCardResponse("Mr", "J KEVIN");
  }

  static Card createCard(AddCardRequest addCardRequest) {
    return new Card(
        ZILCH_TEST_USER_ID,
        addCardRequest.getCompanyId(),
        addCardRequest.getCardNumber(),
        addCardRequest.getPin(),
        LocalDate.of(1, 6, 21),
        LocalDate.of(31, 5, 26),
        addCardRequest.getTitle(),
        addCardRequest.getNameOnCard(),
        OffsetDateTime.now(),
        0);
  }
}
