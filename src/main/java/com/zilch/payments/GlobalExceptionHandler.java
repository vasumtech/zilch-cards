package com.zilch.payments;

import com.zilch.payments.models.ApiError;
import com.zilch.payments.models.InputValidationError;
import com.zilch.payments.models.InternalError;
import java.util.List;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

@RequiredArgsConstructor
@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {
  private static final String UNEXPECTED_ERROR_DESC = "Unexpected error occurred on the server";

  private static final String UNEXPECTED_ERROR_CODE = "101";

  private final MessageSource messageSource;

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ApiError> handleMethodArgumentNotValid(
      MethodArgumentNotValidException exception, Locale locale) {
    ApiError apiError = new ApiError();
    exception
        .getBindingResult()
        .getAllErrors()
        .forEach(
            error -> {
              InputValidationError errorsItem = new InputValidationError();
              errorsItem.fieldName(((FieldError) error).getField());
              String message = messageSource.getMessage(error, locale);
              errorsItem.message(message);
              apiError.addErrorsItem(errorsItem);
            });
    return new ResponseEntity<>(apiError, HttpStatus.BAD_REQUEST);
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<InternalError> handleException(Exception exception) throws Exception {
    if (isResponseStatusWrappedInsideCause(exception)) {
      log.debug(" : Exception with ResponseStatus :: ", exception);
      throw exception;
    } else {
      log.error(exception.getMessage(), exception);
      InternalError internalError = new InternalError(UNEXPECTED_ERROR_CODE, UNEXPECTED_ERROR_DESC);
      return new ResponseEntity<>(internalError, HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  private boolean isResponseStatusWrappedInsideCause(Exception originalException) {
    List<Throwable> throwableList = ExceptionUtils.getThrowableList(originalException);
    return throwableList.stream()
        .anyMatch(throwable -> throwable.getClass().isAnnotationPresent(ResponseStatus.class));
  }
}
