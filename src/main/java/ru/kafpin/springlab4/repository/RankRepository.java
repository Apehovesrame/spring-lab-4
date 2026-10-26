package ru.kafpin.springlab3.repository;

import org.springframework.data.repository.CrudRepository;
import ru.kafpin.springlab3.model.Rank;

public interface RankRepository extends CrudRepository<Rank, Long> {
}