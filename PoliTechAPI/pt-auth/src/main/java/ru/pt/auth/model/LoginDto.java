package ru.pt.auth.model;

public record LoginDto(
    Long id,
    Long clientId,
    String clientCode,
    String userLogin,
    String fullName,
    String position
) {}
