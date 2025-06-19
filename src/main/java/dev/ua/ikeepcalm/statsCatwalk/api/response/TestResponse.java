package dev.ua.ikeepcalm.statsCatwalk.api.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TestResponse {
    private Map<String, Object> received;
    private Long timestamp;
    private String message;
}