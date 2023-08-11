package com.publicissapient.kpidashboard.apis.pschat.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class ChatMessageDTO {

    @JsonProperty("id")
    private String id;

    @JsonProperty("role")
    private String role;

    @JsonProperty("content")
    private String content;
}
