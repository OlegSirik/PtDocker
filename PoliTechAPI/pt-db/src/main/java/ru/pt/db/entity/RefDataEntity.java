package ru.pt.db.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;

import java.io.Serializable;
import java.util.Objects;

@Entity
@Table(name = "pt_refdata")
@IdClass(RefDataEntity.RefDataId.class)
public class RefDataEntity {

    @Id
    @Column(name = "tid", nullable = false)
    private Long tid;

    @Id
    @Column(name = "ref_code", nullable = false, length = 50)
    private String refCode;

    @Id
    @Column(name = "md_code", nullable = false, length = 50)
    private String mdCode;

    @Column(name = "md_name", nullable = false, length = 100)
    private String mdName;

    public Long getTid() {
        return tid;
    }

    public void setTid(Long tid) {
        this.tid = tid;
    }

    public String getRefCode() {
        return refCode;
    }

    public void setRefCode(String refCode) {
        this.refCode = refCode;
    }

    public String getMdCode() {
        return mdCode;
    }

    public void setMdCode(String mdCode) {
        this.mdCode = mdCode;
    }

    public String getMdName() {
        return mdName;
    }

    public void setMdName(String mdName) {
        this.mdName = mdName;
    }

    public static class RefDataId implements Serializable {
        private Long tid;
        private String refCode;
        private String mdCode;

        public RefDataId() {
        }

        public RefDataId(Long tid, String refCode, String mdCode) {
            this.tid = tid;
            this.refCode = refCode;
            this.mdCode = mdCode;
        }

        public Long getTid() {
            return tid;
        }

        public void setTid(Long tid) {
            this.tid = tid;
        }

        public String getRefCode() {
            return refCode;
        }

        public void setRefCode(String refCode) {
            this.refCode = refCode;
        }

        public String getMdCode() {
            return mdCode;
        }

        public void setMdCode(String mdCode) {
            this.mdCode = mdCode;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            RefDataId refDataId = (RefDataId) o;
            return Objects.equals(tid, refDataId.tid)
                    && Objects.equals(refCode, refDataId.refCode)
                    && Objects.equals(mdCode, refDataId.mdCode);
        }

        @Override
        public int hashCode() {
            return Objects.hash(tid, refCode, mdCode);
        }
    }
}
