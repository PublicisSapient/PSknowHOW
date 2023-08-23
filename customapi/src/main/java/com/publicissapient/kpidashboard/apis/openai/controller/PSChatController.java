package com.publicissapient.kpidashboard.apis.openai.controller;

import com.publicissapient.kpidashboard.apis.model.ServiceResponse;
import com.publicissapient.kpidashboard.apis.openai.dto.enums.AssistantType;
import com.publicissapient.kpidashboard.apis.openai.dto.enums.GPTModel;
import com.publicissapient.kpidashboard.apis.openai.model.ChatDTO;
import com.publicissapient.kpidashboard.apis.openai.model.PromptRequest;
import com.publicissapient.kpidashboard.apis.openai.service.PsChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;


@RestController
@RequestMapping("/chats")
public class PSChatController {
    @Autowired
    PsChatService psChatService;

    @GetMapping("/{chatId}")
    public ResponseEntity<ChatDTO> getChat(@PathVariable String chatId) {
        try {
            ChatDTO chat = psChatService.getChat(chatId);
            return ResponseEntity.ok(chat);
        } catch (HttpClientErrorException e) {
            return ResponseEntity.status(e.getStatusCode()).build();
        }
    }

    @PostMapping("/prompt")
    public ResponseEntity<ChatDTO> sendPrompt(@RequestBody PromptRequest promptRequest) {
        try {
            ChatDTO chat = psChatService.sendPrompt(promptRequest);
            return ResponseEntity.ok(chat);
        } catch (HttpClientErrorException e) {
            return ResponseEntity.status(e.getStatusCode()).build();
        }
    }

    @PostMapping("/getChat")
    public String getRecommendationForIteration() {

        StringBuilder promptBuilder = new StringBuilder();

        promptBuilder.append("What is a anime?");

        return psChatService.getRecommendationForPrompt(GPTModel.GPT_35_TURBO,
                AssistantType.DEFAULT_ASSISTANT, promptBuilder.toString());
    }
    @PostMapping("/getChat/{sprintId}")
    public ServiceResponse getRecommendationForIteration(@PathVariable String sprintId) {

        ServiceResponse a =  psChatService.getIterationPrompt(GPTModel.GPT_35_TURBO,
                AssistantType.PS_ASSISTANT, sprintId);

        return a;
    }
}
