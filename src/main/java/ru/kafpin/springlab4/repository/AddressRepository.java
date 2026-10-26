package ru.kafpin.springlab3.repository;

import org.springframework.data.repository.CrudRepository;
import ru.kafpin.springlab3.model.Address;

public interface AddressRepository extends CrudRepository<Address, Long> {
}