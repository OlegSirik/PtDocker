    package ru.pt.api.dto.auth;

    public record PrincipalAccount(
        Long id,
        String role,
        String name
    ) {}
        
