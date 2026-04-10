package ru.pt.numbers.entity;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "pt_number_generators")
public class NumberGeneratorEntity {

    @Id
    @Column(name = "id", nullable = false)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "pt_seq")
    @SequenceGenerator(name = "pt_seq", sequenceName = "pt_seq", allocationSize = 1)
    private Long id;

    @Column(name = "tid", nullable = false)
    private Long tid;

//    @Column(name = "code", nullable = false, length = 100)
//    private String code;

    @Column(name = "current_value", nullable = false)
    private Long currentValue = 0L;

    @Column(name = "last_reset", nullable = false)
    private LocalDate lastReset = LocalDate.now();


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getTid() {
        return tid;
    }

    public void setTid(Long tid) {
        this.tid = tid;
    }

    public Long getCurrentValue() {
        return currentValue;
    }

    public void setCurrentValue(Long currentValue) {
        this.currentValue = currentValue;
    }

//    public String getCode() {
//        return code;
//    }

//    public void setCode(String code) {
//        this.code = code;
//    }

    public LocalDate getLastReset() {
        return lastReset;
    }

    public void setLastReset(LocalDate lastReset) {
        this.lastReset = lastReset;
    }
}