package ru.pt.api.dto.addon;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import ru.pt.api.dto.refs.RecordStatus;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProviderDto {
    private Long id;
    private String name;
    private RecordStatus recordStatus;
    private String executionMode;  // LOCAL, API
}
