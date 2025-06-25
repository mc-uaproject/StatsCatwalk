package dev.ua.ikeepcalm.statsCatwalk.api.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import dev.ua.uaproject.catwalk.bridge.annotations.ApiProperty;
import dev.ua.uaproject.catwalk.bridge.annotations.ApiSchema;
import io.javalin.openapi.JsonSchema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonSchema
@JsonIgnoreProperties(ignoreUnknown = true)
@ApiSchema(
        description = "Test response for API endpoint validation and debugging",
        properties = {
                @ApiProperty(
                        name = "received",
                        type = "object",
                        description = "Echo of the request data that was received",
                        required = true,
                        example = "{\"testParam\": \"testValue\", \"number\": 42}"
                ),
                @ApiProperty(
                        name = "timestamp",
                        type = "integer",
                        description = "Unix timestamp when the request was processed",
                        required = true,
                        example = "1704067200000"
                ),
                @ApiProperty(
                        name = "message",
                        type = "string",
                        description = "Response message indicating test status",
                        required = true,
                        example = "Test endpoint processed successfully"
                )
        }
)
public class TestResponse {
    
    @JsonProperty("received")
    private Map<String, Object> received;
    
    @JsonProperty("timestamp")
    private Long timestamp;
    
    @JsonProperty("message")
    private String message;
}