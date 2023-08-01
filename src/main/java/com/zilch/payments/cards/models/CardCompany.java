package com.zilch.payments.cards.models;

import jakarta.persistence.*;
import lombok.*;

@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity(name = "card_company")
public class CardCompany {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "company_id", nullable = false)
  private Integer companyId;

  @Column(name = "company_name", length = 30, nullable = false)
  private String companyName;
}
