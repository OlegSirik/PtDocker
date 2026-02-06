package ru.pt.api.service.product;

import ru.pt.api.dto.product.LobModel;

import java.util.List;

/**
 * Методы для работы с общей частью продукта(Линия бизнеса - LOB)
 */
public interface LobService {
    /**
     * Получить список активных линий бизнеса
     * @return краткие описания LOB
     */
    List<LobModel> listActiveSummaries();

    /**
     * Найти LOB по коду
     * @param code код линии бизнеса
     * @return модель линии бизнеса
     */
    LobModel getByCode(String code);

    /**
     * Найти LOB по идентификатору
     * @param id айди линии бизнеса
     * @return модель линии бизнеса
     */
    LobModel getById(Integer id);

    /**
     * Создать новую линию бизнеса
     * @param payload описание линии
     * @return созданная модель
     */
    LobModel create(LobModel payload);

    /**
     * Мягко удалить LOB по идентификатору
     * @param id айди линии бизнеса
     * @return true если отметка удаления установлена
     */
    boolean softDeleteById(Integer id);

    /**
     * Обновить LOB по коду
     * @param payload новое описание
     * @return обновленная модель
     */
    LobModel update(LobModel payload);
}