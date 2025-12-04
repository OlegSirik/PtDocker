package ru.pt.api.dto.auth;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Setter
@Getter
@NoArgsConstructor
public class Account {
    private Long id;
    private Long tid;
    private Long clientId;
    private Long parentId;
    private String nodeType;
    private String name;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
