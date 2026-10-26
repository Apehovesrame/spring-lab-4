package ru.kafpin.springlab3.repository;

import org.springframework.data.repository.CrudRepository;
import ru.kafpin.springlab3.model.Serviceman;

import java.util.List;

public interface ServicemanRepository extends CrudRepository<Serviceman, Long> {
    List<Serviceman> findBySurnameContainsIgnoreCase(String surname);
    List<Serviceman> findByRankTitleIgnoreCase(String title);
    List<Serviceman> findByAddressCityIgnoreCase(String city);
}