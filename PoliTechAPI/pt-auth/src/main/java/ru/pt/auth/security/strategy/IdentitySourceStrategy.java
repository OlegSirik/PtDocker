package ru.pt.auth.security.strategy;

import jakarta.servlet.http.HttpServletRequest;
import ru.pt.auth.model.AuthType;


public interface IdentitySourceStrategy {

    boolean supports(AuthType type);

    void resolveIdentity (HttpServletRequest request);
}

