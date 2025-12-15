package ru.pt.api.dto.product;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class PvPackage {

    @JsonProperty("code")
    private Integer code;

    @JsonProperty("name")
    private String name;

    @JsonProperty("covers")
    private List<PvCover> covers;

    @JsonProperty("files")
    private List<PvFile> files;

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<PvCover> getCovers() {
        return covers;
    }

    public void setCovers(List<PvCover> covers) {
        this.covers = covers;
    }

    public List<PvFile> getFiles() {
        return files;
    }

    public void setFiles(List<PvFile> files) {
        this.files = files;
    }
}