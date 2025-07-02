package com.insightpulse.InsightPulse.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
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
You are an expert productivity coach providing a requested task management service. I want your  help analyzing my task list to improve their productivity and time management. 
I want you to be friendly, motivating and concise about your response. Break down what i should do and be friendly
Here is the task list they've shared for analysis:

%s

Please provide a comprehensive and helpful analysis with the following insights:

## 1. SMART PRIORITIZATION
- **Critical (Do First)**: Tasks that are both urgent and important, blocking other work, or have hard deadlines
- **Important (Schedule)**: High-impact tasks that aren't urgent but drive long-term goals
- **Quick Wins (Do Soon)**: Low-effort, high-impact tasks that can build momentum  
- **Delegate/Eliminate**: Tasks that may not need to be done by the user or at all

For each category, explain WHY each task belongs there.

## 2. WORKLOAD & CAPACITY ANALYSIS
- Estimate total time needed for all tasks
- Identify potential bottlenecks or dependencies between tasks
- Assess if the workload is realistic for the timeframe
- Flag any signs of overcommitment or unrealistic expectations

## 3. PATTERNS & INSIGHTS
- Identify recurring themes or types of work
- Spot tasks that could be batched together for efficiency
- Notice any missing tasks that might be implied by the current list
- Highlight any tasks that seem vague and need clarification

## 4. STRATEGIC RECOMMENDATIONS
- Suggest an optimal order for tackling tasks
- Recommend time-blocking strategies
- Identify tasks that could be automated, templated, or systemized
- Propose delegation opportunities

## 5. ENERGY & FOCUS OPTIMIZATION
- Categorize tasks by energy level required (High focus/Low focus)
- Suggest which tasks to do when (morning brain work vs. afternoon admin)
- Identify tasks that could be done in similar contexts or locations

## 6. PERSONALIZED MOTIVATION & MINDSET
Based on the task list, provide:
- Recognition of what they're trying to accomplish
- Encouragement tailored to their specific situation  
- A reframe of overwhelming aspects into manageable steps
- Celebration of progress they're likely making

## 7. NEXT ACTIONS
Provide 3 specific, actionable next steps they should take in the next 24 hours to make meaningful progress.

Be specific, actionable, and supportive in your analysis. Focus on helping them work smarter, not just harder.
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

}
