package com.zilch.payments.security.auth.repositories;

import com.zilch.payments.security.auth.model.ZilchUser;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ZilchUserRepository extends JpaRepository<ZilchUser, Long> {

  Optional<ZilchUser> findByUserName(String username);
}
