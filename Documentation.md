# AdvancedCoreRealms - Comprehensive Documentation

## Table of Contents
1. [Overview](#overview)
2. [Features](#features)
3. [Installation](#installation)
4. [Configuration](#configuration)
5. [Commands](#commands)
6. [Permissions](#permissions)
7. [Placeholders](#placeholders)
8. [Menu Customization](#menu-customization)
9. [Language Customization](#language-customization)
10. [API](#api)

## Overview
AdvancedCoreRealms is a feature-rich plugin that allows players to create and manage their own personal worlds (Realms). The plugin includes a fully customizable GUI system, multilingual support, PlaceholderAPI integration, and comprehensive realm management features.

## Features
- **Personal Worlds:** Create your own private worlds with different types (Flat, Normal, Amplified)
- **Customizable GUI:** Fully configurable menus with gradient colors and advanced formatting
- **Multilingual Support:** Support for English, Spanish, and Indonesian with easy addition of more languages
- **PlaceholderAPI Integration:** Extensive placeholder support for use in other plugins
- **Permission System:** Flexible permission system for realm limits and management
- **Separate Inventories:** Each realm maintains its own inventory system
- **Player Invitations:** Invite other players to your realms
- **Configurable Settings:** Adjust player limits, world types, and more

## Installation
1. Download the `AdvancedCoreRealms.jar` file
2. Place it in your server's `plugins` folder
3. Restart or reload your server
4. The plugin will generate its configuration files automatically
5. Configure the settings to your needs

## Configuration

### Main Config (config.yml)
```yaml
# Language Settings
# Available languages: en (English), es (Spanish), id (Indonesian)
language: "en"

# World Settings
max-realms-per-player: 3 # The maximum number of realms a player can own (requires separate permissions for more)
default-world-type: "FLAT" # Default world type on creation: FLAT or NORMAL
default-max-players: 8 # Default maximum number of players allowed in a realm

# Inventory Settings
separate-inventories: true # Enable/disable separate inventories for realms

# Limits
invite-timeout-seconds: 60 # Time in seconds before an invitation expires
```

### Menu Configuration
All menu configurations are stored in the `menu/` folder within the plugin data directory. The system supports:
- Customizable titles with gradient colors
- Configurable item materials, names, and lores
- Permission-based item visibility
- Placeholder support in names and lores

### Language Configuration
Language files are stored in the `languages/` folder. Each language has its own file:
- `en.yml` - English
- `es.yml` - Spanish
- `id.yml` - Indonesian

## Commands
- `/realms` or `/realms gui` - Open the main realms GUI
- `/realms create <name> [type]` - Create a new realm
- `/realms list` - List all your accessible realms
- `/realms tp <world>` or `/realms teleport <world>` - Teleport to a realm
- `/realms delete <world>` - Delete a realm you own
- `/realms invite <world> <player>` - Invite a player to your realm
- `/realms accept` - Accept an invitation
- `/realms deny` - Deny an invitation
- `/realms back` - Return to your previous location
- `/realms reload` - Reload configurations (admin only)
- `/realms transfer <world> <player>` - Transfer realm ownership (admin only)
- `/realms debug` - Run system diagnostics (admin only)

## Permissions
### Basic Permissions
- `advancedcorerealms.user.base` - Access to open the main GUI (Default: true)
- `advancedcorerealms.user.help` - Use the help command (Default: true)
- `advancedcorerealms.user.back` - Use the back command (Default: true)
- `advancedcorerealms.user.list` - List accessible realms (Default: true)

### Creation Permissions
- `advancedcorerealms.user.create` - Create new realms (Default: op)
- `advancedcorerealms.user.delete` - Delete own realms (Default: op)
- `advancedcorerealms.user.teleport` - Teleport to realms (Default: true)

### Invitation Permissions
- `advancedcorerealms.user.invite` - Invite others to your realms (Default: op)
- `advancedcorerealms.user.accept` - Accept invitations (Default: true)
- `advancedcorerealms.user.deny` - Deny invitations (Default: true)

### Administrative Permissions
- `advancedcorerealms.admin.*` - All administrative permissions (Default: op)
- `advancedcorerealms.admin.reload` - Reload configurations (Default: op)
- `advancedcorerealms.admin.transfer` - Transfer realm ownership (Default: op)
- `advancedcorerealms.admin.vanilla` - Access to vanilla subcommands (Default: op)
- `advancedcorerealms.admin.teleport.others` - Teleport to any realm (Default: op)
- `advancedcorerealms.admin.debug` - Debug command access (Default: op)

### Limit Permissions
- `advancedcorerealms.limit.realms.1` - Can own 1 realm (Default: true)
- `advancedcorerealms.limit.realms.3` - Can own 3 realms (Default: false)
- `advancedcorerealms.limit.realms.5` - Can own 5 realms (Default: false)

### Donor Permissions
- `advancedcorerealms.donor.create` - Create realms (donor feature)
- `advancedcorerealms.unlimited.create` - Create unlimited realms

## Placeholders
The plugin provides several PlaceholderAPI placeholders:
- `%advancedcorerealms_total_realms%` - Total number of realms on the server
- `%advancedcorerealms_player_realms_count%` - Number of realms owned by the player
- `%advancedcorerealms_player_invited_realms_count%` - Number of realms the player is invited to
- `%advancedcorerealms_player_total_accessible_realms%` - Total accessible realms for the player
- `%advancedcorerealms_player_current_realm%` - The name of the realm the player is currently in
- `%advancedcorerealms_current_realm_owner%` - Owner of the current realm
- `%advancedcorerealms_current_realm_player_count%` - Number of players in current realm
- `%advancedcorerealms_current_realm_max_players%` - Max players in current realm
- `%advancedcorerealms_current_realm_type%` - Type of current realm

## Menu Customization
All GUI elements can be customized through the configuration files in the `menu/` folder:

### Main Menu Customization
Edit `menu/main_menu.yml` to customize the main menu:
- Change the title with color codes and gradients
- Modify item materials, names, and lores
- Set permission requirements for items
- Customize the glass pane filling

### Advanced Color Formatting
The plugin supports various color formatting options:
- Standard color codes: `&c`, `&a`, etc.
- Hex colors: `#FF5555`, `#6677EE`
- Gradient colors: `{#6677ee>99ffcc}Text here{/#}`
- Placeholders can be used in menu items: `[name]` for realm name

## Language Customization
To customize messages for a language, edit the appropriate file in the `languages/` folder:
- Each message has a unique key that can be customized
- Color codes and gradients are supported
- Placeholders like `%world%` are automatically replaced

## API
The plugin provides a comprehensive API for other plugins to interact with:
- `AdvancedCoreRealms.getInstance()` - Get the plugin instance
- `plugin.getWorldManager()` - Access world management features
- `plugin.getWorldDataManager()` - Access realm data
- `plugin.getGuiManager()` - Access GUI management
- `plugin.getMenuManager()` - Access configurable menu management

### Example API Usage
```java
AdvancedCoreRealms plugin = AdvancedCoreRealms.getInstance();
List<Realm> playerRealms = plugin.getWorldDataManager().getPlayerRealms(playerUUID);
```

## Support
For support, please check:
1. The configuration files for proper setup
2. The server console for error messages
3. Ensure PlaceholderAPI is installed if using placeholders
4. Contact the plugin author for additional support

## Updates
Always backup your configuration files before updating the plugin to prevent losing customizations.