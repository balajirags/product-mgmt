package com.inventory.demo.exception;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void shouldHandleResourceNotFoundException() {
        // given
        ResourceNotFoundException ex = new ResourceNotFoundException("Location", "WH-MUM");

        // when
        ProblemDetail result = handler.handleNotFound(ex);

        // then
        assertThat(result.getStatus()).isEqualTo(HttpStatus.NOT_FOUND.value());
        assertThat(result.getTitle()).isEqualTo("Resource Not Found");
        assertThat(result.getProperties()).containsEntry("error_code", "RESOURCE_NOT_FOUND");
    }

    @Test
    void shouldHandleBusinessRuleException() {
        // given
        BusinessRuleException ex = new BusinessRuleException("DUPLICATE_LOCATION", "Location already exists");

        // when
        ProblemDetail result = handler.handleBusinessRule(ex);

        // then
        assertThat(result.getStatus()).isEqualTo(HttpStatus.CONFLICT.value());
        assertThat(result.getTitle()).isEqualTo("Business Rule Violation");
        assertThat(result.getProperties()).containsEntry("error_code", "DUPLICATE_LOCATION");
    }

    @Test
    void shouldHandleUnexpectedException() {
        // given
        Exception ex = new RuntimeException("Something went wrong");

        // when
        ProblemDetail result = handler.handleUnexpected(ex);

        // then
        assertThat(result.getStatus()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR.value());
        assertThat(result.getTitle()).isEqualTo("Internal Server Error");
        assertThat(result.getProperties()).containsEntry("error_code", "INTERNAL_ERROR");
    }

    @Test
    void shouldHandleBusinessRuleExceptionWithCause() {
        // given
        BusinessRuleException ex = new BusinessRuleException("ERROR",
                "msg", new IllegalArgumentException("root cause"));

        // when
        ProblemDetail result = handler.handleBusinessRule(ex);

        // then
        assertThat(result.getStatus()).isEqualTo(HttpStatus.CONFLICT.value());
        assertThat(result.getDetail()).isEqualTo("msg");
    }

    @SuppressWarnings("unchecked")
    @Test
    void shouldHandleBeanValidation_whenFieldErrorHasNullMessage() {
        // given
        BindingResult bindingResult = mock(BindingResult.class);
        FieldError fieldError = new FieldError("request", "name", null, false,
                null, null, null);
        when(bindingResult.getFieldErrors()).thenReturn(List.of(fieldError));
        MethodArgumentNotValidException ex = new MethodArgumentNotValidException(null, bindingResult);

        // when
        ProblemDetail result = handler.handleBeanValidation(ex);

        // then
        assertThat(result.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        Map<String, String> fieldErrors = (Map<String, String>) result.getProperties().get("field_errors");
        assertThat(fieldErrors).containsEntry("name", "invalid");
    }

    @SuppressWarnings("unchecked")
    @Test
    void shouldHandleBeanValidation_whenDuplicateFieldErrors() {
        // given
        BindingResult bindingResult = mock(BindingResult.class);
        FieldError error1 = new FieldError("request", "name", "first error");
        FieldError error2 = new FieldError("request", "name", "second error");
        when(bindingResult.getFieldErrors()).thenReturn(List.of(error1, error2));
        MethodArgumentNotValidException ex = new MethodArgumentNotValidException(null, bindingResult);

        // when
        ProblemDetail result = handler.handleBeanValidation(ex);

        // then
        assertThat(result.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        Map<String, String> fieldErrors = (Map<String, String>) result.getProperties().get("field_errors");
        assertThat(fieldErrors).containsEntry("name", "first error");
    }
}
