package ru.pt.api.dto.schema;

public record SchemaTreeDto(
    Long id,
    Long parent_id,
    Integer order_nr,
    String name,
    String code
) {
    public SchemaTreeDto(Long id, Long parent_id, Integer order_nr, String name, String code) {
        this.id = id;
        this.parent_id = parent_id;
        this.order_nr = order_nr;
        this.name = name;
        this.code = code;
    }
}
