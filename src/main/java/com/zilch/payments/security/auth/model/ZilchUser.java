package com.zilch.payments.security.auth.model;

import jakarta.persistence.*;
import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.Set;
import lombok.*;

@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "zilch_user")
public class ZilchUser implements Serializable {

  @Id
  @Column(name = "user_id")
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long userId;

  @Column(name = "user_name", nullable = false, unique = true, length = 50)
  private String userName;

  @Column(name = "password", nullable = false, length = 256)
  private String password;

  @ManyToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
  @JoinTable(
      name = "zilch_user_roles",
      joinColumns = @JoinColumn(name = "user_id"),
      inverseJoinColumns = @JoinColumn(name = "role_id"))
  private Set<ZilchRole> roles = new HashSet<>();

  // IMPORTANT:
  // We need to agree and add other details like mobile_number, etc. as per the actual business
  // requirements

  @Column(name = "creation_date_time", nullable = false)
  private OffsetDateTime creationDateTime;

  @Version
  @Column(name = "version", nullable = false)
  private long version;
}
