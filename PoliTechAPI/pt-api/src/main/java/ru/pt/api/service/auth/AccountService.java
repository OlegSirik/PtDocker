package ru.pt.api.service.auth;

import ru.pt.api.dto.auth.Account;
import ru.pt.api.dto.auth.ProductRole;
import java.util.List;

/**
 * Account management service interface.
 * All methods include authorization checks.
 */
public interface AccountService {

    /**
     * Получить учетную запись по идентификатору
     * @param id идентификатор аккаунта
     * @return учетная запись
     */
    Account getAccountById(Long id);

    /**
     * Создать группу внутри клиента
     * @param name название группы
     * @param parentId идентификатор родителя
     * @return созданная запись
     */
    Account createGroup(String name, Long parentId);

    List<Account> getGroups(Long parentId);

    /**
     * Создать рабочий аккаунт
     * @param name имя аккаунта
     * @param parentId идентификатор группы
     * @return созданная запись
     */
    Account createAccount(String name, Long parentId);

    List<Account> getAccounts(Long parentId);

    /**
     * Get child accounts, optionally filtered by node type.
     * @param parentId parent account id
     * @param nodeType optional filter (e.g. GROUP, ACCOUNT, SUB)
     * @return list of child accounts
     */
    List<Account> getChildren(Long parentId, String nodeType);

    /**
     * Create a child account of the given type under the parent.
     * @param name child account name
     * @param nodeType type (e.g. GROUP, ACCOUNT, SUB)
     * @param parentId parent account id
     * @return created account
     */
    Account createChild(String name, String nodeType, Long parentId);

    /**
     * Создать субаккаунт
     * @param name имя субаккаунта
     * @param parentId идентификатор родительского аккаунта
     * @return созданная запись
     */
    Account createSubaccount(String name, Long parentId);

    List<Account> getSubaccounts(Long parentId);

    /**
     * Получить все аккаунты для текущего пользователя
     * @param tenantCode код тенанта
     * @param clientId код клиента
     * @param userLogin логин пользователя
     * @return список аккаунтов
     */
    List<Account> getAllMyAccounts(String tenantCode, Long clientId, String userLogin);

    /**
     * Получить роль продукта для аккаунта
     * @param accountId идентификатор аккаунта
     * @param productId идентификатор продукта
     * @return роль продукта
     */
    ProductRole getProductRole(Long accountId, Long productId);

    /**
     * Назначить/обновить роли продукта аккаунту
     * @param accountId идентификатор аккаунта
     * @param productRole описание прав по продукту
     * @return обновленная запись роли
     */
    ProductRole grantProduct(Long accountId, ProductRole productRole);

    /**
     * Отозвать роль продукта у аккаунта
     * @param accountId идентификатор аккаунта
     * @param productId идентификатор продукта
     */
    void revokeProduct(Long accountId, Long productId);

    /**
     * Assign product role to account (TNT_ADMIN / GROUP_ADMIN).
     * @param accountId account to assign role to
     * @param roleProductId product id
     * @param roleAccountId role account id
     * @return created product role
     */
    ProductRole assignProductRole(Long accountId, Long roleProductId, Long roleAccountId,
                                  Boolean canRead, Boolean canQuote, Boolean canPolicy,
                                  Boolean canAddendum, Boolean canCancel, Boolean canProlong);

    /**
     * Revoke product role by id (TNT_ADMIN / GROUP_ADMIN).
     * @param productRoleId product role id to revoke
     */
    void revokeProductRole(Long productRoleId);

    /**
     * Update product role permissions (TNT_ADMIN / GROUP_ADMIN).
     * @param productRoleId product role id
     * @return updated product role
     */
    ProductRole updateProductRole(Long productRoleId, Boolean canRead, Boolean canQuote,
                                 Boolean canPolicy, Boolean canAddendum, Boolean canCancel,
                                 Boolean canProlong);

    /**
     * Получить все продуктовые роли для аккаунта
     * @param accountId идентификатор аккаунта
     * @return список ролей продукта
     */
    List<ProductRole> getProductRolesByAccountId(Long accountId);

    /**
     * Получить путь от acting account до указанного аккаунта
     * @param accountId идентификатор аккаунта
     * @return список аккаунтов от acting account до указанного
     */
    List<Account> getPathToRoot(Long accountId);

    /**
     * Найти дочерний «ролевой» аккаунт заданного типа под родителем (первый подходящий).
     * Обычно для узлов администраторов (например {@code GROUP_ADMIN}).
     *
     * @param accountNodeType имя типа узла ({@code AccountNodeType#name()})
     * @param parentId идентификатор родительского аккаунта
     * @return найденный аккаунт или {@code null}
     */
    Account getRoleAccount(String accountNodeType, Long parentId);
}
