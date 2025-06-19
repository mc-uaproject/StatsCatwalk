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
public class HourlyDistributionResponse {
    private Map<String, Integer> hourlyDistribution;
}