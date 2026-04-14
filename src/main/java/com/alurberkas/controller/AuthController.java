package com.alurberkas.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class AuthController {

    @GetMapping("/login")
    public String loginPage(@RequestParam(value = "error", required = false) String error,
                           @RequestParam(value = "logout", required = false) String logout,
                           Model model) {
        if (error != null) {
            model.addAttribute("error", "NIP atau Password salah. Silakan coba lagi.");
        }
        if (logout != null) {
            model.addAttribute("message", "Anda telah berhasil logout.");
        }
        return "login";
    }

    @GetMapping("/")
    public String root() {
        return "redirect:/dashboard";
    }
}
