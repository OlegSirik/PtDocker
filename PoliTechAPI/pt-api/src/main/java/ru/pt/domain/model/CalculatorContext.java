package ru.pt.domain.model;

public interface CalculatorContext extends VariableContext  {
    Object put(String key, Object value);
    
}
