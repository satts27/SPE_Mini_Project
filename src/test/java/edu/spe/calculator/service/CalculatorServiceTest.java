package edu.spe.calculator.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import edu.spe.calculator.api.CalculationResponse;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class CalculatorServiceTest {

    private final CalculatorService calculatorService = new CalculatorService();

    @Test
    void addsNumbers() {
        CalculationResponse response = calculatorService.add(decimal("2"), decimal("3"));
        assertEquals("add", response.operation());
        assertEquals(0, decimal("5").compareTo(response.result()));
    }

    @Test
    void subtractsNumbers() {
        CalculationResponse response = calculatorService.subtract(decimal("7"), decimal("3"));
        assertEquals("subtract", response.operation());
        assertEquals(0, decimal("4").compareTo(response.result()));
    }

    @Test
    void multipliesNumbers() {
        CalculationResponse response = calculatorService.multiply(decimal("2.5"), decimal("4"));
        assertEquals("multiply", response.operation());
        assertEquals(0, decimal("10").compareTo(response.result()));
    }

    @Test
    void dividesNumbers() {
        CalculationResponse response = calculatorService.divide(decimal("7"), decimal("2"));
        assertEquals("divide", response.operation());
        assertEquals(0, decimal("3.5").compareTo(response.result()));
    }

    @Test
    void rejectsDivisionByZero() {
        assertThrows(DivisionByZeroException.class,
                () -> calculatorService.divide(decimal("1"), decimal("0")));
    }

    private BigDecimal decimal(String value) {
        return new BigDecimal(value);
    }
}
