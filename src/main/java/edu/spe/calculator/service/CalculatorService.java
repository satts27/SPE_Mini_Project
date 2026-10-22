package edu.spe.calculator.service;

import edu.spe.calculator.api.CalculationResponse;
import java.math.BigDecimal;
import java.math.MathContext;
import org.springframework.stereotype.Service;

@Service
public class CalculatorService {

    public CalculationResponse add(BigDecimal a, BigDecimal b) {
        return response("add", a, b, a.add(b));
    }

    public CalculationResponse subtract(BigDecimal a, BigDecimal b) {
        return response("subtract", a, b, a.subtract(b));
    }

    public CalculationResponse multiply(BigDecimal a, BigDecimal b) {
        return response("multiply", a, b, a.multiply(b));
    }

    public CalculationResponse divide(BigDecimal a, BigDecimal b) {
        if (b.compareTo(BigDecimal.ZERO) == 0) {
            throw new DivisionByZeroException("Division by zero is not allowed");
        }
        return response("divide", a, b, a.divide(b, MathContext.DECIMAL128));
    }

    private CalculationResponse response(String operation, BigDecimal a, BigDecimal b, BigDecimal result) {
        return new CalculationResponse(operation, a, b, result);
    }
}
