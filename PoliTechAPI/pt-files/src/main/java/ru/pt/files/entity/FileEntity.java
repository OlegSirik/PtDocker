package ru.pt.files.entity;

import java.util.UUID;

import jakarta.persistence.*;

@Entity
@Table(name = "pt_files")
public class FileEntity {

    @Id
    @Column(name = "id", nullable = false)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "pt_seq")
    @SequenceGenerator(name = "pt_seq", sequenceName = "pt_seq", allocationSize = 1)
    private Long id;

    @Column(name = "public_id")
    private String publicId;

    @Column(name = "filename", length = 512)
    private String filename;

    @Column(name = "content_type", length = 255)
    private String contentType;

    @Column(name = "size")
    private Long size;

    @Column(name = "file_body")
    private byte[] fileBody;


    @Column(name = "tid", nullable = false)
    private Long tid;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getPublicId() {
        return publicId;
    }

    public void setPublicId(String publicId) {
        this.publicId = publicId;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public Long getSize() {
        return size;
    }

    public void setSize(Long size) {
        this.size = size;
    }

    public byte[] getFileBody() {
        return fileBody;
    }

    public void setFileBody(byte[] fileBody) {
        this.fileBody = fileBody;
    }

    public Long getTid() {
        return tid;
    }

    public void setTid(Long tid) {
        this.tid = tid;
    }
}