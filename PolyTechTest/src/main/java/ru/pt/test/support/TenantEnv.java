package ru.pt.test.support;

/** Ephemeral tenant created for e2e / admin-lifecycle. */
public record TenantEnv(
        String code,
        String userLogin,
        String userPassword,
        long defaultClientId,
        Long clientAccountId,
        Long defaultAccountId,
        /** Account id for TNT_ADMIN from /auth/me (admin setup, product grants). */
        Long tntAdminAccountId,
        /** Account id for PRODUCT_ADMIN from /auth/me (sales / load). */
        Long productAdminAccountId
) {
}
