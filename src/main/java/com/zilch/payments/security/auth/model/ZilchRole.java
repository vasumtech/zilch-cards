package com.zilch.payments.security.auth.model;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;

@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "zilch_role")
public class ZilchRole implements GrantedAuthority {

  @Id
  @Column(name = "role_id")
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  private String name;

  public Integer getId() {
    return id;
  }

  public String getAuthority() {
    return name;
  }
}
