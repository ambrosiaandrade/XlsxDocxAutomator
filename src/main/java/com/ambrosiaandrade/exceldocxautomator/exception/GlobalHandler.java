package com.ambrosiaandrade.exceldocxautomator.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

@ControllerAdvice
public class GlobalHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalHandler.class);

    @ExceptionHandler(Exception.class)
    @ResponseBody
    public String handleException(Exception e, Model model) {
        logger.error("Error: ", e);
        model.addAttribute("errorMessage", e.getMessage());
        model.addAttribute("timestamp", java.time.LocalDateTime.now());
        model.addAttribute("errorType", e.getClass().getSimpleName());
        return e.getMessage();
    }

}
