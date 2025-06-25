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

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonSchema
@JsonIgnoreProperties(ignoreUnknown = true)
@ApiSchema(
        description = "Top player data entry with statistics",
        properties = {
                @ApiProperty(
                        name = "name",
                        type = "string",
                        description = "Player's username",
                        required = true,
                        example = "ikeepcalm"
                ),
                @ApiProperty(
                        name = "uuid",
                        type = "string",
                        description = "Player's UUID",
                        required = true,
                        example = "123e4567-e89b-12d3-a456-426614174000"
                ),
                @ApiProperty(
                        name = "playtime",
                        type = "long",
                        description = "Total playtime in milliseconds",
                        required = true,
                        example = "12450000"
                ),
                @ApiProperty(
                        name = "online",
                        type = "boolean",
                        description = "Whether the player is currently online",
                        required = true,
                        example = "true"
                ),
                @ApiProperty(
                        name = "level",
                        type = "integer",
                        description = "Player's current level (only if online)",
                        required = false,
                        example = "30"
                ),
                @ApiProperty(
                        name = "health",
                        type = "double",
                        description = "Player's current health (only if online)",
                        required = false,
                        example = "20.0"
                )
        }
)
public class TopPlayerData {
    
    @JsonProperty("name")
    private String name;
    
    @JsonProperty("uuid")
    private String uuid;
    
    @JsonProperty("playtime")
    private Long playtime;
    
    @JsonProperty("online")
    private Boolean online;
    
    @JsonProperty("level")
    private Integer level;
    
    @JsonProperty("health")
    private Double health;
}