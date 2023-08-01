package com.zilch.payments.cards.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Version;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import lombok.*;

@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity(name = "card")
public class Card {

  @Id
  @Column(name = "user_id", length = 50, nullable = false)
  private Long userId;

  @Column(name = "company_id", nullable = false)
  private int companyId;

  // cardNumber is encrypted
  @Column(name = "card_number", length = 256, nullable = false)
  private String cardNumber;

  // pin is encrypted
  @Column(name = "pin", length = 256, nullable = false)
  private String pin;

  @Column(name = "valid_from", nullable = false)
  private LocalDate validFrom;

  @Column(name = "valid_upto", nullable = false)
  private LocalDate validUpto;

  @Column(name = "title", length = 5, nullable = false)
  private String title;

  @Column(name = "name_on_card", length = 50, nullable = false)
  private String nameOnCard;

  @Column(name = "creation_date_time", nullable = false)
  private OffsetDateTime creationDateTime;

  @Version
  @Column(name = "version", nullable = false)
  private long version;
}
