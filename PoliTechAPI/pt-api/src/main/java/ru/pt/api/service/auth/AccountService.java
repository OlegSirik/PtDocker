package ru.pt.api.service.auth;

import com.fasterxml.jackson.databind.node.ObjectNode;
import ru.pt.api.dto.auth.Account;
import ru.pt.api.dto.auth.ProductRole;

import java.util.List;
import java.util.Set;

public interface AccountService {
    /**
     * Получить учетную запись по идентификатору
     * @param id идентификатор аккаунта
     * @return учетная запись
     */
    Account getAccountById(Long id);

    /**
     * Создать клиентскую запись верхнего уровня
     * @param name имя клиента
     * @return созданная запись
     */
    Account createClient(String name);

    /**
     * Создать группу внутри клиента
     * @param name название группы
     * @param parentId идентификатор родителя
     * @return созданная запись
     */
    Account createGroup(String name, Long parentId);

    /**
     * Создать рабочий аккаунт
     * @param name имя аккаунта
     * @param parentId идентификатор группы
     * @return созданная запись
     */
    Account createAccount(String name, Long parentId);

    /**
     * Создать субаккаунт
     * @param name имя субаккаунта
     * @param parentId идентификатор родительского аккаунта
     * @return созданная запись
     */
    Account createSubaccount(String name, Long parentId);

    /**
     * Назначить роли продукта аккаунту
     * @param accountId идентификатор аккаунта
     * @param productRole описание прав по продукту
     * @return обновленная запись роли
     */
    Account grantProduct(Long accountId, ProductRole productRole);

    /**
     * Получить роли продукта в полном описании
     * @param accountId идентификатор аккаунта
     * @return список ролей продукта
     */
    List<ProductRole> getProductRolesByAccountId(Long accountId);

    /**
     * Получить набор кодов ролей продукта
     * @param accountId идентификатор аккаунта
     * @return множество ролей
     */
    Set<String> getProductRoles(Long accountId);

    /**
     * Вернуть данные для логина аккаунта
     * @param login логин пользователя
     * @param client код клиента
     * @param accountId идентификатор аккаунта
     * @return json с данными логина
     */
    ObjectNode getAccountLogin(String login, String client, Long accountId);

    List<Account> getAccountsByParentId(Long parentId);

    /**
     * Получить все аккаунты для текущего пользователя
     * @param tenantCode код тенанта
     * @param authClientId код клиента
     * @param userLogin логин пользователя
     * @return список аккаунтов
     */
    List<Account> getAllMyAccounts(String tenantCode, Long clientId, String userLogin);
}
