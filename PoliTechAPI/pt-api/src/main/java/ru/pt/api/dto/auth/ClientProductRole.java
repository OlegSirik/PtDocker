package ru.pt.api.dto.auth;

/*
Настрйока связки продукт-клиент
будет расширятся потом
*/
public record ClientProductRole (
  Long id,
  Long productId,
  String productName,
  Boolean isDeleted )
 {}
