package com.minekarta.advancedcorerealms.menu.menus;

import com.minekarta.advancedcorerealms.AdvancedCoreRealms;
import com.minekarta.advancedcorerealms.data.object.Realm;
import com.minekarta.advancedcorerealms.manager.RealmManager;
import com.minekarta.advancedcorerealms.menu.Menu;
import com.minekarta.advancedcorerealms.menu.MenuManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class RealmManagementMenu extends Menu {

    private final AdvancedCoreRealms plugin;
    private final RealmManager realmManager;
    private final FileConfiguration menuConfig;
    private final MenuManager menuManager;
    private final String realmName;
    private final boolean fromMyRealms;
    private final Map<Integer, String> slotActions = new HashMap<>();
    private Realm realm; // To hold the loaded realm data

    public RealmManagementMenu(AdvancedCoreRealms plugin, Player player, FileConfiguration menuConfig, MenuManager menuManager, String realmName, boolean fromMyRealms) {
        super(plugin, player, menuConfig.getString("realm_management.title", "Realm Management").replace("[name]", realmName), menuConfig.getInt("realm_management.size", 36));
        this.plugin = plugin;
        this.realmManager = plugin.getRealmManager();
        this.menuConfig = menuConfig;
        this.menuManager = menuManager;
        this.realmName = realmName;
        this.fromMyRealms = fromMyRealms;
        loadAndSetItems();
    }

    private void loadAndSetItems() {
        // Set a loading item
        inventory.setItem(13, createGuiItem(Material.CLOCK, "&7Loading..."));

        realmManager.getRealmByName(realmName).thenAccept(loadedRealm -> {
            this.realm = loadedRealm;
            if (this.realm == null) {
                // If realm not found, close inventory and send message
                Bukkit.getScheduler().runTask(plugin, () -> {
                    player.closeInventory();
                    plugin.getLanguageManager().sendMessage(player, "error.realm_not_found");
                });
                return;
            }
            // Once data is loaded, build the menu on the main thread
            Bukkit.getScheduler().runTask(plugin, this::setMenuItems);
        });
    }

    private void setMenuItems() {
        inventory.clear(); // Clear the loading item
        ConfigurationSection elements = menuConfig.getConfigurationSection("realm_management.elements");
        if (elements == null) return;

        for (String key : elements.getKeys(false)) {
            ConfigurationSection itemConfig = elements.getConfigurationSection(key);
            if (itemConfig == null) continue;

            if ("glass_panes".equals(key)) {
                if (itemConfig.getBoolean("fill_remaining", false)) {
                    Material material = Material.matchMaterial(itemConfig.getString("material", "BLACK_STAINED_GLASS_PANE"));
                    if (material == null) material = Material.BLACK_STAINED_GLASS_PANE;
                    String name = itemConfig.getString("name", " ");
                    fillWith(createGuiItem(material, name));
                }
                continue;
            }

            int slot = itemConfig.getInt("slot");
            String permission = itemConfig.getString("permission_required", "");
            boolean hasPermission = checkPermissions(permission);

            Material material;
            String name;
            List<String> lore;

            if (hasPermission) {
                material = Material.matchMaterial(itemConfig.getString("material"));
                name = itemConfig.getString("name");
                lore = itemConfig.getStringList("lore");
            } else {
                material = Material.matchMaterial(itemConfig.getString("no_permission_material", "BARRIER"));
                name = itemConfig.getString("no_permission_name", "&cNo Permission");
                lore = itemConfig.getStringList("no_permission_lore");
            }

            if (material == null) material = Material.STONE;
            name = name.replace("[name]", realmName);
            lore = lore.stream().map(line -> line.replace("[name]", realmName)).collect(Collectors.toList());

            inventory.setItem(slot, createGuiItem(material, name, lore.toArray(new String[0])));
            if (hasPermission) {
                slotActions.put(slot, key);
            }
        }
    }

    private boolean checkPermissions(String permissions) {
        if (permissions == null || permissions.isEmpty()) {
            return true;
        }

        // Realm object is now available as a field
        boolean isOwner = this.realm != null && this.realm.getOwner().equals(player.getUniqueId());

        for (String perm : permissions.split(",")) {
            perm = perm.trim();
            if ("realm.owner".equalsIgnoreCase(perm) && isOwner) {
                return true;
            }
            if (player.hasPermission(perm)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void handleMenu(InventoryClickEvent e) {
        ItemStack clickedItem = e.getCurrentItem();
        if (clickedItem == null || clickedItem.getType().isAir() || this.realm == null) return; // Don't handle clicks while loading

        String action = slotActions.get(e.getSlot());
        if (action == null) return;

        switch (action) {
            case "teleport":
                plugin.getWorldManager().teleportToRealm(player, realmName);
                player.closeInventory();
                break;
            case "manage_members":
                // Use the loaded realm object
                menuManager.openManageMembersMenu(player, this.realm);
                break;
            case "realm_settings":
                menuManager.openRealmSettingsMenu(player, realmName, fromMyRealms);
                break;
            case "delete_realm":
                player.closeInventory();
                plugin.getLanguageManager().sendMessage(player, "realm.delete_command_info", "%realm%", realmName);
                break;
            case "back_button":
                menuManager.openRealmsListMenu(player, fromMyRealms);
                break;
        }
    }
}