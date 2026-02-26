package ru.pt.api.dto.marketplace;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Legal data for marketplace page owner.
 */
public class MpLegalData {

    @JsonProperty("fullName")
    private String fullName;

    @JsonProperty("address")
    private String address;

    @JsonProperty("inn")
    private String inn;

    @JsonProperty("licenseInfo")
    private String licenseInfo;

    @JsonProperty("ogrn")
    private String ogrn;

    @JsonProperty("phone")
    private String phone;

    @JsonProperty("email")
    private String email;

    @JsonProperty("phoneCs")
    private String phoneCs;

    @JsonProperty("webSite")
    private String webSite;

    public MpLegalData() {
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getInn() {
        return inn;
    }

    public void setInn(String inn) {
        this.inn = inn;
    }

    public String getLicenseInfo() {
        return licenseInfo;
    }

    public void setLicenseInfo(String licenseInfo) {
        this.licenseInfo = licenseInfo;
    }

    public String getOgrn() {
        return ogrn;
    }

    public void setOgrn(String ogrn) {
        this.ogrn = ogrn;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhoneCs() {
        return phoneCs;
    }

    public void setPhoneCs(String phoneCs) {
        this.phoneCs = phoneCs;
    }

    public String getWebSite() {
        return webSite;
    }

    public void setWebSite(String webSite) {
        this.webSite = webSite;
    }
}
