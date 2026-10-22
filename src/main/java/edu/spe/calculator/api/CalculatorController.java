package edu.spe.calculator.api;

import edu.spe.calculator.service.CalculatorService;
import java.math.BigDecimal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
public class CalculatorController {

    private final CalculatorService calculatorService;

    public CalculatorController(CalculatorService calculatorService) {
        this.calculatorService = calculatorService;
    }

    @GetMapping("/add")
    public CalculationResponse add(@RequestParam BigDecimal a, @RequestParam BigDecimal b) {
        return calculatorService.add(a, b);
    }

    @GetMapping("/subtract")
    public CalculationResponse subtract(@RequestParam BigDecimal a, @RequestParam BigDecimal b) {
        return calculatorService.subtract(a, b);
    }

    @GetMapping("/multiply")
    public CalculationResponse multiply(@RequestParam BigDecimal a, @RequestParam BigDecimal b) {
        return calculatorService.multiply(a, b);
    }

    @GetMapping("/divide")
    public CalculationResponse divide(@RequestParam BigDecimal a, @RequestParam BigDecimal b) {
        return calculatorService.divide(a, b);
    }
}
