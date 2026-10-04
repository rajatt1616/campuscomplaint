package com.campuscomplaint.web;

import com.campuscomplaint.exception.InvalidStatusException;
import com.campuscomplaint.exception.UnauthorizedException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.FlashMap;
import org.springframework.web.servlet.support.RequestContextUtils;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@ControllerAdvice
public class ExceptionHandlers {

    private static final Logger log = LoggerFactory.getLogger(ExceptionHandlers.class);

    @ExceptionHandler(UnauthorizedException.class)
    public String unauthorized(UnauthorizedException e, HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            return "redirect:/login";
        }
        flashError(request, e.getMessage());
        return "redirect:" + backTo(request);
    }

    @ExceptionHandler(InvalidStatusException.class)
    public String invalidStatus(InvalidStatusException e, HttpServletRequest request) {
        flashError(request, e.getMessage());
        return "redirect:" + backTo(request);
    }

    @ExceptionHandler(org.springframework.web.method.annotation.MethodArgumentTypeMismatchException.class)
    public String badInput(org.springframework.web.method.annotation.MethodArgumentTypeMismatchException e,
                           HttpServletRequest request) {
        flashError(request, "Invalid value for \"" + e.getName() + "\".");
        return "redirect:" + backTo(request);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public void notFound(HttpServletResponse response) throws Exception {
        response.sendError(HttpServletResponse.SC_NOT_FOUND);
    }

    @ExceptionHandler(Exception.class)
    public void unexpected(Exception e, HttpServletRequest request,
                           HttpServletResponse response) throws Exception {
        log.error("Unhandled exception on {} {}", request.getMethod(), request.getRequestURI(), e);
        if (!response.isCommitted()) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    private void flashError(HttpServletRequest request, String message) {
        FlashMap flashMap = RequestContextUtils.getOutputFlashMap(request);
        flashMap.put("error", message);
    }

    private String backTo(HttpServletRequest request) {
        String referer = request.getHeader("Referer");
        if (referer != null && !referer.isBlank()) {
            return referer;
        }
        return "/";
    }
}
