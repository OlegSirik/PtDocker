package ru.pt.api.dto.marketplace;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Response for GET /integrations/mp/owner
 */
public class MpOwnerResponse {

    @JsonProperty("legal_data")
    private MpLegalData legalData;

    @JsonProperty("logo")
    private String logo;

    @JsonProperty("title")
    private String title;

    public MpOwnerResponse() {
    }

    public MpLegalData getLegalData() {
        return legalData;
    }

    public void setLegalData(MpLegalData legalData) {
        this.legalData = legalData;
    }

    public String getLogo() {
        return logo;
    }

    public void setLogo(String logo) {
        this.logo = logo;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
