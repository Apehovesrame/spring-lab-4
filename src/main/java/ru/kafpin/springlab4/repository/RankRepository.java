package ru.kafpin.springlab4.repository;

import org.springframework.data.repository.CrudRepository;
import ru.kafpin.springlab4.model.Rank;
import java.util.Optional;

public interface RankRepository extends CrudRepository<Rank, Long> {
    Optional<Rank> findByTitleIgnoreCase(String title);
}