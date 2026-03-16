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
}

