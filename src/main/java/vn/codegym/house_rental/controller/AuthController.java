package vn.codegym.house_rental.controller;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import vn.codegym.house_rental.model.User;
import vn.codegym.house_rental.service.UserService;

@Controller
@AllArgsConstructor
public class AuthController {

    private final UserService userService;

    @GetMapping("/login")
    public String login(){
        return "auth/login";
    }

    @PostMapping("/register")
    public String register(User user){

        userService.save(user);

        return "redirect:/login";
    }
}
