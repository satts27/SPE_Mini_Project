package edu.spe.calculator.api;

public record ApiError(String error, String message, int status) {
}
