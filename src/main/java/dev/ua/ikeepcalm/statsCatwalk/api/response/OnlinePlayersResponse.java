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
        description = "Online players response containing historical data and hourly distribution",
        properties = {
                @ApiProperty(
                        name = "players",
                        type = "array",
                        description = "List of historical online player data entries",
                        required = true,
                        example = "[{\"timestamp\": 1704067200000, \"online\": 45, \"hour\": 12, \"day\": \"MONDAY\"}]"
                ),
                @ApiProperty(
                        name = "hourly_distribution",
                        type = "object",
                        description = "Player count distribution by hour of day (0-23)",
                        required = true,
                        example = "{\"0\": 12, \"1\": 8, \"12\": 45, \"18\": 67}"
                )
        }
)
public class OnlinePlayersResponse {
    
    @JsonProperty("players")
    private List<OnlinePlayerData> players;
    
    @JsonProperty("hourly_distribution")
    private Map<String, Integer> hourlyDistribution;
}