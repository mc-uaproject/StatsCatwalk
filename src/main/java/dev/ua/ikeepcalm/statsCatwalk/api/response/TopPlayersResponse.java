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
                        example = "[{\"name\": \"ikeepcalm\", \"uuid\": \"123e4567-e89b-12d3-a456-426614174000\", \"playtime\": 12450000, \"online\": true, \"level\": 30, \"health\": 20.0}]"
                )
        }
)
public class TopPlayersResponse {

    @JsonProperty("players")
    private List<TopPlayerData> players;
}