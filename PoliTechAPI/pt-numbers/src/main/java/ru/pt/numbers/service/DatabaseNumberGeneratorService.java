package ru.pt.numbers.service;

import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import ru.pt.api.dto.exception.NotFoundException;
import ru.pt.api.dto.numbers.NumberGeneratorDescription;
import ru.pt.api.service.numbers.NumberGeneratorService;
import ru.pt.numbers.entity.NumberGeneratorEntity;
import ru.pt.numbers.repository.NumberGeneratorRepository;

import java.time.LocalDate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ru.pt.domain.model.VariableContext;
import org.springframework.stereotype.Component;
import lombok.RequiredArgsConstructor;

/**
 * Кор реализация сервиса генерации номеров через таблицу в БД
 */
@Component
@RequiredArgsConstructor
public class DatabaseNumberGeneratorService implements NumberGeneratorService {

    private final NumberGeneratorRepository repository;

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public String getNextNumber(Long tid, NumberGeneratorDescription ng, VariableContext values) {

        Long currentValue = getNext(tid, ng);

        String mask = ng.getMask();
        StringBuilder resultMask = new StringBuilder(mask);
        LocalDate today = LocalDate.now();

        // Replace {KEY} patterns
        Pattern pattern = Pattern.compile("\\{([^}]+)\\}");
        Matcher matcher = pattern.matcher(mask);

        while (matcher.find()) {
            String key = matcher.group(1);
            String replacement;

            // Built-in date keys
            if ("YYYY".equals(key)) {
                replacement = Integer.toString(today.getYear());
            } else if ("YY".equals(key)) {
                replacement = String.format("%02d", today.getYear() % 100);
            } else if ("MM".equals(key)) {
                replacement = String.format("%02d", today.getMonthValue());
            } else if (key.matches("X+")) {
                // Handle any number of X's as sequence number (XXXX, XXX, XX, etc.)
                int xCount = key.length();
                //Long currentValue = ng.getCurrentValue();
                if (ng.getXorMask() != null && !ng.getXorMask().isEmpty()) {
                    currentValue = currentValue ^ Integer.parseInt(ng.getXorMask());
                }
                replacement = String.format("%0" + xCount + "d", currentValue);

            } else {
                // Get value from provided map
                replacement = values.getString(key);
            }

            replaceAll(resultMask, matcher.group(0), replacement);
        }

        return resultMask.toString();
    }


    private static void replaceAll(StringBuilder sb, String target, String replacement) {
        int idx;
        while ((idx = sb.indexOf(target)) != -1) {
            sb.replace(idx, idx + target.length(), replacement);
        }
    }

    @Override
    public void create(Long tid, NumberGeneratorDescription numberGeneratorDescription) {
        var entity = new NumberGeneratorEntity();
        //entity.setCode(numberGeneratorDescription.getProductCode());
        entity.setTid(tid);
        entity.setCurrentValue(0L);
        entity.setLastReset(LocalDate.now());
        repository.save(entity);
        numberGeneratorDescription.setId(entity.getId());
    }

    @Override
    public void reset(Long tid, Long id) {
        var nge = repository.findByTidAndIdForUpdate(tid, id)
            .orElseThrow(() -> new NotFoundException("NumberGenerator not found with id: " + id));

        nge.setCurrentValue(0L);
        nge.setLastReset(LocalDate.now());
        repository.save(nge);
    }

    private Long getNext(Long tid, NumberGeneratorDescription ng) {

        NumberGeneratorEntity nge = repository.findByTidAndIdForUpdate(tid, ng.getId())
                .orElseThrow(() -> new NotFoundException("Generator not found: " + ng.getId()));

        LocalDate today = LocalDate.now();
        switch (ng.getResetPolicy()) {
            case YEARLY:
                if (nge.getLastReset().getYear() != today.getYear()) {
                    nge.setLastReset(today);
                    nge.setCurrentValue(0L);
                }
                break;
            case MONTHLY:
                if (nge.getLastReset().getYear() != today.getYear() || nge.getLastReset().getMonth() != today.getMonth()) {
                    nge.setLastReset(today);
                    nge.setCurrentValue(0L);
                }
                break;
            case NEVER:
        }

        Long next = nge.getCurrentValue() + 1;
        if (ng.getMaxValue() != null && next > ng.getMaxValue()) {
            nge.setCurrentValue(0L);
        }
        repository.save(nge);

        Long newValue = repository.incrementAndGetCurrentValue(tid, ng.getId());
        if (newValue == null) {
            throw new NotFoundException("Generator not found: " + ng.getId());
        }
        nge.setCurrentValue(newValue);

        return newValue;
    }

}
