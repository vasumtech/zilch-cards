package com.zilch.payments.security.auth.repositories;

import com.zilch.payments.security.auth.model.ZilchRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ZilchRoleRepository extends JpaRepository<ZilchRole, Integer> {

  ZilchRole findByName(String name);
}
