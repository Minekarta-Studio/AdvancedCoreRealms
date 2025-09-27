
-----

### **Project Documentation: AdvancedCoreRealms**

#### **1. Project Summary**

**AdvancedCoreRealms** is a comprehensive PaperMC plugin designed to provide players with a "private realm" experience on a Minecraft server. This plugin allows players to create, manage, and share their own personal worlds (of either *flat* or *vanilla* type) with friends.

Featuring capabilities such as per-world separated inventories, an easy-to-use invitation system, and an intuitive Graphical User Interface (GUI), AdvancedCoreRealms aims to enhance player engagement and provide personal creative spaces without impacting the main server world. The plugin is also equipped with a robust set of admin commands for management and moderation.

**Target Audience:** Survival, Creative, or Towny servers that wish to offer a premium private world feature or as a standard feature for all players.

-----

#### **2. Recommended Project Structure**

To maintain an organized, scalable, and maintainable codebase, the following package structure is recommended:

```
com.yourdomain.advancedcorerealms
├── AdvancedCoreRealms.java         // Main plugin class (onEnable, onDisable)
│
├── commands                        // For all command handler classes
│   ├── RealmsCommand.java          // Main handler for /realms and its subcommands
│   └── ...                         // Additional command logic if needed
│
├── data                            // Classes for managing data storage
│   ├── WorldDataManager.java       // Manages world data (owner, members, settings)
│   ├── PlayerDataManager.java      // Manages player data (inventories, last location)
│   └── object                      // Custom data objects (e.g., Realm.java)
│
├── gui                             // Classes related to the GUI
│   ├── menu                        // Classes for each specific GUI menu
│   │   ├── MainMenu.java
│   │   ├── WorldListMenu.java
│   │   └── WorldSettingsMenu.java
│   └── GuiManager.java             // Manages GUI opening and interactions
│
├── listeners                       // For all event listeners
│   ├── PlayerConnectionListener.java // Handles PlayerJoinEvent, PlayerQuitEvent
│   └── PlayerWorldListener.java    // Handles PlayerChangedWorldEvent (for inventories)
│
├── manager                         // Manager classes for core logic
│   ├── WorldManager.java           // Logic for creating, deleting, loading worlds
│   ├── InviteManager.java          // Manages pending invitations
│   └── LanguageManager.java        // Manages multi-language messages
│
└── utils                           // Utility classes
    └── MessageUtils.java           // Utilities for sending formatted/colored messages
```

-----

#### **3. Configuration Files**

**`plugin.yml`**
The primary metadata file for the plugin.

```yaml
name: AdvancedCoreRealms
version: 1.0.0
main: com.yourdomain.advancedcorerealms.AdvancedCoreRealms
api-version: '1.18' # Or the minimum supported version
author: [YourName]
description: A comprehensive plugin for player-managed personal worlds.
commands:
  realms:
    description: Main command for AdvancedCoreRealms.
    usage: /realms <subcommand>
    aliases: [realm]
```

**`config.yml`**
The main configuration file, editable by server administrators.

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

**`languages/en.yml`, `languages/es.yml`, `languages/id.yml`**
Files for multi-language support.

```yaml
# Example: en.yml
prefix: "&8[&bRealms&8] &r"
command:
  help: "&aShowing help for AdvancedCoreRealms..."
  reloaded: "&aConfiguration reloaded successfully."
world:
  created: "&aSuccessfully created your new realm named &e%world%&a."
  deleted: "&cSuccessfully deleted your realm &e%world%&c."
  teleport: "&7Teleporting you to &e%world%&7..."
error:
  no-permission: "&cYou do not have permission to use this command."
  world-exists: "&cA realm with that name already exists."
  not-owner: "&cYou are not the owner of this realm."
```

-----

#### **4. Permissions System**

All permissions use the `advancedcorerealms.` prefix.

**Player Permissions:**

