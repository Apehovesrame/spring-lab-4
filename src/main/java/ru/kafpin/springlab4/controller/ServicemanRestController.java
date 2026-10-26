package ru.kafpin.springlab4.controller;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.kafpin.springlab4.dto.ServicemanDTO;
import ru.kafpin.springlab4.model.Address;
import ru.kafpin.springlab4.model.Rank;
import ru.kafpin.springlab4.model.Serviceman;
import ru.kafpin.springlab4.repository.AddressRepository;
import ru.kafpin.springlab4.repository.RankRepository;
import ru.kafpin.springlab4.repository.ServicemanRepository;

import java.time.LocalDate;
import java.time.Period;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@RestController
@RequestMapping("/api")
public class ServicemanRestController {

    private final ServicemanRepository servicemanRepository;
    private final RankRepository rankRepository;
    private final AddressRepository addressRepository;

    public ServicemanRestController(ServicemanRepository servicemanRepository,
                                    RankRepository rankRepository,
                                    AddressRepository addressRepository) {
        this.servicemanRepository = servicemanRepository;
        this.rankRepository = rankRepository;
        this.addressRepository = addressRepository;
    }

    // 1. БАЗОВЫЕ CRUD ОПЕРАЦИИ

    // Получить всех военнослужащих
    @GetMapping("/servicemen")
    public Iterable<Serviceman> getAllServicemen() {
        return servicemanRepository.findAll();
    }

    // Получить все звания
    @GetMapping("/ranks")
    public Iterable<Rank> getAllRanks() {
        return rankRepository.findAll();
    }

    // Получить все адреса
    @GetMapping("/addresses")
    public Iterable<Address> getAllAddresses() {
        return addressRepository.findAll();
    }

    // Получить одного военнослужащего по ID
    @GetMapping("/servicemen/{id}")
    public Serviceman getServiceman(@PathVariable("id") Long id) {
        return servicemanRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Военнослужащий с ID " + id + " не найден"));
    }

    // Создать нового военнослужащего через DTO (201 CREATED)
    @PostMapping("/servicemen")
    @ResponseStatus(HttpStatus.CREATED)
    public Serviceman createServiceman(@RequestBody ServicemanDTO dto) {
        Rank rank = rankRepository.findByTitleIgnoreCase(dto.getRankTitle())
                .orElseThrow(() -> new EntityNotFoundException("Звание не найдено: " + dto.getRankTitle()));
        Address address = addressRepository.findById(dto.getAddressId())
                .orElseThrow(() -> new EntityNotFoundException("Адрес с ID " + dto.getAddressId() + " не найден"));

        Serviceman serviceman = new Serviceman();
        serviceman.setSurname(dto.getSurname());
        serviceman.setName(dto.getName());
        serviceman.setPatronymic(dto.getPatronymic());
        serviceman.setNationality(dto.getNationality());
        serviceman.setBirthDate(dto.getBirthDate());
        serviceman.setPosition(dto.getPosition());
        serviceman.setRank(rank);
        serviceman.setAddress(address);

        return servicemanRepository.save(serviceman);
    }

    // Полное обновление военнослужащего (PUT)
    @PutMapping("/servicemen/{id}")
    public Serviceman updateServiceman(@PathVariable("id") Long id, @RequestBody ServicemanDTO dto) {
        Serviceman serviceman = servicemanRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Военнослужащий с ID " + id + " не найден"));
        Rank rank = rankRepository.findByTitleIgnoreCase(dto.getRankTitle())
                .orElseThrow(() -> new EntityNotFoundException("Некорректное звание: " + dto.getRankTitle()));
        Address address = addressRepository.findById(dto.getAddressId())
                .orElseThrow(() -> new EntityNotFoundException("Некорректный ID адреса: " + dto.getAddressId()));

        serviceman.setSurname(dto.getSurname());
        serviceman.setName(dto.getName());
        serviceman.setPatronymic(dto.getPatronymic());
        serviceman.setNationality(dto.getNationality());
        serviceman.setBirthDate(dto.getBirthDate());
        serviceman.setPosition(dto.getPosition());
        serviceman.setRank(rank);
        serviceman.setAddress(address);

        return servicemanRepository.save(serviceman);
    }

