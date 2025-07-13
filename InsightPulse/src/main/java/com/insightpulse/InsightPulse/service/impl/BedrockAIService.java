package com.insightpulse.InsightPulse.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.services.calendar.model.Event;
import com.insightpulse.InsightPulse.model.Task;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.bedrockruntime.BedrockRuntimeClient;
import software.amazon.awssdk.services.bedrockruntime.model.InvokeModelRequest;
import software.amazon.awssdk.services.bedrockruntime.model.InvokeModelResponse;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@ConfigurationProperties(prefix = "aws")
@Setter
@Getter
public class BedrockAIService {

    private String accessKey;
    private String secretKey;
    private String region;

    private BedrockRuntimeClient bedrockClient;

    @PostConstruct
    public void init() {
        AwsCredentialsProvider credentials = StaticCredentialsProvider.create(
                AwsBasicCredentials.create(accessKey, secretKey)
        );

        this.bedrockClient = BedrockRuntimeClient.builder()
                .region(Region.of(region))
                .credentialsProvider(credentials)
                .build();
    }



    public String getTaskInsight(List<Task> tasks) {
        String taskSummary = tasks.stream()
                .map(t -> "- " + t.getTaskName() + " (Due: " + t.getTaskDueDate() + ")")
                .collect(Collectors.joining("\n"));

        LocalDate today = LocalDate.now();
        String currentDate = today.format(DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy"));

        String content = """
You are an expert productivity coach providing you my tasks that i have to finish. I want your help analyzing my tasks such as schoolwork, work, job interview and etc, help me improve their productivity and time management.If there is nothing in the list then provide a nice message back. 
I want you to be friendly, motivating and concise about your response. Break down what i should do and be friendly and do not make it too wordy and dont bold or italicize your response and introduce your self as InsightPulse.
Here is the task list they've shared for analysis:

%s
""".formatted(taskSummary);

        try {
            // Build proper JSON structure
            Map<String, Object> requestMap = new HashMap<>();
            requestMap.put("anthropic_version", "bedrock-2023-05-31");
            requestMap.put("max_tokens", 300);
            requestMap.put("temperature", 0.7);

            List<Map<String, String>> messages = new ArrayList<>();
            messages.add(Map.of(
                    "role", "user",
                    "content", content
            ));

            requestMap.put("messages", messages);
            String systemPrompt = "You are Claude, an AI assistant created by Anthropic. You are an expert productivity coach. Respond as if it's" + currentDate + " and be aware of current productivity trends and tools.";
            requestMap.put("system", systemPrompt );

            ObjectMapper mapper = new ObjectMapper();
            String jsonBody = mapper.writeValueAsString(requestMap);

            InvokeModelRequest request = InvokeModelRequest.builder()
                    .modelId("anthropic.claude-v2:1")
                    .contentType("application/json")
                    .accept("application/json")
                    .body(SdkBytes.fromUtf8String(jsonBody))
                    .build();

            InvokeModelResponse response = bedrockClient.invokeModel(request);
            return response.body().asUtf8String();

        } catch (Exception e) {
            e.printStackTrace();
            return "Error generating insight";
        }
    }

    public String getEventInsight(List<String> events) {
        LocalDate today = LocalDate.now();
        String currentDate = today.format(DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy"));

        String content = """
You are an expert time management coach providing a requested google calendar event management service. I want your help analyzing my events such as meetings or classes for the next days list to improve their productivity and time management.If there is nothing in the list then provide a nice message back. 
I want you to be friendly, motivating and concise about your response. Break down what i should do and be friendly and do not make it too wordy and dont bold or italicize your response and introduce your self as InsightPulse.
Here is the task list they've shared for analysis:

%s



""".formatted(events);

        try{
            Map<String, Object> requestMap = new HashMap<>();
            requestMap.put("anthropic_version", "bedrock-2023-05-31");
            requestMap.put("max_tokens", 300);
            requestMap.put("temperature", 0.7);
            List<Map<String, String>> messages = new ArrayList<>();
            messages.add(Map.of(
                    "role", "user",
                    "content", content
            ));
            requestMap.put("messages", messages);
            String systemPrompt = "You are Claude, an AI assistant created by Anthropic. You are an expert productivity coach. Respond as if it's" + currentDate + " and be aware of current productivity trends and tools.";
            requestMap.put("system", systemPrompt );

            ObjectMapper mapper = new ObjectMapper();
            String jsonBody = mapper.writeValueAsString(requestMap);

            InvokeModelRequest model = InvokeModelRequest.builder()
                    .modelId("anthropic.claude-v2:1")
                    .contentType("application/json")
                    .accept("application/json")
                    .body(SdkBytes.fromUtf8String(jsonBody))
                    .build();
            InvokeModelResponse response = bedrockClient.invokeModel(model);
            return response.body().asUtf8String();

        }catch(Exception e){
            e.printStackTrace();
            return "Error generating insight";

        }

    }

}
