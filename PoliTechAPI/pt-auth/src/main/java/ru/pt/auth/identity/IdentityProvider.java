package ru.pt.auth.identity;

import ru.pt.auth.entity.ClientEntity;
import ru.pt.auth.entity.LoginEntity;
import ru.pt.auth.entity.TenantEntity;
import ru.pt.auth.model.AuthType;

/**
 * Провайдер внешней/внутренней идентичности для конкретного AuthType.
 * Используется при создании клиентов и логинов, чтобы синхронизировать их с IdP.
 */
public interface IdentityProvider {

    AuthType supportedAuthType();

    /**
     * Создать клиента в IdP (если требуется).
     * Может быть no-op для некоторых AuthType.
     */
    void createClient(TenantEntity tenant, ClientEntity client);

    /**
     * Создать пользователя (login) в IdP (если требуется).
     * Может быть no-op для некоторых AuthType.
     */
    void createUser(TenantEntity tenant, LoginEntity login);

    /**
     * Установить пароль пользователя в IdP (если требуется), например после смены пароля в БД.
     * По умолчанию — no-op для AuthType без внешнего IdP.
     *
     * @param newPassword пароль в открытом виде (как ввёл администратор)
     * @param temporary если true — IdP может потребовать смену пароля при следующем входе
     */
    default void setUserPassword(TenantEntity tenant, LoginEntity login, String newPassword, boolean temporary) {
        // no-op
    }
}

