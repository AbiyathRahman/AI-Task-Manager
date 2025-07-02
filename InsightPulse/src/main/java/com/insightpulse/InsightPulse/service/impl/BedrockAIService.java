package com.insightpulse.InsightPulse.service.impl;

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

import java.util.List;
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

        String prompt = """
            Given this list of tasks:
            %s

            Suggest priority levels (High/Medium/Low) and give a brief motivational message or warning if the list is overloaded.
            Output in plain text.
            """.formatted(taskSummary);

        InvokeModelRequest request = InvokeModelRequest.builder()
                .modelId("anthropic.claude-v2") // or titan-lite if using Titan
                .contentType("application/json")
                .accept("application/json")
                .body(SdkBytes.fromUtf8String("""
            {
              "prompt": "%s\\n\\nAssistant:".formatted(prompt),
              "max_tokens_to_sample": 300,
              "temperature": 0.7,
              "stop_sequences": ["\\n\\nHuman:"]
            }
            """))
                .build();

        InvokeModelResponse response = bedrockClient.invokeModel(request);
        return response.body().asUtf8String();
    }
}
