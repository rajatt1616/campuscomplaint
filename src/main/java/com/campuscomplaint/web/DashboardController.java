package com.campuscomplaint.web;

import com.campuscomplaint.model.Complaint;
import com.campuscomplaint.model.ComplaintStatus;
import com.campuscomplaint.model.User;
import com.campuscomplaint.service.AuthService;
import com.campuscomplaint.service.ComplaintService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestAttribute;

import java.util.List;
import java.util.Map;

@Controller
public class DashboardController {

    private final ComplaintService complaints;
    private final AuthService auth;

    public DashboardController(ComplaintService complaints, AuthService auth) {
        this.complaints = complaints;
        this.auth = auth;
    }

    @GetMapping("/")
    public String home(@RequestAttribute("currentUser") User currentUser) {
        return "redirect:" + currentUser.dashboardView();
    }

    @GetMapping("/dashboard/student")
    public String student(@RequestAttribute("currentUser") User currentUser, Model model) {
        if (!currentUser.isStudent()) {
            return "redirect:" + currentUser.dashboardView();
        }
        List<Complaint> mine = complaints.byUser(currentUser.getId());
        model.addAttribute("complaints", mine);
        model.addAttribute("stats", List.of(
                stat("Total filed", mine.size()),
                stat("Open", countOpen(mine)),
                stat("Awaiting you", countStatus(mine, ComplaintStatus.RESOLVED)),
                stat("Closed", countClosed(mine))));
        model.addAttribute("page", "student");
        return "dashboard";
    }

    @GetMapping("/dashboard/admin")
    public String admin(@RequestAttribute("currentUser") User currentUser, Model model) {
        if (!currentUser.isAdmin()) {
            return "redirect:" + currentUser.dashboardView();
        }
        List<Complaint> all = complaints.all();
        model.addAttribute("complaints", all);
        model.addAttribute("departments", auth.departments());
        model.addAttribute("stats", List.of(
                stat("Total", all.size()),
                stat("Unassigned", countStatus(all, ComplaintStatus.FILED)),
                stat("In progress", countStatus(all, ComplaintStatus.IN_PROGRESS)),
                stat("Closed", countClosed(all))));
        model.addAttribute("page", "admin");
        return "dashboard";
    }

    @GetMapping("/dashboard/department")
    public String department(@RequestAttribute("currentUser") User currentUser, Model model) {
        if (!currentUser.isDepartment()) {
            return "redirect:" + currentUser.dashboardView();
        }
        List<Complaint> mine = complaints.byDepartment(currentUser.getId());
        model.addAttribute("complaints", mine);
        model.addAttribute("stats", List.of(
                stat("Assigned to me", mine.size()),
                stat("In progress", countStatus(mine, ComplaintStatus.IN_PROGRESS)),
                stat("Awaiting student", countStatus(mine, ComplaintStatus.RESOLVED)),
                stat("Closed", countClosed(mine))));
        model.addAttribute("page", "department");
        return "dashboard";
    }

    private Map<String, Object> stat(String label, long value) {
        return Map.of("label", label, "value", value);
    }

    private long countStatus(List<Complaint> list, ComplaintStatus status) {
        return list.stream().filter(c -> c.getStatus() == status).count();
    }

    private long countOpen(List<Complaint> list) {
        return list.stream()
                .filter(c -> c.getStatus() != ComplaintStatus.CLOSED
                        && c.getStatus() != ComplaintStatus.AUTO_CLOSED)
                .count();
    }

    private long countClosed(List<Complaint> list) {
        return list.stream()
                .filter(c -> c.getStatus() == ComplaintStatus.CLOSED
                        || c.getStatus() == ComplaintStatus.AUTO_CLOSED)
                .count();
    }
}
