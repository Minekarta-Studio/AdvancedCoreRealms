# AdvancedCoreRealms Documentation

## Table of Contents
1. [Overview](#overview)
2. [Features](#features)
3. [Installation](#installation)
4. [Configuration](#configuration)
5. [Commands](#commands)
6. [Permissions](#permissions)
7. [API Integration](#api-integration)
8. [Development Guidelines](#development-guidelines)
9. [Troubleshooting](#troubleshooting)
10. [FAQ](#faq)

## Overview

AdvancedCoreRealms is a comprehensive PaperMC plugin designed to provide players with a "private realm" experience on a Minecraft server. This plugin allows players to create, manage, and share their own personal worlds with friends, featuring capabilities such as per-world separated inventories, an easy-to-use invitation system, and an intuitive Graphical User Interface (GUI). The plugin is also equipped with a robust set of admin commands for management and moderation.

**Target Minecraft Version**: 1.21  
**Target Platform**: PaperMC  
**Target Audience**: Survival, Creative, or Towny servers that wish to offer a premium private world feature or as a standard feature for all players.

## Features

### Core Features
- **Personal Realms**: Players can create their own private worlds (flat or vanilla type)
- **Separate Inventories**: Each realm has its own inventory system that's separate from other realms
- **Invitation System**: Players can invite others to join their private realms
- **GUI Interface**: Intuitive graphical user interface for easy realm management
- **Admin Tools**: Comprehensive administration commands for server management
- **Multi-Language Support**: English, Spanish, and Indonesian language options

### World Management
- **World Creation**: Create new realms with flat or vanilla terrain types
- **World Deletion**: Securely delete realms with proper cleanup
- **World Teleportation**: Teleport between realms with a single command
- **Realm Listing**: View all accessible realms (owned and invited)

### Inventory Management
- **Per-World Inventories**: Separate inventories maintained for each realm
- **Inventory Persistence**: Player inventories are saved and restored when switching between worlds
- **Configurable**: Inventory separation can be enabled/disabled via configuration

### Invitation System
- **Easy Sharing**: Invite players to access your private realm
- **Temporary Invitations**: Invitations have configurable timeout periods
- **Accept/Deny Mechanism**: Players can accept or deny realm invitations
- **Pending Invitation Tracking**: System tracks pending invitations per player

### Multi-Language Support
- **Configurable Languages**: English, Spanish, and Indonesian supported
- **Customizable Messages**: All messages can be customized per language
- **Easy Extension**: Additional languages can be added by creating new language files

## Installation

### Prerequisites
- PaperMC server version 1.21 or higher
- Java 21 or higher
- Sufficient disk space for additional world files
- Appropriate server permissions for world creation/deletion

### Installation Steps
1. Download the `advancedrealmcore-1.0.0-ALPHA.jar` file
2. Place the JAR file in your server's `plugins` directory
3. Restart your PaperMC server
4. The plugin will automatically generate configuration files in the `plugins/AdvancedCoreRealms/` directory
5. Configure the plugin as needed (see Configuration section)
6. Restart the server again to apply configuration changes

### Post-Installation Checks
- Verify the plugin loaded successfully in the server console
- Check that the configuration files were generated in the plugin directory
- Test basic functionality with a test realm
- Verify permissions are properly set for your players

## Configuration

### Main Configuration (config.yml)
```yaml
# Language Settings
# Available languages: en (English), es (Spanish), id (Indonesian)
language: "en"

# World Settings
max-realms-per-player: 3 # The maximum number of realms a player can own (requires separate permissions for more)
default-world-type: "FLAT" # Default world type on creation: FLAT or NORMAL

# Inventory Settings
separate-inventories: true # Enable/disable separate inventories for realms

# Limits
invite-timeout-seconds: 60 # Time in seconds before an invitation expires
```

### Configuration Options Detailed

#### Language Settings
- `language`: Sets the default language for plugin messages (valid values: "en", "es", "id")
- Default: "en"

#### World Settings
- `max-realms-per-player`: Maximum number of realms a player can own (this can be overridden with permissions)
- `default-world-type`: Default terrain type when creating new realms (valid values: "FLAT", "NORMAL")
- Default: 3 realms per player, FLAT world type

#### Inventory Settings
- `separate-inventories`: Whether to maintain separate inventories per realm
- Default: true

#### Limits
- `invite-timeout-seconds`: How long invitations remain valid before expiring
- Default: 60 seconds

### Language Configuration
Language files are located in `plugins/AdvancedCoreRealms/languages/`:
- `en.yml` - English language settings
- `es.yml` - Spanish language settings  
- `id.yml` - Indonesian language settings

Each language file contains:
- `prefix`: Message prefix for all plugin messages
- `command.*`: Messages related to commands
- `world.*`: Messages related to world operations
- `error.*`: Error messages

## Commands

### Player Commands
| Command | Alias | Usage | Permission | Description |
|---------|-------|-------|------------|-------------|
| `/realms` | `/realm` | `/realms` | `advancedcorerealms.user.base` | Opens the main GUI interface |
| `/realms gui` | - | `/realms gui` | `advancedcorerealms.user.base` | Opens the main GUI interface |
| `/realms list` | - | `/realms list` | `advancedcorerealms.user.list` | Lists all accessible realms |
| `/realms create` | - | `/realms create <name> [FLAT/NORMAL]` | `advancedcorerealms.user.create` | Creates a new realm with specified name and type |
| `/realms delete` | - | `/realms delete <world>` | `advancedcorerealms.user.delete` | Deletes the specified realm (must be owner) |
| `/realms tp` | - | `/realms tp <world>` | `advancedcorerealms.user.teleport` | Teleports to the specified realm |
| `/realms teleport` | - | `/realms teleport <world>` | `advancedcorerealms.user.teleport` | Teleports to the specified realm |
| `/realms invite` | - | `/realms invite <world> <player>` | `advancedcorerealms.user.invite` | Invites a player to a realm |
| `/realms accept` | - | `/realms accept` | `advancedcorerealms.user.accept` | Accepts a pending realm invitation |
| `/realms deny` | - | `/realms deny` | `advancedcorerealms.user.deny` | Denies a pending realm invitation |
| `/realms help` | - | `/realms help` | `advancedcorerealms.user.help` | Shows help information |

### Admin Commands
| Command | Usage | Permission | Description |
|---------|-------|------------|-------------|
| `/realms reload` | `/realms reload` | `advancedcorerealms.admin.reload` | Reloads plugin configuration |
| `/realms transfer` | `/realms transfer <world> <player>` | `advancedcorerealms.admin.transfer` | Transfers ownership of a realm to another player |

### Command Details

#### Creating Realms
- Usage: `/realms create <name> [type]`
- Examples:
  - `/realms create myrealm` (creates a flat realm named "myrealm")
  - `/realms create myrealm NORMAL` (creates a normal world named "myrealm")
- The realm name cannot contain special characters
- Players are limited by the `max-realms-per-player` setting or their permissions
- Realm names must be unique across the server

#### Deleting Realms
- Usage: `/realms delete <world>`
- The command will fail if you're not the owner of the specified realm
- All players in the realm will be teleported out before deletion
- The world files will be permanently removed from the server
- The realm data will be removed from the database

#### Teleporting to Realms
- Usage: `/realms tp <world>`
- You must have access to the realm (be the owner or invited)
- Your previous location is saved for the `/realms back` command
- If separate inventories are enabled, the appropriate inventory will be loaded

#### Inviting Players
- Usage: `/realms invite <world> <player>`
- You must be the owner of the specified world
- The invited player will receive an invitation message
- The invitation will expire after the configured timeout period
- The invited player can accept with `/realms accept` or deny with `/realms deny`

## Permissions

The plugin uses a comprehensive permission system to control access to features.

### Player Base Permissions
- `advancedcorerealms.user.base` - Grants access to open the main GUI with `/realms`
- `advancedcorerealms.user.help` - Allows the user to view the help command
- `advancedcorerealms.user.create` - Allows the user to create new realms
- `advancedcorerealms.user.delete` - Allows the user to delete their own realms
- `advancedcorerealms.user.teleport` - Allows the user to teleport to their realms
- `advancedcorerealms.user.back` - Allows the user to return to their previous location
- `advancedcorerealms.user.list` - Allows the user to list their accessible realms
- `advancedcorerealms.user.invite` - Allows the user to invite others to their realms
- `advancedcorerealms.user.accept` - Allows the user to accept a realm invitation
- `advancedcorerealms.user.deny` - Allows the user to deny a realm invitation

### Realm Limit Permissions
- `advancedcorerealms.limit.realms.1` - Allows owning 1 realm
- `advancedcorerealms.limit.realms.3` - Allows owning 3 realms
- `advancedcorerealms.limit.realms.5` - Allows owning 5 realms
- (etc.)

These permissions allow for flexible realm limit management. Players can have multiple limit permissions, and the highest limit will apply.

### Admin Permissions
- `advancedcorerealms.admin.*` - Grants all administrative permissions
- `advancedcorerealms.admin.reload` - Allows use of `/realms reload`
- `advancedcorerealms.admin.transfer` - Allows transferring ownership of a realm
- `advancedcorerealms.admin.vanilla` - Grants access to all `/realms vanilla` subcommands
- `advancedcorerealms.admin.teleport.others` - Allows teleporting to any realm without an invitation

### Default Permission Mappings
- Players with `op` status automatically get admin permissions
- Standard players get `advancedcorerealms.user.*` permissions by default
- Realm limits default to 1, but can be increased with permissions

## API Integration

The plugin provides a comprehensive API for other developers to integrate with AdvancedCoreRealms.

### Getting Started
```java
AdvancedCoreRealms plugin = AdvancedCoreRealms.getInstance();
```

### Available Managers
- `getWorldManager()` - For world creation, deletion, and management
- `getInviteManager()` - For managing invitations
- `getLanguageManager()` - For handling multi-language messages
- `getWorldDataManager()` - For accessing realm data
- `getPlayerDataManager()` - For accessing player-specific data

### Example API Usage
```java
// Creating a realm programmatically
WorldManager worldManager = AdvancedCoreRealms.getInstance().getWorldManager();
worldManager.createWorldAsync(player, "myRealm", "FLAT");

// Checking if a player has access to a realm
WorldDataManager dataManager = AdvancedCoreRealms.getInstance().getWorldDataManager();
boolean hasAccess = dataManager.isPlayerInRealm(player.getUniqueId(), "realmName");

// Sending localized messages
MessageUtils.sendMessage(player, "world.created", "%world%", "MyRealm");
```

## Development Guidelines

### Code Structure
The plugin follows the Model-View-Presenter (MVP) pattern with the following package structure:

```
com.minekarta.advancedcorerealms
├── AdvancedCoreRealms.java         // Main plugin class (onEnable, onDisable)
├── commands                        // For all command handler classes
│   ├── RealmsCommand.java          // Main handler for /realms and its subcommands
├── data                            // Classes for managing data storage
│   ├── WorldDataManager.java       // Manages world data (owner, members, settings)
│   ├── PlayerDataManager.java      // Manages player data (inventories, last location)
│   └── object                      // Custom data objects (e.g., Realm.java)
├── gui                             // Classes related to the GUI
│   ├── menu                        // Classes for each specific GUI menu
│   │   ├── MainMenu.java
│   │   ├── WorldListMenu.java
│   │   └── WorldSettingsMenu.java
│   └── GuiManager.java             // Manages GUI opening and interactions
├── listeners                       // For all event listeners
│   ├── PlayerConnectionListener.java // Handles PlayerJoinEvent, PlayerQuitEvent
│   └── PlayerWorldListener.java    // Handles PlayerChangedWorldEvent (for inventories)
├── manager                         // Manager classes for core logic
│   ├── WorldManager.java           // Logic for creating, deleting, loading worlds
│   ├── InviteManager.java          // Manages pending invitations
│   └── LanguageManager.java        // Manages multi-language messages
└── utils                           // Utility classes
    └── MessageUtils.java           // Utilities for sending formatted/colored messages
```

### Adding New Features
1. Create new functionality in the appropriate package
2. Follow existing code patterns and conventions
3. Ensure proper error handling and validation
4. Add appropriate permissions where needed
5. Test thoroughly before deployment
6. Update documentation as needed

### Data Storage
- Worlds are stored in `worlds.yml` in the plugin data folder
- Player-specific data (inventories) is stored in `playerdata/<uuid>/` directory
- Configuration is stored in `config.yml`
- Language files are stored in `languages/` directory

### Best Practices
- Always use the provided managers rather than manipulating data directly
- Follow the existing permission checking patterns
- Use MessageUtils for sending localized messages
- Implement proper error handling for all user-facing operations
- Follow Minecraft plugin development best practices for performance

## Troubleshooting

### Common Issues

#### Plugin Won't Load
**Symptoms**: Plugin doesn't appear in the server console or `/plugins` list

**Solutions**:
1. Verify your server is running PaperMC 1.21 or higher
2. Check that the JAR file is placed in the `plugins` directory
3. Review the server console for specific error messages
4. Ensure you have Java 21 or higher installed

#### Configuration Not Loading
**Symptoms**: Default configuration values are not appearing in config.yml

**Solutions**:
1. Verify the plugin loaded successfully first
2. Check file permissions on the plugin folder
3. Restart the server after plugin installation
4. Ensure you're editing the correct config.yml file in the plugin directory

#### World Creation Fails
**Symptoms**: `/realms create` command returns an error

**Solutions**:
1. Verify you have the `advancedcorerealms.user.create` permission
2. Check that you haven't exceeded the realm limit
3. Ensure the world name is unique and doesn't contain invalid characters
4. Check server disk space availability
5. Review server console for specific error messages

#### Inventory Not Switching
**Symptoms**: Inventories don't change when moving between realms

**Solutions**:
1. Verify `separate-inventories` is set to `true` in config.yml
2. Check that the plugin has proper permissions to save player data
3. Verify player data files are being created in the correct directory
4. Ensure no other plugins are interfering with inventory management

#### GUI Not Opening
**Symptoms**: `/realms` command doesn't open the GUI

**Solutions**:
1. Verify you have the `advancedcorerealms.user.base` permission
2. Check that the required permissions are properly assigned
3. Review server console for error messages when opening GUI
4. Ensure no other plugins are conflicting with inventory operations

### Performance Issues
- **High Memory Usage**: Realm data is loaded into memory; consider limiting max realms per player
- **Slow World Creation**: Creating complex worlds (NORMAL type) can take time; consider using FLAT as default
- **Inventory Lag**: With many players and realms, consider optimizing disk I/O

### Debugging Steps
1. Check the server console for error messages
2. Verify all required permissions are assigned
3. Review configuration files for correct settings
4. Test with a fresh server to isolate plugin conflicts
5. Enable debug logging if available

## FAQ

### General Questions

**Q: What Minecraft versions does this plugin support?**  
A: This plugin is designed specifically for PaperMC 1.21. It may work on other 1.21+ versions but is optimized for PaperMC servers.

**Q: How many realms can a player create?**  
A: By default, all players can create up to 1 realm. This can be modified through configuration settings or permissions (`advancedcorerealms.limit.realms.<number>`).

**Q: Are separate inventories automatic?**  
A: Yes, separate inventories are enabled by default and automatically manage player inventory when switching between realms.

**Q: Can I add more languages?**  
A: Yes, you can create additional language files in the `languages/` directory following the same format as the existing ones.

### Technical Questions

**Q: How are realms stored?**  
A: Realms are stored as regular Minecraft world folders on the server, with metadata stored in `worlds.yml`.

**Q: What happens to worlds when players are offline?**  
A: Worlds remain on the server and can be loaded when players return. The plugin handles lazy loading/unloading of worlds.

**Q: Can players access other players' realms?**  
A: Only realm owners and invited players can access a private realm, unless admin permissions allow bypassing this.

**Q: How long do invitations last?**  
A: By default, invitations last 60 seconds before expiring. This can be changed in the configuration.

### Administrative Questions

**Q: How do I reset all realm data?**  
A: Delete `worlds.yml` and the individual world folders (backup first). Also clear the `playerdata/` directory if needed.

**Q: How can I transfer realm ownership?**  
A: Use `/realms transfer <world> <player>` with the `advancedcorerealms.admin.transfer` permission.

**Q: Can I limit realm creation by player rank?**  
A: Yes, use the `advancedcorerealms.limit.realms.<number>` permissions to set different limits per player group.

**Q: What resources does this plugin require?**  
A: Realm creation requires disk space for world files and RAM for active worlds. Performance depends on the number of active realms and players.

### Advanced Questions

**Q: Can other plugins integrate with AdvancedCoreRealms?**  
A: Yes, the plugin provides a complete API for integration with other plugins.

**Q: Is there a way to automatically delete unused realms?**  
A: Currently, there's no automated cleanup feature, but this could be added through custom development.

**Q: How does the inventory system work?**  
A: Each realm has its own inventory file. When a player enters a realm, their inventory is loaded from the specific realm file. When leaving, it's saved to that same file.

**Q: What happens if the server crashes?**  
A: Realm data is persistent and will be preserved. Player inventories are saved when switching worlds, minimizing data loss during crashes.

## Support

For additional support, please:
- Check the troubleshooting section above
- Review the server console for error messages
- Ensure all configuration files are properly formatted
- Verify your server meets the requirements
- Contact the plugin developer with detailed information about the issue, including:
  - Server version and plugin version
  - Steps to reproduce the issue
  - Any relevant console error messages
  - Your configuration files (if appropriate)

---

*Document Version: 1.0*  
*Plugin Version: 1.0.0-ALPHA*  
*Last Updated: September 2025*