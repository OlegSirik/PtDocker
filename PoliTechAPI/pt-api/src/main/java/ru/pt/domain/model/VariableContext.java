package ru.pt.domain.model;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface VariableContext  
{

    Object get(Object key);

    BigDecimal getDecimal(String code);

    String getString(String code);

    PvVarDefinition getDefinition(String code);

    Map<String, Object> getValues();

    void putDefinition(PvVarDefinition def);

    List<PvVarDefinition> getDefinitions();
}