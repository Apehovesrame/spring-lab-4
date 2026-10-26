package ru.kafpin.springlab4.repository;

import org.springframework.data.repository.CrudRepository;
import ru.kafpin.springlab4.model.Address;

public interface AddressRepository extends CrudRepository<Address, Long> {
}