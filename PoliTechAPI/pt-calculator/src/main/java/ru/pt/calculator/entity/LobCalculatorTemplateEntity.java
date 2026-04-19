package ru.pt.calculator.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "pt_lob_calculators")
public class LobCalculatorTemplateEntity {

    @Id
    @Column(name = "id", nullable = false)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "pt_seq")
    @SequenceGenerator(name = "pt_seq", sequenceName = "pt_seq", allocationSize = 1)
    private Long id;

    @Column(name = "tid", nullable = false)
    private Long tId;

    @Column(name = "lob_code", nullable = false, length = 30)
    private String lobCode;

    @Column(name = "calculator_name", nullable = false, length = 300)
    private String calculatorName;

    @Column(name = "calculator_id", nullable = false)
    private Long calculatorId;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "calculator_formula_json", columnDefinition = "jsonb", nullable = false)
    private String calculatorFormulaJson;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "calculator_json", columnDefinition = "jsonb", nullable = false)
    private String calculatorJson;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getTId() {
        return tId;
    }

    public void setTId(Long tId) {
        this.tId = tId;
    }

    public String getLobCode() {
        return lobCode;
    }

    public void setLobCode(String lobCode) {
        this.lobCode = lobCode;
    }

    public String getCalculatorName() {
        return calculatorName;
    }

    public void setCalculatorName(String calculatorName) {
        this.calculatorName = calculatorName;
    }

    public Long getCalculatorId() {
        return calculatorId;
    }

    public void setCalculatorId(Long calculatorId) {
        this.calculatorId = calculatorId;
    }

    public String getCalculatorFormulaJson() {
        return calculatorFormulaJson;
    }

    public void setCalculatorFormulaJson(String calculatorFormulaJson) {
        this.calculatorFormulaJson = calculatorFormulaJson;
    }

    public String getCalculatorJson() {
        return calculatorJson;
    }

    public void setCalculatorJson(String calculatorJson) {
        this.calculatorJson = calculatorJson;
    }
}
