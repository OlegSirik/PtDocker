package ru.pt.product.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;

@Entity
@Table(name = "pt_policy_forms")
public class PolicyFormEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "pt_seq")
    @SequenceGenerator(name = "pt_seq", sequenceName = "pt_seq", allocationSize = 1)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "tid", nullable = false)
    private Long tid;

    @Column(name = "page_id", nullable = false, length = 80)
    private String pageId;

    @Column(name = "title", nullable = false, length = 250)
    private String title;

    @Column(name = "io_type", length = 30)
    private String ioType;

    @Column(name = "record_status", nullable = false, length = 30)
    private String recordStatus = "ACTIVE";

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

    public String getPageId() {
        return pageId;
    }

    public void setPageId(String pageId) {
        this.pageId = pageId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getIoType() {
        return ioType;
    }

    public void setIoType(String ioType) {
        this.ioType = ioType;
    }

    public String getRecordStatus() {
        return recordStatus;
    }

    public void setRecordStatus(String recordStatus) {
        this.recordStatus = recordStatus;
    }
}
