package ru.pt.db.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;

import java.io.Serializable;
import java.util.Objects;

@Entity
@Table(name = "pt_refdicts")
@IdClass(RefDictEntity.RefDictId.class)
public class RefDictEntity {

    @Id
    @Column(name = "tid", nullable = false)
    private Long tid;

    @Id
    @Column(name = "code", nullable = false, length = 50)
    private String code;

    @Column(name = "name", nullable = false, length = 300)
    private String name;

    public Long getTid() {
        return tid;
    }

    public void setTid(Long tid) {
        this.tid = tid;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public static class RefDictId implements Serializable {
        private Long tid;
        private String code;

        public RefDictId() {
        }

        public RefDictId(Long tid, String code) {
            this.tid = tid;
            this.code = code;
        }

        public Long getTid() {
            return tid;
        }

        public void setTid(Long tid) {
            this.tid = tid;
        }

        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            RefDictId refDictId = (RefDictId) o;
            return Objects.equals(tid, refDictId.tid) && Objects.equals(code, refDictId.code);
        }

        @Override
        public int hashCode() {
            return Objects.hash(tid, code);
        }
    }
}
