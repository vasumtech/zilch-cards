package com.zilch.payments.cards.repositories;

import com.zilch.payments.cards.models.Card;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CardRepository extends CrudRepository<Card, Long> {}
