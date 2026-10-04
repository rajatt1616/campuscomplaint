package com.campuscomplaint.web;

import com.campuscomplaint.exception.InvalidStatusException;
import com.campuscomplaint.exception.UnauthorizedException;
import com.campuscomplaint.model.Category;
import com.campuscomplaint.model.Complaint;
import com.campuscomplaint.model.ComplaintStatus;
import com.campuscomplaint.model.Priority;
import com.campuscomplaint.model.User;
import com.campuscomplaint.service.AuthService;
import com.campuscomplaint.service.ComplaintService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.Instant;

@Controller
public class ComplaintController {

    private final ComplaintService complaints;
    private final AuthService auth;

    public ComplaintController(ComplaintService complaints, AuthService auth) {
        this.complaints = complaints;
        this.auth = auth;
    }

    @GetMapping("/complaints/new")
    public String newComplaint(@RequestAttribute("currentUser") User currentUser, Model model) {
        if (!currentUser.isStudent()) {
            return "redirect:" + currentUser.dashboardView();
        }
        model.addAttribute("categories", Category.values());
        model.addAttribute("priorities", Priority.values());
        return "complaints/new";
    }

    @PostMapping("/complaints")
    public String create(@RequestAttribute("currentUser") User currentUser,
                         @RequestParam String title,
                         @RequestParam String description,
                         @RequestParam Category category,
                         @RequestParam Priority priority,
                         RedirectAttributes redirect) {
        String t = title == null ? "" : title.trim();
        String d = description == null ? "" : description.trim();
        java.util.Map<String, String> errors = new java.util.LinkedHashMap<>();
        if (t.length() < 8) {
            errors.put("title", "Title must be at least 8 characters.");
        } else if (t.length() > 120) {
            errors.put("title", "Title must be at most 120 characters.");
        }
        if (d.length() < 20) {
            errors.put("description", "Description must be at least 20 characters.");
        }
        if (!errors.isEmpty()) {
            redirect.addFlashAttribute("errors", errors);
            redirect.addFlashAttribute("formTitle", t);
            redirect.addFlashAttribute("formDescription", d);
            redirect.addFlashAttribute("formCategory", category);
            redirect.addFlashAttribute("formPriority", priority);
            return "redirect:/complaints/new";
        }
        try {
            Complaint c = complaints.create(currentUser, t, d, category, priority);
            redirect.addFlashAttribute("success", "Complaint " + c.getId() + " filed.");
            return "redirect:/complaints/" + c.getId();
        } catch (UnauthorizedException | InvalidStatusException e) {
            redirect.addFlashAttribute("error", e.getMessage());
            return "redirect:/complaints/new";
        }
    }

    @GetMapping("/complaints/{id}")
    public String detail(@RequestAttribute("currentUser") User currentUser,
                         @PathVariable String id,
                         Model model,
                         RedirectAttributes redirect) {
        try {
            Complaint c = complaints.require(id);
            model.addAttribute("c", c);
            model.addAttribute("departments", auth.departments());
            model.addAttribute("canReopen", c.getReopenDeadlineAt() != null
                    && java.time.Instant.now().isBefore(c.getReopenDeadlineAt()));
            return "complaints/detail";
        } catch (UnauthorizedException e) {
            redirect.addFlashAttribute("error", e.getMessage());
            return "redirect:/";
        }
    }

    @PostMapping("/complaints/{id}/assign")
    public String assign(@RequestAttribute("currentUser") User currentUser,
                         @PathVariable String id,
                         @RequestParam String departmentUserId,
                         RedirectAttributes redirect) {
        return action(redirect, () -> complaints.assign(currentUser, id, departmentUserId), id);
    }

    @PostMapping("/complaints/{id}/start")
    public String start(@RequestAttribute("currentUser") User currentUser,
                        @PathVariable String id,
                        RedirectAttributes redirect) {
        return action(redirect, () -> complaints.startWork(currentUser, id), id);
    }

    @PostMapping("/complaints/{id}/resolve")
    public String resolve(@RequestAttribute("currentUser") User currentUser,
                          @PathVariable String id,
                          @RequestParam(required = false) String remark,
                          RedirectAttributes redirect) {
        return action(redirect, () -> complaints.markResolved(currentUser, id, remark), id);
    }

    @PostMapping("/complaints/{id}/accept")
    public String accept(@RequestAttribute("currentUser") User currentUser,
                         @PathVariable String id,
                         RedirectAttributes redirect) {
        return action(redirect, () -> complaints.accept(currentUser, id), id);
    }

    @PostMapping("/complaints/{id}/reject")
    public String reject(@RequestAttribute("currentUser") User currentUser,
                         @PathVariable String id,
                         @RequestParam String reason,
                         RedirectAttributes redirect) {
        return action(redirect, () -> complaints.reject(currentUser, id, reason), id);
    }

    @PostMapping("/complaints/{id}/reopen")
    public String reopen(@RequestAttribute("currentUser") User currentUser,
                         @PathVariable String id,
                         RedirectAttributes redirect) {
        return action(redirect, () -> complaints.reopen(currentUser, id), id);
    }

    @PostMapping("/complaints/{id}/remarks")
    public String remark(@RequestAttribute("currentUser") User currentUser,
                         @PathVariable String id,
                         @RequestParam String text,
                         RedirectAttributes redirect) {
        try {
            complaints.addRemark(currentUser, id, text);
            redirect.addFlashAttribute("success", "Remark added.");
        } catch (UnauthorizedException | InvalidStatusException e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/complaints/" + id;
    }

    @GetMapping("/search")
    public String search(@RequestAttribute("currentUser") User currentUser,
                         @RequestParam(required = false) String keyword,
                         @RequestParam(required = false) ComplaintStatus status,
                         @RequestParam(required = false) Category category,
                         Model model) {
        model.addAttribute("results", complaints.search(keyword, status, category));
        model.addAttribute("keyword", keyword);
        model.addAttribute("selectedStatus", status);
        model.addAttribute("selectedCategory", category);
        model.addAttribute("statuses", ComplaintStatus.values());
        model.addAttribute("categories", Category.values());
        return "search";
    }

    private String action(RedirectAttributes redirect, Runnable work, String id) {
        try {
            work.run();
            redirect.addFlashAttribute("success", "Updated.");
        } catch (UnauthorizedException | InvalidStatusException e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/complaints/" + id;
    }
}
