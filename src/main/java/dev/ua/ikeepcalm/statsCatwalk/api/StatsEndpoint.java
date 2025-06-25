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
            description = "Retrieves comprehensive server statistics including total player counts, average playtime, " +
                    "server performance metrics, and other aggregated data. This endpoint provides a high-level " +
                    "overview of server activity and performance for dashboard displays.",
            tags = {"Server Statistics"},
            responses = {
                    @OpenApiResponse(status = "200", description = "Successfully retrieved server statistics summary",
                            content = @OpenApiContent(
                                    from = StatsSummaryResponse.class,
                                    mimeType = "application/json",
                                    example = """
                        {
                          "summary": {
                            "totalPlayers": 150,
                            "averagePlaytime": 45.5,
                            "peakOnline": 89,
                            "currentTPS": 19.8,
                            "uptime": 72.5
                          }
                        }
                        """
                            )),
                    @OpenApiResponse(status = "500", description = "Internal server error occurred while retrieving statistics",
                            content = @OpenApiContent(from = BridgeApiResponse.class, mimeType = "application/json"))
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
            summary = "Get historical online players data",
            description = "Retrieves historical online player data for a specified number of days along with the current " +
                    "hourly player distribution. This endpoint provides insights into player activity patterns over time " +
                    "and helps identify peak hours and player engagement trends.",
            tags = {"Player Statistics"},
            queryParams = {
                    @OpenApiParam(
                            name = "days", 
                            type = Integer.class, 
                            description = "Number of days of historical data to return (minimum 1, maximum 14, default 7)",
                            example = "7"
                    )
            },
            responses = {
                    @OpenApiResponse(status = "200", description = "Successfully retrieved historical online players data",
                            content = @OpenApiContent(
                                    from = OnlinePlayersResponse.class,
                                    mimeType = "application/json",
                                    example = """
                        {
                          "players": [
                            {
                              "timestamp": "2024-01-01T12:00:00Z",
                              "count": 45,
                              "players": ["ikeepcalm", "player2", "player3"]
                            }
                          ],
                          "hourly_distribution": {
                            "0": 12,
                            "6": 25,
                            "12": 67,
                            "18": 89,
                            "23": 34
                          }
                        }
                        """
                            )),
                    @OpenApiResponse(status = "400", description = "Invalid days parameter provided",
                            content = @OpenApiContent(from = BridgeApiResponse.class, mimeType = "application/json")),
                    @OpenApiResponse(status = "500", description = "Internal server error occurred while retrieving data",
                            content = @OpenApiContent(from = BridgeApiResponse.class, mimeType = "application/json"))
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
            List<OnlinePlayerData> playersData = statsManager.getOnlinePlayersData(days);
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
            description = "Retrieves a ranked list of the most active players sorted by total playtime. This endpoint " +
                    "provides insights into player dedication and engagement, showing the top contributors to server " +
                    "activity along with their playtime statistics and ranking information.",
            tags = {"Player Statistics"},
            queryParams = {
                    @OpenApiParam(
                            name = "limit", 
                            type = Integer.class, 
                            description = "Maximum number of top players to return (minimum 1, default 10)",
                            example = "10"
                    )
            },
            responses = {
                    @OpenApiResponse(status = "200", description = "Successfully retrieved top players ranked by playtime",
                            content = @OpenApiContent(
                                    from = TopPlayersResponse.class,
                                    mimeType = "application/json",
                                    example = """
                        {
                          "players": [
                            {
                              "nickname": "ikeepcalm",
                              "playtime": 12450,
                              "rank": 1,
                              "lastSeen": "2024-01-01T12:00:00Z",
                              "firstJoined": "2023-06-15T08:30:00Z"
                            },
                            {
                              "nickname": "player2",
                              "playtime": 11890,
                              "rank": 2,
                              "lastSeen": "2024-01-01T10:45:00Z",
                              "firstJoined": "2023-08-20T14:20:00Z"
                            }
                          ]
                        }
                        """
                            )),
                    @OpenApiResponse(status = "400", description = "Invalid limit parameter provided",
                            content = @OpenApiContent(from = BridgeApiResponse.class, mimeType = "application/json")),
                    @OpenApiResponse(status = "500", description = "Internal server error occurred while retrieving top players",
                            content = @OpenApiContent(from = BridgeApiResponse.class, mimeType = "application/json"))
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
            List<TopPlayerData> result = statsManager.getTopPlayers(limit);
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
            description = "Retrieves the current player count distribution across all 24 hours of the day. This endpoint " +
                    "provides valuable insights into server activity patterns, helping identify peak hours, low-activity " +
                    "periods, and overall player engagement throughout the day for server optimization and event planning.",
            tags = {"Player Statistics"},
            responses = {
                    @OpenApiResponse(status = "200", description = "Successfully retrieved hourly player distribution data",
                            content = @OpenApiContent(
                                    from = HourlyDistributionResponse.class,
                                    mimeType = "application/json",
                                    example = """
                        {
                          "hourly_distribution": {
                            "0": 12,
                            "1": 8,
                            "6": 25,
                            "12": 67,
                            "18": 89,
                            "19": 95,
                            "20": 87,
                            "23": 34
                          }
                        }
                        """
                            )),
                    @OpenApiResponse(status = "500", description = "Internal server error occurred while retrieving hourly distribution",
                            content = @OpenApiContent(from = BridgeApiResponse.class, mimeType = "application/json"))
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
            summary = "Test endpoint for API validation",
            description = "Test endpoint designed for API validation and debugging purposes. Accepts arbitrary JSON data " +
                    "and returns it along with processing metadata. This endpoint is useful for testing client-server " +
                    "communication, validating request formatting, and debugging API integration issues.",
            tags = {"Development & Testing"},
            requestBody = @OpenApiRequestBody(
                    description = "Test data payload to echo back with processing information",
                    required = true,
                    content = @OpenApiContent(
                            mimeType = "application/json",
                            example = """
                        {
                          "message": "Hello from client",
                          "data": {
                            "key": "value",
                            "number": 42,
                            "nested": {
                              "test": true
                            }
                          },
                          "timestamp": "2024-01-01T12:00:00Z"
                        }
                        """
                    )
            ),
            responses = {
                    @OpenApiResponse(status = "200", description = "Successfully processed test request and returned echo data",
                            content = @OpenApiContent(
                                    from = TestResponse.class,
                                    mimeType = "application/json",
                                    example = """
                        {
                          "received": {
                            "message": "Hello from client",
                            "data": {
                              "key": "value",
                              "number": 42
                            }
                          },
                          "timestamp": 1704067200000,
                          "message": "Successfully processed POST request"
                        }
                        """
                            )),
                    @OpenApiResponse(status = "400", description = "Invalid or missing request body",
                            content = @OpenApiContent(from = BridgeApiResponse.class, mimeType = "application/json")),
                    @OpenApiResponse(status = "500", description = "Internal server error occurred while processing test request",
                            content = @OpenApiContent(from = BridgeApiResponse.class, mimeType = "application/json"))
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