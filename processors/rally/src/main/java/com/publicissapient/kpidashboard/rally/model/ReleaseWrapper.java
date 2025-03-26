package com.publicissapient.kpidashboard.rally.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ReleaseWrapper {
    @JsonProperty("Release")
    private Release release;
}
