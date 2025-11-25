package com.gergov.runnaLog.web;

import com.gergov.runnaLog.exception.UserEmailAlreadyExistsException;
import com.gergov.runnaLog.exception.UserNotFoundException;
import com.gergov.runnaLog.exception.UsernameAlreadyExistsException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MissingRequestValueException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.nio.file.AccessDeniedException;

@ControllerAdvice
public class GlobalControllerAdvice {

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(UserNotFoundException.class)
    public ModelAndView handleException(UserNotFoundException e) {

        ModelAndView modelAndView = new ModelAndView("error-message");
        modelAndView.addObject("errorMessage", e.getMessage());
        modelAndView.addObject("statusCode", HttpStatus.NOT_FOUND.value());

        return modelAndView;
    }

    @ExceptionHandler(UsernameAlreadyExistsException.class)
    public String handleUsernameAlreadyExistsException(UsernameAlreadyExistsException e, RedirectAttributes redirectAttributes) {

        redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());

        return "redirect:/register";
    }

    @ExceptionHandler(UserEmailAlreadyExistsException.class)
    public String handleUserEmailAlreadyExistsException(UserEmailAlreadyExistsException e, RedirectAttributes redirectAttributes) {

        redirectAttributes.addFlashAttribute("errorMessage2", e.getMessage());

        return "redirect:/register";
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler({
            AccessDeniedException.class,
            NoResourceFoundException.class,
            MethodArgumentTypeMismatchException.class,
            MissingRequestValueException.class
    })
    public ModelAndView handleNotFoundExceptions(Exception exception) {

        ModelAndView modelAndView = new ModelAndView("error-message");
        modelAndView.addObject("errorMessage", exception.getMessage());
        modelAndView.addObject("statusCode", HttpStatus.NOT_FOUND.value());

        return modelAndView;
    }

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(Exception.class)
    public ModelAndView handleLeftOverException(Exception e) {

        ModelAndView modelAndView = new ModelAndView("error-message");

        modelAndView.addObject("errorMessage", e.getClass().getSimpleName());

        return modelAndView;
    }
}
