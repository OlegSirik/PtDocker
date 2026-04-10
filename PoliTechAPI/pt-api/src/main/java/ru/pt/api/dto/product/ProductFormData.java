package ru.pt.api.dto.product;

import java.util.Map;

/**
 * Данные для UI-формы продукта: пример JSON и справочники (код → подпись).
 */
public record ProductFormData(
        /** Например {@code quote} / {@code save} — тип сценария. */
        String formKey,
        String jsonExample,
        Map<String, Map<String, String>> lists
) {
}
