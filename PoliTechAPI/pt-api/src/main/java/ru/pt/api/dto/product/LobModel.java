package ru.pt.api.dto.product;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.List;

/**
 * Линия бизнеса - общие параметры для какого-то типа продуктов
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class LobModel {

    @JsonProperty("id")
    private Long id;

    @JsonProperty("mpCode")
    private String mpCode;

    @JsonProperty("mpName")
    private String mpName;

    @JsonProperty("mpVars")
    private List<LobVar> mpVars;

    @JsonProperty("mpCovers")
    private List<LobCover> mpCovers;

    @JsonProperty("mpPhType")
    private String mpPhType;

    @JsonProperty("mpInsObjectType")
    private String mpInsObjectType;

    @JsonProperty("mpFiles")
    private List<LobFile> mpFiles;

}
