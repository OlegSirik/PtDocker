package ru.pt.api.dto.schema;

public record SchemaTreeDto(
    Long id,
    Long parent_id,
    Integer order_nr,
    String code,
    String name,
    String varType,
    String varDataType,
    String varList,
    boolean isSystem,
    String varPath,
    String varCdm,
    String varValue
) {}
