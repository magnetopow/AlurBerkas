package com.alurberkas.controller;

import com.alurberkas.model.Berkas;
import com.alurberkas.model.User;
import com.alurberkas.model.enums.BerkasStatus;
import com.alurberkas.service.BerkasService;
import com.alurberkas.service.NotificationService;
import com.alurberkas.service.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;
import java.util.Map;

@Controller
public class DashboardController {

    private final BerkasService berkasService;
    private final UserService userService;
    private final NotificationService notificationService;

    public DashboardController(BerkasService berkasService, UserService userService,
                               NotificationService notificationService) {
        this.berkasService = berkasService;
        this.userService = userService;
        this.notificationService = notificationService;
    }

    @GetMapping("/dashboard")
    public String dashboard(Authentication auth, Model model) {
        User user = userService.findByNip(auth.getName());
        if (user == null) {
            return "redirect:/login";
        }

        model.addAttribute("user", user);
        model.addAttribute("pageTitle", "Dashboard");

        // Get berkas for this user's role
        List<Berkas> myBerkas = berkasService.findByRole(user);
        model.addAttribute("myBerkas", myBerkas);
        model.addAttribute("myBerkasCount", myBerkas.size());

        // Statistics
        model.addAttribute("totalBerkas", berkasService.countAll());
        model.addAttribute("completedBerkas", berkasService.countCompleted());
        model.addAttribute("inProgressBerkas", berkasService.countInProgress());

        // Status counts for chart
        Map<BerkasStatus, Long> statusCounts = berkasService.getStatusCounts();
        model.addAttribute("statusCounts", statusCounts);

        // Per-status counts
        model.addAttribute("countDiterima", berkasService.countByStatus(BerkasStatus.DITERIMA));
        model.addAttribute("countPengukuran", berkasService.countByStatus(BerkasStatus.PENGUKURAN));
        model.addAttribute("countPemetaan", berkasService.countByStatus(BerkasStatus.PEMETAAN));
        model.addAttribute("countVerifikasi", berkasService.countByStatus(BerkasStatus.VERIFIKASI));
        model.addAttribute("countApproval", berkasService.countByStatus(BerkasStatus.APPROVAL));
        model.addAttribute("countSelesai", berkasService.countByStatus(BerkasStatus.SELESAI));
        model.addAttribute("countDikembalikan", berkasService.countByStatus(BerkasStatus.DIKEMBALIKAN));

        // Notifications
        model.addAttribute("unreadCount", notificationService.countUnread(user.getId()));
        model.addAttribute("notifications", notificationService.getRecentNotifications(user.getId()));

        return "dashboard";
    }
}
