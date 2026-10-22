package edu.spe.calculator.api;

import edu.spe.calculator.service.DivisionByZeroException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(DivisionByZeroException.class)
    public ResponseEntity<ApiError> handleDivisionByZero(DivisionByZeroException exception) {
        return badRequest("division_by_zero", exception.getMessage());
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiError> handleMissingParameter(MissingServletRequestParameterException exception) {
        return badRequest("missing_parameter", "Required parameter '" + exception.getParameterName() + "' is missing");
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiError> handleInvalidParameter(MethodArgumentTypeMismatchException exception) {
        return badRequest("invalid_parameter", "Parameter '" + exception.getName() + "' must be numeric");
    }

    private ResponseEntity<ApiError> badRequest(String error, String message) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ApiError(error, message, HttpStatus.BAD_REQUEST.value()));
    }
}
