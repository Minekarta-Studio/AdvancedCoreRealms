# MiniMessage Implementation for AdvancedCoreRealms

## Overview
This document explains how MiniMessage has been integrated into the AdvancedCoreRealms plugin to provide rich text formatting capabilities throughout the plugin.

## Features Implemented

### 1. MiniMessage Dependencies
- Added Adventure API and MiniMessage dependencies to the project
- Integrated with existing PlaceholderAPI support
- Shaded dependencies into the final JAR for standalone operation

### 2. ColorUtils Enhancement
The ColorUtils class has been updated to utilize MiniMessage for all text processing:

```java
// MiniMessage-based color processing
public static String processColors(String message, Player player) {
    if (message == null) {
        return null;
    }
    
    // Process placeholders first (if player is provided and PlaceholderAPI is available)
    if (player != null && Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
        message = PlaceholderAPI.setPlaceholders(player, message);
    }
    
    // Use MiniMessage to parse the message (supports & colors, hex colors, gradients, etc.)
    Component component = miniMessage.deserialize(message);
    
    return miniMessage.serialize(component);
}
```

### 3. Supported Formatting Options

#### Standard Minecraft Colors
```
<red>This is red text</red>
<blue>This is blue text</blue>
<green>This is green text</green>
```

#### Hex Colors
```
<color:#FF5555>This is a custom hex color</color>
<color:#6677EE>This is another hex color</color>
```

#### Gradients
```
<gradient:#6677EE:#99FFCC>This text has a gradient</gradient>
<gradient:red:blue:yellow>This has a multi-color gradient</gradient>
```

#### Advanced Formatting
```
<bold>Bold text</bold>
<italic>Italic text</italic>
<underline>Underlined text</underline>
<strikethrough>Strikethrough text</strikethrough>
```

#### Click and Hover Events
```
<click:run_command:/say hello><hover:show_text:"Click me!">Clickable text</hover></click>
<click:open_url:https://example.com>Visit website</click>
```

### 4. Language Manager Integration
Updated language files now support MiniMessage formatting:

```yaml
# English language file (en.yml)
prefix: "<dark_gray>[<aqua>Realms<dark_gray>] <reset>"
command.help: "<green>Showing help for AdvancedCoreRealms..."
command.reloaded: "<green>Configuration reloaded successfully."
world.created: "<green>Successfully created your new realm named <yellow>%world%<green>."
```

### 5. GUI Menu Support
All GUI elements now support MiniMessage formatting:

```yaml
# Menu configuration (main_menu.yml)
main_menu:
  title: "<gradient:#6677ee:#99ffcc>AdvancedCoreRealms</gradient>"
  elements:
    info_item:
      name: "<gradient:#6677ee:#99ffcc>AdvancedCoreRealms</gradient>"
      lore:
        - "<gradient:#6677ee:#99ffcc>Welcome to AdvancedCoreRealms!</gradient>"
```

### 6. API Usage Examples

#### Sending Messages to Players
```java
// Using ColorUtils with MiniMessage
ColorUtils.sendMessage(player, "<green>Welcome to <yellow>AdvancedCoreRealms<green>!");

// Using MessageUtils with placeholders
MessageUtils.sendMessage(player, "<green>Hello <yellow>%player_name%<green>!", 
                         "%player_name%", player.getName());

// Sending components directly
Component component = ColorUtils.toComponent("<rainbow>Rainbow Text!</rainbow>", player);
player.sendMessage(component);
```

#### Creating Formatted Items
```java
// Creating items with MiniMessage formatted names and lore
ItemStack item = createItem(Material.DIAMOND, 
                           "<gradient:gold:aqua>Diamond of Power</gradient>",
                           Arrays.asList(
                               "<gray>Legendary Item",
                               "<green>+10 Strength",
                               "<blue>+5 Magic"));
```

## Benefits of MiniMessage Implementation

### 1. Rich Text Formatting
- Gradient text support
- Hex color codes
- Advanced text decorations
- Clickable and hoverable text elements

