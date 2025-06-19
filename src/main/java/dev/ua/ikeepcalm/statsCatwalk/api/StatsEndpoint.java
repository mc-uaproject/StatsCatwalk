package dev.ua.ikeepcalm.statsCatwalk.api;

import dev.ua.ikeepcalm.statsCatwalk.StatsCatwalk;
import dev.ua.ikeepcalm.statsCatwalk.api.response.*;
import dev.ua.ikeepcalm.statsCatwalk.manager.StatsManager;
import dev.ua.uaproject.catwalk.bridge.annotations.BridgeEventHandler;
import dev.ua.uaproject.catwalk.bridge.annotations.BridgeQueryParam;
import dev.ua.uaproject.catwalk.bridge.annotations.BridgeRequestBody;
import dev.ua.uaproject.catwalk.bridge.source.BridgeApiResponse;
import io.javalin.http.HttpStatus;
import io.javalin.openapi.*;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class StatsEndpoint {

    private final StatsManager statsManager;

    public StatsEndpoint(StatsManager statsManager) {
        this.statsManager = statsManager;
    }

    @OpenApi(
            path = "/stats/summary",
            methods = HttpMethod.GET,
            summary = "Get server statistics summary",
            description = "Returns overall server statistics including player counts, TPS, and playtime averages",
            tags = {"stats"},
            responses = {
                    @OpenApiResponse(status = "200", description = "Successfully retrieved stats summary", content = @OpenApiContent(from = ApiResponse.class)),
                    @OpenApiResponse(status = "500", description = "Internal server error", content = @OpenApiContent(from = ApiResponse.class))
            }
    )
    @BridgeEventHandler(requiresAuth = false, description = "Returns server statistics summary", logRequests = true, scopes = {"stats"})
    public CompletableFuture<BridgeApiResponse<StatsSummaryResponse>> getStatsSummary() {
        try {
            Map<String, Object> summary = statsManager.getStatsSummary();
            return CompletableFuture.completedFuture(
                    BridgeApiResponse.success(new StatsSummaryResponse(summary))
            );
        } catch (Exception e) {
            logError("Failed to get stats summary", e);
            return CompletableFuture.completedFuture(
                    BridgeApiResponse.error("Failed to retrieve stats summary", HttpStatus.INTERNAL_SERVER_ERROR)
            );
        }
    }

    @OpenApi(
            path = "/stats/online",
            methods = HttpMethod.GET,
            summary = "Get online players data for the past week",
            description = "Returns historical online player data and current hourly distribution",
            tags = {"stats"},
            queryParams = {
                    @OpenApiParam(name = "days", type = Integer.class, description = "Number of days of data to return (max 14, default 7)")
            },
            responses = {
                    @OpenApiResponse(status = "200", description = "Successfully retrieved online players data", content = @OpenApiContent(from = ApiResponse.class)),
                    @OpenApiResponse(status = "400", description = "Invalid days parameter", content = @OpenApiContent(from = ApiResponse.class)),
                    @OpenApiResponse(status = "500", description = "Internal server error", content = @OpenApiContent(from = ApiResponse.class))
            }
    )
    @BridgeEventHandler(requiresAuth = false, description = "Returns online players historical data", logRequests = true, scopes = {"stats"})
    public CompletableFuture<BridgeApiResponse<OnlinePlayersResponse>> getOnlinePlayersData(@BridgeQueryParam("days") String daysParam) {
        int days = 7;
        
        if (daysParam != null) {
            try {
                days = Integer.parseInt(daysParam);
                if (days > 14) days = 14;
                if (days < 1) days = 1;
            } catch (NumberFormatException e) {
                return CompletableFuture.completedFuture(
                        BridgeApiResponse.error("Invalid days parameter", HttpStatus.BAD_REQUEST)
                );
            }
        }

        try {
            List<Map<String, Object>> playersData = statsManager.getOnlinePlayersData(days);
            Map<String, Integer> hourlyDistribution = statsManager.getCurrentHourlyDistribution();

            OnlinePlayersResponse response = new OnlinePlayersResponse(playersData, hourlyDistribution);
            return CompletableFuture.completedFuture(BridgeApiResponse.success(response));
        } catch (Exception e) {
            logError("Failed to get online players data", e);
            return CompletableFuture.completedFuture(
                    BridgeApiResponse.error("Failed to retrieve online players data", HttpStatus.INTERNAL_SERVER_ERROR)
            );
        }
    }

    @OpenApi(
            path = "/stats/topplayers",
            methods = HttpMethod.GET,
            summary = "Get most active players by playtime",
            description = "Returns a list of top players sorted by total playtime",
            tags = {"stats"},
            queryParams = {
                    @OpenApiParam(name = "limit", type = Integer.class, description = "Number of players to return (default 10)")
            },
            responses = {
                    @OpenApiResponse(status = "200", description = "Successfully retrieved top players", content = @OpenApiContent(from = ApiResponse.class)),
                    @OpenApiResponse(status = "400", description = "Invalid limit parameter", content = @OpenApiContent(from = ApiResponse.class)),
                    @OpenApiResponse(status = "500", description = "Internal server error", content = @OpenApiContent(from = ApiResponse.class))
            }
    )
    @BridgeEventHandler(requiresAuth = false, description = "Returns top players by playtime", logRequests = true, scopes = {"stats"})
    public CompletableFuture<BridgeApiResponse<TopPlayersResponse>> getTopPlayers(@BridgeQueryParam("limit") String limitParam) {
        int limit = 10;
        
        if (limitParam != null) {
            try {
                limit = Integer.parseInt(limitParam);
                if (limit < 1) limit = 1;
            } catch (NumberFormatException e) {
                return CompletableFuture.completedFuture(
                        BridgeApiResponse.error("Invalid limit parameter", HttpStatus.BAD_REQUEST)
                );
            }
        }

        try {
            List<Map<String, Object>> result = statsManager.getTopPlayers(limit);
            return CompletableFuture.completedFuture(
                    BridgeApiResponse.success(new TopPlayersResponse(result))
            );
        } catch (Exception e) {
            logError("Failed to get top players", e);
            return CompletableFuture.completedFuture(
                    BridgeApiResponse.error("Failed to retrieve top players", HttpStatus.INTERNAL_SERVER_ERROR)
            );
        }
    }

    @OpenApi(
            path = "/stats/hourly",
            methods = HttpMethod.GET,
            summary = "Get current hourly player distribution",
            description = "Returns player count distribution across all hours for today",
            tags = {"stats"},
            responses = {
                    @OpenApiResponse(status = "200", description = "Successfully retrieved hourly distribution", content = @OpenApiContent(from = ApiResponse.class)),
                    @OpenApiResponse(status = "500", description = "Internal server error", content = @OpenApiContent(from = ApiResponse.class))
            }
    )
    @BridgeEventHandler(requiresAuth = false, description = "Returns current hourly player distribution", logRequests = true, scopes = {"stats"})
    public CompletableFuture<BridgeApiResponse<HourlyDistributionResponse>> getHourlyDistribution() {
        try {
            Map<String, Integer> hourlyDistribution = statsManager.getCurrentHourlyDistribution();
            return CompletableFuture.completedFuture(
                    BridgeApiResponse.success(new HourlyDistributionResponse(hourlyDistribution))
            );
        } catch (Exception e) {
            logError("Failed to get hourly distribution", e);
            return CompletableFuture.completedFuture(
                    BridgeApiResponse.error("Failed to retrieve hourly distribution", HttpStatus.INTERNAL_SERVER_ERROR)
            );
        }
    }

    @OpenApi(
            path = "/stats/test",
            methods = HttpMethod.POST,
            summary = "Test endpoint for POST requests with body",
            description = "Test endpoint to verify POST functionality with JSON body processing",
            tags = {"stats"},
            requestBody = @OpenApiRequestBody(
                    description = "Test data to process",
                    required = true,
                    content = @OpenApiContent(
                            type = "application/json",
                            example = """
                                    {
                                        "message": "Hello from client",
                                        "data": {
                                            "key": "value",
                                            "number": 42
                                        }
                                    }
                                    """
                    )
            ),
            responses = {
                    @OpenApiResponse(status = "200", description = "Successfully processed test request", content = @OpenApiContent(from = ApiResponse.class)),
                    @OpenApiResponse(status = "400", description = "Invalid request body", content = @OpenApiContent(from = ApiResponse.class)),
                    @OpenApiResponse(status = "500", description = "Internal server error", content = @OpenApiContent(from = ApiResponse.class))
            }
    )
    @BridgeEventHandler(requiresAuth = false, description = "Test POST endpoint", logRequests = true, scopes = {"stats"})
    public CompletableFuture<BridgeApiResponse<TestResponse>> testPostEndpoint(@BridgeRequestBody Map<String, Object> requestBody) {
        try {
            if (requestBody == null) {
                return CompletableFuture.completedFuture(
                        BridgeApiResponse.error("Request body is required", HttpStatus.BAD_REQUEST)
                );
            }

            TestResponse response = TestResponse.builder()
                    .received(requestBody)
                    .timestamp(System.currentTimeMillis())
                    .message("Successfully processed POST request")
                    .build();

            return CompletableFuture.completedFuture(BridgeApiResponse.success(response));
        } catch (Exception e) {
            logError("Failed to process test request", e);
            return CompletableFuture.completedFuture(
                    BridgeApiResponse.error("Failed to process test request", HttpStatus.INTERNAL_SERVER_ERROR)
            );
        }
    }

    private void logError(String message, Throwable e) {
        StatsCatwalk.error(message);
        if (e != null) {
            StatsCatwalk.error(e.getMessage());
            e.printStackTrace();
        }
    }
}