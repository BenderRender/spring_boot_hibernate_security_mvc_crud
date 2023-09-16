package ru.kata.spring.boot_security.demo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import ru.kata.spring.boot_security.demo.model.User;
import ru.kata.spring.boot_security.demo.service.UserService;

import javax.validation.Valid;
import java.security.Principal;


@Controller
public class UserController {

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/index")
    public String showIndex() {
        return "index";
    }

    @GetMapping("/admin")
    public String showAllUsers(Model model) {
        model.addAttribute("users", userService.findAll());
        return "users/all_users";
    }

    @GetMapping("/user")
    public String showUserDetails(Model model, Principal principal) {
        String username = principal.getName();
        User user = userService.findByName(username);
        if (user == null) {
            return "access-denied";
        }
        model.addAttribute("user", user);
        return "user";
    }

    @GetMapping("/{id}")
    public String showUserDetails(Model model, @PathVariable("id") int id, Principal principal) {
        User user = userService.findById(id);

        if (user == null) {
            return "access-denied";
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(r -> r.getAuthority().equals("ROLE_ADMIN"));


        if (!isAdmin) {
            String username = principal.getName();
            if (!user.getName().equals(username)) {
                return "access-denied";
            }
        }

        model.addAttribute("user", user);
        return "user";
    }

    @GetMapping("/new")
    public String newPerson(@ModelAttribute("user") User user) {
        return "users/new";
    }

    @PostMapping()
    public String create(@ModelAttribute("user") @Valid User user,
                         BindingResult bindingResult) {
        if (bindingResult.hasErrors())
            return "users/new";

        userService.save(user);
        return "redirect:/admin";
    }

    @GetMapping("/{id}/edit")
    public String edit(Model model, @PathVariable("id") int id) {
        model.addAttribute("user", userService.findById(id));
        return "users/edit";
    }

    @PatchMapping("/{id}")
    public String update(@ModelAttribute("user") @Valid User user, BindingResult bindingResult,
                         @PathVariable("id") int id) {
        if (bindingResult.hasErrors())
            return "users/edit";

        userService.update(id, user);
        return "redirect:/admin";
    }

    @DeleteMapping("/{id}")
    public String delete(@PathVariable("id") int id) {
        userService.delete(id);
        return "redirect:/admin";
    }

}
