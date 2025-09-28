# AdvancedCoreRealms

**AdvancedCoreRealms** is a powerful and flexible Minecraft plugin designed for PaperMC servers, giving players the ability to create, manage, and customize their own personal worlds, known as "Realms." With a fully configurable GUI, extensive administrative controls, and multi-language support, it offers a premium and immersive experience for any server community.

## ✨ Features

- **Personal Player Worlds (Realms)**: Allow players to create their own private worlds. Supports multiple world types, including `FLAT`, `NORMAL`, and `AMPLIFIED`.
- **Fully Configurable GUI**: A modern, professional GUI system where every aspect—from layout and items to colors and text—can be customized through easy-to-edit YAML files.
- **Gradient & Hex Color Support**: Utilize MiniMessage formatting in all menus and messages, including gradients, hex colors, and more, to create a unique and visually appealing experience.
- **Player & Realm Management**: Players can manage their realms, invite friends, and set permissions. Admins have full oversight and control over all realms.
- **Dynamic Paging**: Menus with lists (like realms or players) are paginated to handle a large number of entries gracefully.
- **Permission-Based Access**: Fine-grained permission nodes control every feature, from creating realms to accessing specific menu items.
- **PlaceholderAPI Integration**: Exposes a rich set of placeholders for use in other plugins, allowing for deep integration with your server's ecosystem.
- **Multi-Language Support**: Comes with pre-built language files (English, Spanish, Indonesian) and is easily expandable.
- **Developer API**: A simple API to interact with player and realm data for custom integrations.

## 🚀 Installation

1.  Download the latest version of `AdvancedCoreRealms.jar`.
2.  Place the `.jar` file into your server's `plugins/` directory.
3.  Restart your server. The plugin will generate its default configuration files.
4.  (Optional) If you want to use placeholders, install [PlaceholderAPI](https://www.spigotmc.org/resources/placeholderapi.6245/).

## ⚙️ Configuration

All configuration is located in the `plugins/AdvancedCoreRealms/` directory.

-   `config.yml`: Main plugin configuration.
-   `languages/`: Contains language files (`en.yml`, `es.yml`, etc.).
-   `menu/`: Contains all GUI menu configuration files.

### Customizing Menus

The GUI is entirely configured in the `menu/` directory. Each file corresponds to a different menu.

**Example: `main_menu.yml`**
```yaml
main_menu:
  title: "{#6677ee>99ffcc}AdvancedCoreRealms{/#}"
  size: 27
  elements:
    my_realms:
      slot: 10
      material: "GRASS_BLOCK"
      name: "{#6677ee>99ffcc}My Realms{/#}"
      lore:
        - "{#6677ee>99ffcc}View and manage your Realms{/#}"
    glass_panes:
      material: "BLACK_STAINED_GLASS_PANE"
      name: " "
      fill_remaining: true
```

-   **title**: The title of the menu. Supports MiniMessage formatting.
-   **size**: The size of the inventory (must be a multiple of 9).
-   **elements**: A list of items to display in the menu.
    -   **slot**: The inventory slot for the item (0-indexed).
    -   **material**: The [Bukkit Material name](https://hub.spigotmc.org/javadocs/spigot/org/bukkit/Material.html).
    -   **name/lore**: The display name and lore for the item. Supports MiniMessage.
    -   **permission_required**: (Optional) A comma-separated list of permissions the player must have to see the item normally.
    -   **no_permission_material/name/lore**: (Optional) The item's appearance if the player lacks the required permission.
    -   **fill_remaining**: (Optional, for `glass_panes`) If `true`, fills all empty slots with this item.

## 💬 Commands

### User Commands
| Command | Description | Permission |
| --- | --- | --- |
| `/realms` | Opens the main realms menu. | `advancedcorerealms.user.base` |
| `/realms create <name> [type]` | Creates a new realm. | `advancedcorerealms.user.create` |
| `/realms list` | Lists all your owned and invited realms. | `advancedcorerealms.user.list` |
| `/realms tp <realm>` | Teleports you to a realm you own or are invited to. | `advancedcorerealms.user.teleport` |
| `/realms invite <realm> <player>` | Invites a player to your realm. | `advancedcorerealms.user.invite` |
| `/realms accept` | Accepts a pending realm invitation. | `advancedcorerealms.user.accept` |
| `/realms deny` | Denies a pending realm invitation. | `advancedcorerealms.user.deny` |
| `/realms back` | Teleports you to your location before your last realm teleport. | `advancedcorerealms.user.back` |
| `/realms delete <realm>` | Deletes a realm you own. | `advancedcorerealms.user.delete` |

### Admin Commands
| Command | Description | Permission |
| --- | --- | --- |
| `/realms reload` | Reloads all configuration files. | `advancedcorerealms.admin.reload` |
| `/realms transfer <realm> <player>` | Transfers ownership of a realm to another player. | `advancedcorerealms.admin.transfer` |
| `/realms delete <realm>` | Force-deletes any realm. | `advancedcorerealms.admin.delete` |

## Permissions

### User Permissions
- `advancedcorerealms.user.*`: Grants all basic user permissions.
- `advancedcorerealms.user.base`: Allows opening the main menu.
- `advancedcorerealms.user.create`: Allows creating new realms.
- `advancedcorerealms.limit.realms.<count>`: Sets the maximum number of realms a player can own (e.g., `advancedcorerealms.limit.realms.3`).
- `advancedcorerealms.donor.create`: (Example) A permission for donors to create realms, can be configured in `main_menu.yml`.

### Admin Permissions
- `advancedcorerealms.admin.*`: Grants all administrative permissions.
- `advancedcorerealms.admin.reload`: Allows reloading the plugin's configuration.
- `advancedcorerealms.admin.transfer`: Allows transferring realm ownership.
- `advancedcorerealms.admin.delete`: Allows deleting any player's realm.

## PlaceholderAPI Placeholders

If PlaceholderAPI is installed, you can use the following placeholders:

- `%advancedcorerealms_player_realms_count%`: The number of realms a player owns.
- `%advancedcorerealms_current_realm_name%`: The name of the realm the player is currently in.
- `%advancedcorerealms_current_realm_owner%`: The owner of the realm the player is currently in.
- ...and many more! Check the plugin's code for a full list.

## For Developers

AdvancedCoreRealms provides a simple API to access its data.

**Get the API instance:**
```java
AdvancedCoreRealms plugin = (AdvancedCoreRealms) Bukkit.getPluginManager().getPlugin("AdvancedCoreRealms");
```

**Example Usage:**
```java
// Get a player's owned realms
List<Realm> realms = plugin.getWorldDataManager().getPlayerRealms(player.getUniqueId());

// Get a specific realm by name
Realm realm = plugin.getWorldDataManager().getRealm("my_awesome_realm");
```