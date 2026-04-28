package ru.pt.api.service.currency;

import ru.pt.api.dto.currency.CurrencyRateDto;

import java.time.LocalDate;
import java.util.List;

public interface CurrencyRateService {

    CurrencyRateDto create(CurrencyRateDto dto);

    CurrencyRateDto update(Long id, CurrencyRateDto dto);

    void delete(Long id);

    CurrencyRateDto getById(Long id);

    List<CurrencyRateDto> getAll();

    List<CurrencyRateDto> getByPair(String fromCurrency, String toCurrency);

    List<CurrencyRateDto> getByPairAndDate(String fromCurrency, String toCurrency, LocalDate date);
}
