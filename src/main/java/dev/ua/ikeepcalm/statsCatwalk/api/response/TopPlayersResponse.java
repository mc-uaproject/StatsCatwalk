package dev.ua.ikeepcalm.statsCatwalk.api.response;

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
public class TopPlayersResponse {
    private List<Map<String, Object>> players;
}