package ru.pt.db.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.pt.api.dto.currency.CurrencyRateDto;
import ru.pt.api.dto.exception.NotFoundException;
import ru.pt.api.service.currency.CurrencyRateService;
import ru.pt.db.entity.CurrencyRateEntity;
import ru.pt.db.repository.CurrencyRateRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Locale;

@Service
public class CurrencyRateServiceImpl implements CurrencyRateService {

    private final CurrencyRateRepository currencyRateRepository;

    public CurrencyRateServiceImpl(CurrencyRateRepository currencyRateRepository) {
        this.currencyRateRepository = currencyRateRepository;
    }

    @Override
    @Transactional
    public CurrencyRateDto create(CurrencyRateDto dto) {
        CurrencyRateEntity entity = toEntity(dto);
        normalizeCurrencies(entity);
        CurrencyRateEntity saved = currencyRateRepository.save(entity);
        return toDto(saved);
    }

    @Override
    @Transactional
    public CurrencyRateDto update(Long id, CurrencyRateDto dto) {
        CurrencyRateEntity existing = currencyRateRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Currency rate not found: " + id));

        existing.setTid(dto.getTid());
        existing.setFromCurrency(dto.getFromCurrency());
        existing.setToCurrency(dto.getToCurrency());
        existing.setRate(dto.getRate());
        existing.setValidFrom(dto.getValidFrom());
        existing.setValidTo(dto.getValidTo());
        existing.setSource(dto.getSource());
        normalizeCurrencies(existing);

        CurrencyRateEntity saved = currencyRateRepository.save(existing);
        return toDto(saved);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        if (!currencyRateRepository.existsById(id)) {
            throw new NotFoundException("Currency rate not found: " + id);
        }
        currencyRateRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public CurrencyRateDto getById(Long id) {
        return currencyRateRepository.findById(id)
                .map(this::toDto)
                .orElseThrow(() -> new NotFoundException("Currency rate not found: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<CurrencyRateDto> getAll() {
        return currencyRateRepository.findAll().stream()
                .map(this::toDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<CurrencyRateDto> getByPair(String fromCurrency, String toCurrency) {
        return currencyRateRepository
                .findByFromCurrencyIgnoreCaseAndToCurrencyIgnoreCaseOrderByValidFromDesc(fromCurrency, toCurrency)
                .stream()
                .map(this::toDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<CurrencyRateDto> getByPairAndDate(String fromCurrency, String toCurrency, LocalDate date) {
        return currencyRateRepository
                .findByPairAndDate(fromCurrency, toCurrency, date)
                .stream()
                .map(this::toDto)
                .toList();
    }

    private CurrencyRateDto toDto(CurrencyRateEntity entity) {
        CurrencyRateDto dto = new CurrencyRateDto();
        dto.setId(entity.getId());
        dto.setTid(entity.getTid());
        dto.setFromCurrency(entity.getFromCurrency());
        dto.setToCurrency(entity.getToCurrency());
        dto.setRate(entity.getRate());
        dto.setValidFrom(entity.getValidFrom());
        dto.setValidTo(entity.getValidTo());
        dto.setSource(entity.getSource());
        return dto;
    }

    private CurrencyRateEntity toEntity(CurrencyRateDto dto) {
        CurrencyRateEntity entity = new CurrencyRateEntity();
        entity.setTid(dto.getTid());
        entity.setFromCurrency(dto.getFromCurrency());
        entity.setToCurrency(dto.getToCurrency());
        entity.setRate(dto.getRate());
        entity.setValidFrom(dto.getValidFrom());
        entity.setValidTo(dto.getValidTo());
        entity.setSource(dto.getSource());
        return entity;
    }

    private void normalizeCurrencies(CurrencyRateEntity entity) {
        if (entity.getFromCurrency() != null) {
            entity.setFromCurrency(entity.getFromCurrency().trim().toUpperCase(Locale.ROOT));
        }
        if (entity.getToCurrency() != null) {
            entity.setToCurrency(entity.getToCurrency().trim().toUpperCase(Locale.ROOT));
        }
    }
}
