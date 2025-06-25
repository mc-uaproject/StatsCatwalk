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
        description = "Hourly distribution response showing player activity patterns",
        properties = {
                @ApiProperty(
                        name = "hourly_distribution",
                        type = "object",
                        description = "Player count distribution by hour of day (0-23 hours)",
                        required = true,
                        example = "{\"0\": 12, \"6\": 25, \"12\": 67, \"18\": 89, \"23\": 34}"
                )
        }
)
public class HourlyDistributionResponse {
    
    @JsonProperty("hourly_distribution")
    private Map<String, Integer> hourlyDistribution;
}