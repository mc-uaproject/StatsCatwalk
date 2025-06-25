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

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonSchema
@JsonIgnoreProperties(ignoreUnknown = true)
@ApiSchema(
        description = "Top players response containing ranked player statistics",
        properties = {
                @ApiProperty(
                        name = "players",
                        type = "array",
                        description = "List of top players sorted by playtime or other metrics",
                        required = true,
                        example = "[{\"nickname\": \"ikeepcalm\", \"playtime\": 12450, \"rank\": 1, \"lastSeen\": \"2024-01-01T12:00:00Z\"}]"
                )
        }
)
public class TopPlayersResponse {
    
    @JsonProperty("players")
    private List<Map<String, Object>> players;
}