### 2. Backward Compatibility
- Maintains support for traditional color codes (&c, &a, etc.)
- Seamless integration with existing configurations
- No breaking changes to current setups

### 3. Performance
- Efficient parsing and serialization
- Built-in caching mechanisms
- Optimized for Minecraft server environments

### 4. Extensibility
- Easy to extend with custom tags
- Modular design allows selective feature usage
- Compatible with other text processing libraries

## Migration Guide

### For Server Administrators
Existing configurations will continue to work unchanged. To utilize new MiniMessage features:

1. Update language files to use MiniMessage syntax:
   ```yaml
   # Old format
   prefix: "&8[&bRealms&8] &r"
   
   # New format with gradients
   prefix: "<gradient:#6677ee:#99ffcc>Realms</gradient>"
   ```

2. Update menu configurations for enhanced visuals:
   ```yaml
   # Menu item with gradient name
   name: "<gradient:blue:cyan>My Realms</gradient>"
   ```

### For Developers
Utilize the enhanced ColorUtils and MessageUtils:

```java
// Import the updated utilities
import com.minekarta.advancedcorerealms.utils.ColorUtils;
import com.minekarta.advancedcorerealms.utils.MessageUtils;

// Send rich formatted messages
ColorUtils.sendMessage(player, "<rainbow>Welcome to our server!</rainbow>");

// Create formatted components
Component welcomeComponent = ColorUtils.toComponent(
    "<gradient:green:blue>Welcome, <yellow>%player%</yellow>!</gradient>", 
    player);

player.sendMessage(welcomeComponent);
```

## Configuration Examples

### Language File Example (en.yml)
```yaml
prefix: "<dark_gray>[<aqua>Realms<dark_gray>] <reset>"
command:
  help: "<green>Showing help for AdvancedCoreRealms..."
  reloaded: "<green>Configuration reloaded successfully."
world:
  created: "<green>Successfully created your new realm named <yellow>%world%<green>."
  deleted: "<red>Successfully deleted your realm <yellow>%world%<red>."
  teleport: "<gray>Teleporting you to <yellow>%world%<gray>..."
error:
  no-permission: "<red>You do not have permission to use this command."
  world-exists: "<red>A realm with that name already exists."
  not-owner: "<red>You are not the owner of this realm."
```

### Menu Configuration Example (main_menu.yml)
```yaml
main_menu:
  title: "<gradient:#6677ee:#99ffcc>AdvancedCoreRealms</gradient>"
  size: 27
  elements:
    info_item:
      slot: 4
      material: "WRITTEN_BOOK"
      name: "<gradient:#6677ee:#99ffcc>AdvancedCoreRealms</gradient>"
      lore:
        - "<gradient:#6677ee:#99ffcc>Welcome to AdvancedCoreRealms!</gradient>"
        - ""
        - "<gradient:#6677ee:#99ffcc>Create and manage your personal worlds.</gradient>"
    
    my_realms:
      slot: 10
      material: "GRASS_BLOCK"
      name: "<gradient:#6677ee:#99ffcc>My Realms</gradient>"
      lore:
        - "<gradient:#6677ee:#99ffcc>View and manage your Realms</gradient>"
```

## Troubleshooting

### Common Issues

1. **Formatting not appearing**
   - Ensure MiniMessage tags are properly closed
   - Check for typos in color names or hex codes
   - Verify the message is being processed by ColorUtils

2. **Performance concerns**
   - Use simpler formatting for frequently updated text
   - Cache complex components when possible
   - Consider using static formatting for rarely changed messages

3. **Compatibility issues**
   - Legacy color codes (&c) still work alongside MiniMessage
   - Mixed formatting is supported
   - Test configurations after major updates

## Conclusion

The MiniMessage implementation in AdvancedCoreRealms provides powerful text formatting capabilities while maintaining backward compatibility and performance. Server administrators can now create visually stunning interfaces with gradients, hex colors, and advanced text effects, while developers have access to a robust API for building rich user experiences.