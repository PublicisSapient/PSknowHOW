package com.publicissapient.kpidashboard.apis.openai.dto;

import lombok.Data;

@Data
public class PostChatResponseDTO {

    private ChatDataDTO data;

    private String message;

}
