package ru.kafpin.springlab4.repository;

import org.springframework.data.repository.CrudRepository;
import ru.kafpin.springlab4.model.Serviceman;

import java.util.List;

public interface ServicemanRepository extends CrudRepository<Serviceman, Long> {
    List<Serviceman> findBySurnameContainsIgnoreCase(String surname);
    List<Serviceman> findByRankTitleIgnoreCase(String title);
    List<Serviceman> findByAddressCityIgnoreCase(String city);
}