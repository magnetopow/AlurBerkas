package com.alurberkas.controller;

import com.alurberkas.model.User;
import com.alurberkas.model.enums.Role;
import com.alurberkas.service.NotificationService;
import com.alurberkas.service.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/users")
public class UserController {

    private final UserService userService;
    private final NotificationService notificationService;

    public UserController(UserService userService, NotificationService notificationService) {
        this.userService = userService;
        this.notificationService = notificationService;
    }

    private User getCurrentUser(Authentication auth) {
        return userService.findByNip(auth.getName());
    }

    @GetMapping
    public String listUsers(Authentication auth, Model model) {
        User user = getCurrentUser(auth);
        model.addAttribute("user", user);
        model.addAttribute("pageTitle", "Manajemen User");
        model.addAttribute("users", userService.findAll());
        model.addAttribute("roles", Role.values());
        model.addAttribute("unreadCount", notificationService.countUnread(user.getId()));
        return "users/list";
    }

    @GetMapping("/create")
    public String createForm(Authentication auth, Model model) {
        User user = getCurrentUser(auth);
        model.addAttribute("user", user);
        model.addAttribute("pageTitle", "Tambah User Baru");
        model.addAttribute("roles", Role.values());
        model.addAttribute("unreadCount", notificationService.countUnread(user.getId()));
        return "users/form";
    }

    @PostMapping("/create")
    public String createUser(@RequestParam String nip,
                            @RequestParam String nama,
                            @RequestParam String password,
                            @RequestParam String role,
                            @RequestParam(required = false) String noHp,
                            RedirectAttributes redirectAttributes) {
        try {
            userService.createUser(nip, nama, password, Role.valueOf(role), noHp);
            redirectAttributes.addFlashAttribute("success", "User berhasil ditambahkan");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Gagal menambah user: " + e.getMessage());
            return "redirect:/users/create";
        }
        return "redirect:/users";
    }

    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable Long id, Authentication auth, Model model) {
        User user = getCurrentUser(auth);
        User editUser = userService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User tidak ditemukan"));

        model.addAttribute("user", user);
        model.addAttribute("editUser", editUser);
        model.addAttribute("pageTitle", "Edit User: " + editUser.getNama());
        model.addAttribute("roles", Role.values());
        model.addAttribute("unreadCount", notificationService.countUnread(user.getId()));
        return "users/form";
    }

    @PostMapping("/edit/{id}")
    public String updateUser(@PathVariable Long id,
                            @RequestParam String nama,
                            @RequestParam String role,
                            @RequestParam(required = false) String noHp,
                            @RequestParam(required = false) boolean active,
                            RedirectAttributes redirectAttributes) {
        try {
            userService.updateUser(id, nama, Role.valueOf(role), noHp, active);
            redirectAttributes.addFlashAttribute("success", "User berhasil diperbarui");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Gagal memperbarui user: " + e.getMessage());
        }
        return "redirect:/users";
    }

    @PostMapping("/toggle/{id}")
    public String toggleActive(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            userService.toggleActive(id);
            redirectAttributes.addFlashAttribute("success", "Status user berhasil diubah");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Gagal mengubah status: " + e.getMessage());
        }
        return "redirect:/users";
    }

    @PostMapping("/reset-password/{id}")
    public String resetPassword(@PathVariable Long id,
                               @RequestParam String newPassword,
                               RedirectAttributes redirectAttributes) {
        try {
            userService.resetPassword(id, newPassword);
            redirectAttributes.addFlashAttribute("success", "Password berhasil direset");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Gagal reset password: " + e.getMessage());
        }
        return "redirect:/users";
    }
}
