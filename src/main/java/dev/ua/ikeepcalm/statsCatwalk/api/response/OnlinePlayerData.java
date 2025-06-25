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
        description = "Online player data entry representing a point in time",
        properties = {
                @ApiProperty(
                        name = "timestamp",
                        type = "long",
                        description = "Unix timestamp in milliseconds",
                        required = true,
                        example = "1704067200000"
                ),
                @ApiProperty(
                        name = "online",
                        type = "integer",
                        description = "Number of online players at this timestamp",
                        required = true,
                        example = "45"
                ),
                @ApiProperty(
                        name = "hour",
                        type = "integer",
                        description = "Hour of the day (0-23)",
                        required = true,
                        example = "12"
                ),
                @ApiProperty(
                        name = "day",
                        type = "string",
                        description = "Day of the week",
                        required = true,
                        example = "MONDAY"
                )
        }
)
public class OnlinePlayerData {
    
    @JsonProperty("timestamp")
    private Long timestamp;
    
    @JsonProperty("online")
    private Integer online;
    
    @JsonProperty("hour")
    private Integer hour;
    
    @JsonProperty("day")
    private String day;
}