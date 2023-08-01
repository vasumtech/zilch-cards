package com.zilch.payments.cards.controllers.advice;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.zilch.payments.GlobalExceptionHandler;
import com.zilch.payments.cards.exceptions.BadRequest;
import com.zilch.payments.models.ApiError;
import com.zilch.payments.models.InputValidationError;
import com.zilch.payments.models.InternalError;
import java.util.List;
import java.util.Locale;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;

@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {

  private static final String UNEXPECTED_ERROR_CODE = "101";

  private static final String UNEXPECTED_ERROR_DESC = "Unexpected error occurred on the server";

  private static final Locale locale = Locale.ENGLISH;

  private static final String FIELD = "field Name";

  private static final String ERROR_MESSAGE = "Error message";

  private static final String OBJECT_NAME = "objectName";

  @Mock private MessageSource messageSource;

  @Mock private BindingResult bindingResult;

  private GlobalExceptionHandler globalExceptionHandler;

  @BeforeEach
  void setup() {
    globalExceptionHandler = new GlobalExceptionHandler(messageSource);
  }

  @Test
  void handleMethodArgumentNotValid() {
    MethodArgumentNotValidException methodArgumentNotValidException =
        new MethodArgumentNotValidException((MethodParameter) null, bindingResult);
    ObjectError error = new FieldError(OBJECT_NAME, FIELD, ERROR_MESSAGE);
    when(bindingResult.getAllErrors()).thenReturn(List.of(error));
    when(messageSource.getMessage(any(), any())).thenReturn(ERROR_MESSAGE);

    ResponseEntity<ApiError> response =
        globalExceptionHandler.handleMethodArgumentNotValid(
            methodArgumentNotValidException, locale);

    verify(messageSource).getMessage(error, locale);
    assertThat(response.getStatusCode().value(), is(equalTo(HttpStatus.BAD_REQUEST.value())));
    ApiError apiError = response.getBody();
    assertNotNull(apiError);
    List<InputValidationError> validationErrors = apiError.getErrors();
    assertThat(validationErrors.size(), is(equalTo(1)));
    InputValidationError inputValidationError = new InputValidationError();
    inputValidationError.setFieldName(FIELD);
    inputValidationError.setMessage(ERROR_MESSAGE);
    assertThat(validationErrors.get(0), is(equalTo(inputValidationError)));
  }

  @Test
  void handleExceptionWithResponseCode() throws Exception {
    BadRequest badRequest = new BadRequest(ERROR_MESSAGE);
    assertThrows(BadRequest.class, () -> globalExceptionHandler.handleException(badRequest));
  }

  @Test
  void handleExceptionWithoutResponseCode() throws Exception {
    Exception exe = new Exception(ERROR_MESSAGE);
    ResponseEntity<?> response = globalExceptionHandler.handleException(exe);
    assertThat(
        response.getStatusCode().value(), is(equalTo(HttpStatus.INTERNAL_SERVER_ERROR.value())));
    InternalError expected = new InternalError(UNEXPECTED_ERROR_CODE, UNEXPECTED_ERROR_DESC);
    assertThat(response.getBody(), is(equalTo(expected)));
  }
}
