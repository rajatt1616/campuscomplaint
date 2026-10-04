package com.campuscomplaint.web;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class ViewAdvice {

    @ModelAttribute("fmt")
    public ViewFormatter formatter() {
        return new ViewFormatter();
    }
}
