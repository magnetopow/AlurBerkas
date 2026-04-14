package com.alurberkas.controller;

import com.alurberkas.model.User;
import com.alurberkas.service.NotificationService;
import com.alurberkas.service.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/notifications")
public class NotificationController {

    private final NotificationService notificationService;
    private final UserService userService;

    public NotificationController(NotificationService notificationService, UserService userService) {
        this.notificationService = notificationService;
        this.userService = userService;
    }

    @GetMapping
    public String listNotifications(Authentication auth, Model model) {
        User user = userService.findByNip(auth.getName());
        model.addAttribute("user", user);
        model.addAttribute("pageTitle", "Notifikasi");
        model.addAttribute("allNotifications", notificationService.getAllNotifications(user.getId()));
        model.addAttribute("unreadCount", notificationService.countUnread(user.getId()));
        return "notifications";
    }

    @PostMapping("/read/{id}")
    public String markAsRead(@PathVariable Long id,
                            @RequestParam(required = false) String redirect) {
        notificationService.markAsRead(id);
        return "redirect:" + (redirect != null ? redirect : "/notifications");
    }

    @PostMapping("/read-all")
    public String markAllAsRead(Authentication auth) {
        User user = userService.findByNip(auth.getName());
        notificationService.markAllAsRead(user.getId());
        return "redirect:/notifications";
    }

    @GetMapping("/count")
    @ResponseBody
    public long getUnreadCount(Authentication auth) {
        User user = userService.findByNip(auth.getName());
        return notificationService.countUnread(user.getId());
    }
}