    // Удаление военнослужащего (204 NO CONTENT)
    @DeleteMapping("/servicemen/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteServiceman(@PathVariable("id") Long id) {
        servicemanRepository.deleteById(id);
    }

    // 2. РАСШИРЕННАЯ БИЗНЕС-ЛОГИКА

    // Бизнес-операция 1: Статистика и аналитика (с фильтром по городу)
    @GetMapping("/servicemen/statistics")
    public Map<String, Object> getStatistics(@RequestParam(required = false) String city) {
        List<Serviceman> list = StreamSupport.stream(servicemanRepository.findAll().spliterator(), false)
                .collect(Collectors.toList());

        if (city != null && !city.isBlank()) {
            list = list.stream()
                    .filter(s -> s.getAddress().getCity().equalsIgnoreCase(city))
                    .collect(Collectors.toList());
        }

        Map<String, Long> countByRank = list.stream()
                .collect(Collectors.groupingBy(s -> s.getRank().getTitle(), Collectors.counting()));

        double averageAge = list.stream()
                .filter(s -> s.getBirthDate() != null)
                .mapToInt(s -> Period.between(s.getBirthDate(), LocalDate.now()).getYears())
                .average()
                .orElse(0.0);

        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("totalCount", list.size());
        stats.put("averageAge", Math.round(averageAge * 10.0) / 10.0);
        stats.put("distributionByRank", countByRank);
        return stats;
    }

    // Бизнес-операция 2: Повышение в звании и назначение на новую должность
    @PostMapping("/servicemen/{id}/promote")
    public Map<String, Object> promoteServiceman(@PathVariable Long id,
                                                 @RequestBody Map<String, String> request) {
        Serviceman serviceman = servicemanRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Военнослужащий не найден"));

        String newRankTitle = request.get("newRank");
        String newPosition = request.get("newPosition");

        String oldRankTitle = serviceman.getRank().getTitle();
        Rank newRank = rankRepository.findByTitleIgnoreCase(newRankTitle)
                .orElseThrow(() -> new EntityNotFoundException("Звание " + newRankTitle + " не найдено в справочнике"));

        serviceman.setRank(newRank);
        if (newPosition != null && !newPosition.isBlank()) {
            serviceman.setPosition(newPosition);
        }
        servicemanRepository.save(serviceman);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("message", "Военнослужащий успешно повышен");
        response.put("serviceman", serviceman.getSurname() + " " + serviceman.getName());
        response.put("oldRank", oldRankTitle);
        response.put("newRank", newRank.getTitle());
        response.put("currentPosition", serviceman.getPosition());
        return response;
    }

    // Бизнес-операция 3: Частичное обновление данных (PATCH)
    @PatchMapping("/servicemen/{id}")
    public Serviceman partialUpdateServiceman(@PathVariable Long id,
                                              @RequestBody Map<String, Object> updates) {
        Serviceman serviceman = servicemanRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Военнослужащий не найден"));

        updates.forEach((key, value) -> {
            switch (key) {
                case "surname" -> serviceman.setSurname((String) value);
                case "name" -> serviceman.setName((String) value);
                case "patronymic" -> serviceman.setPatronymic((String) value);
                case "nationality" -> serviceman.setNationality((String) value);
                case "position" -> serviceman.setPosition((String) value);
                case "birthDate" -> serviceman.setBirthDate(LocalDate.parse((String) value));
                case "rankTitle" -> {
                    Rank rank = rankRepository.findByTitleIgnoreCase((String) value)
                            .orElseThrow(() -> new EntityNotFoundException("Звание не найдено"));
                    serviceman.setRank(rank);
                }
                case "addressId" -> {
                    Long addrId = Long.valueOf(value.toString());
                    Address address = addressRepository.findById(addrId)
                            .orElseThrow(() -> new EntityNotFoundException("Адрес не найден"));
                    serviceman.setAddress(address);
                }
            }
        });

        return servicemanRepository.save(serviceman);
    }
}