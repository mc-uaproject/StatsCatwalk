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
        description = "Server statistics summary containing aggregated data",
        properties = {
                @ApiProperty(
                        name = "summary",
                        type = "object",
                        description = "Map containing various server statistics and metrics",
                        required = true,
                        example = "{\"totalPlayers\": 150, \"averagePlaytime\": 45.5, \"peakOnline\": 89}"
                )
        }
)
public class StatsSummaryResponse {
    
    @JsonProperty("summary")
    private Map<String, Object> summary;
}