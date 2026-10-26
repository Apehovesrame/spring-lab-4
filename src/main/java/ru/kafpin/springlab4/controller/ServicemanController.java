package ru.kafpin.springlab4.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import ru.kafpin.springlab4.model.Serviceman;
import ru.kafpin.springlab4.repository.ServicemanRepository;
import ru.kafpin.springlab4.repository.RankRepository;
import ru.kafpin.springlab4.repository.AddressRepository;

import java.util.Optional;

@Controller
@RequestMapping("/servicemen")
public class ServicemanController {

    private final ServicemanRepository repository;
    private final RankRepository rankRepository;
    private final AddressRepository addressRepository;

    // Внедряем все три репозитория через конструктор
    public ServicemanController(ServicemanRepository repository,
                                RankRepository rankRepository,
                                AddressRepository addressRepository) {
        this.repository = repository;
        this.rankRepository = rankRepository;
        this.addressRepository = addressRepository;
    }

    @GetMapping
    public String getAll(Model model) {
        model.addAttribute("servicemen", repository.findAll());
        return "index";
    }

    @GetMapping("/details/{id}")
    public String details(@PathVariable("id") Long id, Model model) {
        Optional<Serviceman> optional = repository.findById(id);
        if (optional.isEmpty()) {
            return "redirect:/servicemen";
        }
        model.addAttribute("serviceman", optional.get());
        return "details";
    }

    @GetMapping("/add")
    public String addForm(Model model) {
        model.addAttribute("serviceman", new Serviceman());
        // Передаем списки для выпадающих меню в форме
        model.addAttribute("ranks", rankRepository.findAll());
        model.addAttribute("addresses", addressRepository.findAll());
        return "form";
    }

    @PostMapping("/save")
    public String save(@ModelAttribute Serviceman serviceman) {
        repository.save(serviceman);
        return "redirect:/servicemen";
    }

    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable("id") Long id, Model model) {
        Optional<Serviceman> optional = repository.findById(id);
        if (optional.isEmpty()) {
            return "redirect:/servicemen";
        }
        model.addAttribute("serviceman", optional.get());
        // Передаем списки для выпадающих меню при редактировании
        model.addAttribute("ranks", rankRepository.findAll());
        model.addAttribute("addresses", addressRepository.findAll());
        return "form";
    }

    @GetMapping("/delete/{id}")
    public String delete(@PathVariable("id") Long id) {
        if (repository.existsById(id)) {
            repository.deleteById(id);
        }
        return "redirect:/servicemen";
    }
}