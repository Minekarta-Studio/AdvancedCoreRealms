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
-   `templates/`: Contains world templates for realm creation.
-   `inventories/`: Stores per-realm player inventories.

### Per-Realm Inventories
To provide a unique and isolated experience, AdvancedCoreRealms now manages player inventories on a per-realm basis.

**How it Works:**
- **Inventory Swapping**: When a player enters a realm, their current inventory (main, armor, off-hand, and ender chest) is saved, and their realm-specific inventory is loaded. When they leave, their original inventory is restored. This prevents unauthorized item transfers between worlds.
- **Data Safety**: The system is designed to be safe against accidental data loss. If a player disconnects mid-swap or the server crashes, their inventory state is securely cached and restored upon their next login.
- **Storage**: Inventories are stored in a human-readable YAML format under `plugins/AdvancedCoreRealms/inventories/`. Each realm has a folder named after its world folder name, containing individual files for each player's inventory (`<player-uuid>.yml`).

### Access Control & Roles
Realms are now protected by a role-based access control system, ensuring only authorized players can build and interact.

**Roles:**
- **OWNER**: The creator of the realm. Has full permissions and cannot be kicked or demoted.
- **ADMIN**: Can manage the realm, including building, interacting, and managing members (promoting, demoting, and kicking).
- **MEMBER**: The default role for invited players. Can build and interact within the realm.
- **VISITOR**: A player who is not a member. Can explore the realm but cannot build, break blocks, or interact with containers and doors.

**Managing Members:**
Realm owners and admins can manage their members through the new **Manage Members GUI**, accessible from the `Realm Management` menu.

### Foldered World Creation (Milestone A)

AdvancedCoreRealms now creates each realm as a separate world folder on the server, providing better isolation and stability. This process is asynchronous to prevent server lag.

**How it Works:**
1. When a player runs `/realms create <name> <template>`, the plugin finds the corresponding template in the `plugins/AdvancedCoreRealms/templates/` folder.
2. It asynchronously copies this template to a new folder under the server's `realms/` directory (this is configurable).
3. The new world is then loaded, configured, and the player is teleported to it.
4. If anything goes wrong, the system automatically cleans up any partial files to prevent clutter.

**Configuration (`config.yml`):**
```yaml
realms:
  templates-folder: "templates"
  server-realms-folder: "realms"   # An empty string "" will create worlds in the server root.
  world-name-format: "acr_{owner}_{name}_{ts}"
  default-border-size: 50
  sanitize:
    max-length: 30
    allowed-regex: "[a-z0-9_-]"
```

**Creating Templates:**
- Create a new folder inside `plugins/AdvancedCoreRealms/templates/`. The name of this folder is the template name (e.g., `vanilla`, `skyblock`).
- Copy a complete world save into this folder (including `level.dat`, `region/`, `data/`, etc.).
- Players can then create realms using this template: `/realms create MyNewRealm vanilla`.

**Note for Server Hosters (Pterodactyl, etc.):**
Some hosting panels may have restrictions on file system access. If the plugin fails to create worlds in a subfolder, you can set `server-realms-folder: ""` in `config.yml`. This will create the realm folders directly in the server's root directory, which is usually permitted.

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
| `/realms create <name> [template]` | Creates a new realm from a template. | `advancedcorerealms.user.create` |
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

### Management Permissions
- `advancedcorerealms.manage`: Grants access to the member management GUI. Intended for realm owners and admins.
- `advancedcorerealms.role.transfer`: Allows a player to use the ownership transfer feature in the GUI.
- `advancedcorerealms.build.public`: Allows building in public realms (if the realm has public building enabled).

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