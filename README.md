# StatsCatwalk

A comprehensive Minecraft server statistics plugin that provides detailed analytics through a REST API.

## Features

- **Player Statistics**: Track online player counts, session times, and playtime data
- **Historical Data**: Store and retrieve player activity over configurable time periods
- **Hourly Analytics**: Detailed hourly player distribution tracking
- **Top Players**: Leaderboards based on playtime and activity
- **TPS Monitoring**: Server performance tracking (optional)
- **REST API**: Modern RESTful endpoints for external integrations
- **Data Persistence**: CSV-based data storage with automatic cleanup

## API Endpoints

### Statistics Summary
- **GET** `/stats/summary` - Overall server statistics including player counts and TPS

### Online Players Data
- **GET** `/stats/online?days=7` - Historical online player data (max 14 days)

### Top Players
- **GET** `/stats/topplayers?limit=10` - Most active players by playtime

### Hourly Distribution
- **GET** `/stats/hourly` - Current day's hourly player distribution

### Test Endpoint
- **POST** `/stats/test` - Test endpoint for API functionality

## Configuration

The plugin is configured via `config.yml`:

```yaml
collection:
  intervalMinutes: 10           # Data collection frequency
  dataRetentionDays: 14        # How long to keep historical data

features:
  enableTpsTracking: true       # Enable TPS monitoring
  maxTopPlayersLimit: 100      # Max players in leaderboards
  saveOnPlayerQuit: true       # Immediate data saving
```

## Installation

1. Ensure you have **CatWalk** plugin installed as a dependency
2. Download the latest StatsCatwalk JAR file
3. Place it in your server's `plugins/` folder
4. Restart your server
5. Configure the plugin in `plugins/StatsCatwalk/config.yml` if needed

## Data Storage

Stats are stored in CSV format in the `plugins/StatsCatwalk/stats/` directory:

- `online_history.csv` - Historical online player counts
- `hourly_distribution.csv` - Hourly player distribution data
- `player_playtimes.csv` - Individual player playtime records

## Dependencies

- **CatWalk** - Required for web server functionality
- **Paper/Spigot** 1.21.4+ - Minecraft server platform

## API Usage Examples

### Get Server Summary
```bash
curl http://your-server.com/stats/summary
```

### Get Weekly Online Data
```bash
curl http://your-server.com/stats/online?days=7
```

### Get Top 20 Players
```bash
curl http://your-server.com/stats/topplayers?limit=20
```

## Development

This plugin uses:
- Modern Paper API
- Lombok for boilerplate reduction
- OpenAPI documentation
- Bridge pattern for API endpoints

## License

This plugin is part of the UA Project ecosystem.

## Support

For support and issues, visit: https://uaproject.xyz