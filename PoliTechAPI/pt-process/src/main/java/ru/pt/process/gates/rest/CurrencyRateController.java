package ru.pt.process.gates.rest;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.HttpStatus;
import ru.pt.api.dto.currency.CurrencyRateDto;
import ru.pt.api.service.currency.CurrencyRateService;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/currencyRates")
public class CurrencyRateController {

    private final CurrencyRateService currencyRateService;

    public CurrencyRateController(CurrencyRateService currencyRateService) {
        this.currencyRateService = currencyRateService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CurrencyRateDto create(@RequestBody CurrencyRateDto dto) {
        return currencyRateService.create(dto);
    }

    @PutMapping("/{id}")
    public CurrencyRateDto update(@PathVariable Long id, @RequestBody CurrencyRateDto dto) {
        return currencyRateService.update(id, dto);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        currencyRateService.delete(id);
    }

    @GetMapping("/{id}")
    public CurrencyRateDto getById(@PathVariable Long id) {
        return currencyRateService.getById(id);
    }

    @GetMapping
    public List<CurrencyRateDto> getAll(
            @RequestParam(required = false) String fromCurrency,
            @RequestParam(required = false) String toCurrency,
            @RequestParam(required = false) String cur1,
            @RequestParam(required = false) String cur2,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        String from = fromCurrency != null ? fromCurrency : cur1;
        String to = toCurrency != null ? toCurrency : cur2;

        if (from != null && to != null && date != null) {
            return currencyRateService.getByPairAndDate(from, to, date);
        }
        if (from != null && to != null) {
            return currencyRateService.getByPair(from, to);
        }
        return currencyRateService.getAll();
    }
}
