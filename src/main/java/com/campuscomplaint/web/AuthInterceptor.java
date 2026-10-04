package com.campuscomplaint.web;

import com.campuscomplaint.model.User;
import com.campuscomplaint.service.AuthService;
import com.campuscomplaint.service.NotificationService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class AuthInterceptor implements HandlerInterceptor {

    private final AuthService auth;
    private final NotificationService notifications;

    public AuthInterceptor(AuthService auth, NotificationService notifications) {
        this.auth = auth;
        this.notifications = notifications;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        String path = request.getRequestURI();
        if (path.startsWith("/login") || path.startsWith("/css/") || path.startsWith("/js/")
                || path.startsWith("/fonts/") || path.startsWith("/img/") || path.equals("/error")) {
            return true;
        }
        HttpSession session = request.getSession(false);
        String userId = session == null ? null : (String) session.getAttribute("userId");
        if (userId == null) {
            response.sendRedirect("/login");
            return false;
        }
        User user = auth.findById(userId).orElse(null);
        if (user == null) {
            if (session != null) {
                session.invalidate();
            }
            response.sendRedirect("/login");
            return false;
        }
        request.setAttribute("currentUser", user);
        request.setAttribute("notifications", notifications.recent(userId, 6));
        request.setAttribute("unreadCount", notifications.unreadCount(userId));
        return true;
    }
}
