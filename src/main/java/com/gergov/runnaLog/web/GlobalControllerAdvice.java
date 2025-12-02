package com.gergov.runnaLog.web;

import com.gergov.runnaLog.exception.UserEmailAlreadyExistsException;
import com.gergov.runnaLog.exception.UserNeedsEliteSubscriptionException;
import com.gergov.runnaLog.exception.UserNotFoundException;
import com.gergov.runnaLog.exception.UsernameAlreadyExistsException;
import org.springframework.http.HttpStatus;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.web.bind.MissingRequestValueException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.nio.file.AccessDeniedException;

@RestControllerAdvice
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

    @ExceptionHandler(UserNeedsEliteSubscriptionException.class)
    public ModelAndView handleUserNeedsEliteSubscriptionException(UserNeedsEliteSubscriptionException e) {


        ModelAndView modelAndView = new ModelAndView("error-message");
        modelAndView.addObject("errorMessage", e.getMessage());
        modelAndView.addObject("statusCode", 404);

        return modelAndView;
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler({
            AccessDeniedException.class,
            AuthorizationDeniedException.class,
            NoResourceFoundException.class,
            MethodArgumentTypeMismatchException.class,
            MissingRequestValueException.class
    })
    public ModelAndView handleNotFoundExceptions() {

        ModelAndView modelAndView = new ModelAndView("error-message");
        modelAndView.addObject("errorMessage", "The page you are looking for doesn’t exist, the request was invalid or you don't have access to it.");
        modelAndView.addObject("statusCode", 404);

        return modelAndView;
    }

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(Exception.class)
    public ModelAndView handleLeftOverException() {

        ModelAndView modelAndView = new ModelAndView("error-message");

        modelAndView.addObject("errorMessage", "Something went wrong on our side.\n" +
                "We’re working to fix it — please try again shortly.");

        return modelAndView;
    }
}
