# AdvancedCoreRealms

A comprehensive Minecraft plugin for creating and managing personal worlds (Realms) with advanced features and customization options.

## Features

- **Personal Worlds**: Create private worlds of different types (Flat, Normal, Amplified)
- **Customizable GUI**: Fully configurable menus with gradient colors and advanced formatting
- **Multilingual Support**: English, Spanish, and Indonesian with easy expansion
- **PlaceholderAPI Integration**: Extensive placeholder support
- **Separate Inventories**: Each realm maintains its own inventory system
- **Player Management**: Invite others and manage realm access
- **Flexible Permissions**: Control realm creation and management through permissions

## Commands

- `/realms` - Open the main GUI
- `/realms create <name> [type]` - Create a new realm
- `/realms list` - List your realms
- `/realms tp <world>` - Teleport to a realm
- `/realms delete <world>` - Delete a realm
- `/realms invite <world> <player>` - Invite a player
- `/realms back` - Return to previous location

## Installation

1. Place the JAR file in your server's `plugins` folder
2. Restart/reload the server
3. Configure as needed in the generated config files
4. If using placeholders, install PlaceholderAPI

## Configuration

All configuration files are created in the plugin's data folder:
- `config.yml` - Main configuration
- `languages/` - Language files for multilingual support
- `menu/` - Menu customization files

## Permissions

- `advancedcorerealms.user.*` - Basic user permissions
- `advancedcorerealms.limit.realms.X` - Realm limits (1, 3, 5)
- `advancedcorerealms.admin.*` - Administrative permissions

## Placeholders

When PlaceholderAPI is installed:
- `%advancedcorerealms_player_realms_count%`
- `%advancedcorerealms_current_realm_owner%`
- And many more for use in other plugins

## Customization

The plugin is fully customizable:
- Menu layouts and items
- Colors and formatting with gradients
- Language messages
- Realm settings and limits