* `advancedcorerealms.user.base` - Grants access to open the main GUI with `/realms`.
* `advancedcorerealms.user.help` - Allows the user to view the help command.
* `advancedcorerealms.user.create` - Allows the user to create new realms.
* `advancedcorerealms.user.delete` - Allows the user to delete their own realms.
* `advancedcorerealms.user.teleport` - Allows the user to teleport to their realms.
* `advancedcorerealms.user.back` - Allows the user to return to their previous location.
* `advancedcorerealms.user.list` - Allows the user to list their accessible realms.
* `advancedcorerealms.user.invite` - Allows the user to invite others to their realms.
* `advancedcorerealms.user.accept` - Allows the user to accept a realm invitation.
* `advancedcorerealms.user.deny` - Allows the user to deny a realm invitation.
* `advancedcorerealms.limit.realms.<amount>` - (e.g., `advancedcorerealms.limit.realms.5`) Sets the maximum number of realms a player can own.

**Admin Permissions:**

* `advancedcorerealms.admin.*` - Grants all administrative permissions.
* `advancedcorerealms.admin.reload` - Allows use of `/realms reload`.
* `advancedcorerealms.admin.transfer` - Allows transferring ownership of a realm.
* `advancedcorerealms.admin.vanilla` - Grants access to all `/realms vanilla` subcommands.
* `advancedcorerealms.admin.teleport.others` - Allows teleporting to any realm without an invitation.

-----

#### **5. Command Implementation Details**

* **/realms, /realms gui, /realms list**

    * **Action:** Checks for `advancedcorerealms.user.base` permission. Opens the main GUI, which displays a list of the player's owned realms and realms they are invited to.

* **/realms create \<name\>**

    * **Action:** Checks for `advancedcorerealms.user.create` permission and the player's realm limit. Validates the name (no illegal characters, not a duplicate). Uses the PaperMC API (`WorldCreator`) to generate a new world asynchronously with the type specified in `config.yml`. Saves the world's owner data.

* **/realms delete \<world\>**

    * **Action:** Checks for `advancedcorerealms.user.delete` permission. Verifies that the player is the owner of the specified realm. Teleports all players out of the world being deleted. Unloads and deletes the world files from the server. Removes the world's data from the data file.

* **/realms tp/teleport \<world\>**

    * **Action:** Checks for `advancedcorerealms.user.teleport` permission. Ensures the player is either the owner or has been invited. Saves the player's current location (for `/realms back`). If `separate-inventories` is enabled, saves the current world's inventory and loads/creates the inventory for the destination realm. Teleports the player.

* **/realms invite \<world\> \<player\>**

    * **Action:** Checks for `advancedcorerealms.user.invite` permission. Verifies realm ownership. Sends an invitation message to the target player and stores it in the `InviteManager` with a timestamp for expiration.

* **/realms accept/deny**

    * **Action:** Checks for `advancedcorerealms.user.accept` or `deny` permission. Checks for a pending invitation for that player. If accepted, adds the player to the realm's member list and teleports them. If denied, removes the pending invitation.

* **/realms transfer \<world\> \<player\>**

    * **Action:** Checks for `advancedcorerealms.admin.transfer` permission. Validates the world and target player. Changes the owner UUID in the data file.

* **/realms reload**

    * **Action:** Checks for `advancedcorerealms.admin.reload` permission. Reloads `config.yml` and all language files.

-----

#### **6. Core Logic & Data Management**

* **World Persistence:**

    * Utilize a custom YAML file (e.g., `worlds.yml`) or an SQLite database for data storage to ensure data persists across restarts.
    * The data structure for each world should include:
        * `world-name` (The folder name of the world)
        * `owner-uuid` (The UUID of the owner)
        * `members` (A list of UUIDs of invited players)
        * `settings` (e.g., `is-peaceful: true`)

* **Inventory Management:**

    * When a player moves from a main world to a realm (or vice-versa):
        1.  Listen for the `PlayerChangedWorldEvent`.
        2.  Identify if the source or destination world is a realm.
        3.  If so, save the player's current inventory to a separate file (e.g., `playerdata/<uuid>/<world_name>.yml`).
        4.  Clear the player's inventory.
        5.  Load the inventory data from the file corresponding to the destination world. If none exists, the player will have a fresh, empty inventory.
    * This process is reversed when the player returns.

* **Multi-Language Support:**

    * The `LanguageManager` will load the language `.yml` file specified in `config.yml` on plugin startup.
    * Create a utility method like `sendMessage(Player player, String key, String... placeholders)` that fetches a message string by its key (e.g., "world.created"), replaces placeholders (like `%world%`) with dynamic values, and sends it to the player.