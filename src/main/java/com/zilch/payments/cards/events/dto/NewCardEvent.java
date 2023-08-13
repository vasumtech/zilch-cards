package com.zilch.payments.cards.events.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

@AllArgsConstructor
@Data
@Builder
@Jacksonized
public class NewCardEvent {

  private long userId;

  private int companyId;

  private String cardNumber;
}
