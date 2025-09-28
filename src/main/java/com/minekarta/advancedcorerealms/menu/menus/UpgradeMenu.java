package com.minekarta.advancedcorerealms.menu.menus;

import com.minekarta.advancedcorerealms.AdvancedCoreRealms;
import com.minekarta.advancedcorerealms.data.object.Realm;
import com.minekarta.advancedcorerealms.menu.Menu;
import com.minekarta.advancedcorerealms.menu.MenuManager;
import com.minekarta.advancedcorerealms.upgrades.RealmUpgrade;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UpgradeMenu extends Menu {

    private final MenuManager menuManager;
    private final FileConfiguration menuConfig;
    private final Map<Integer, String> slotActions = new HashMap<>();
    private final Map<Integer, String> upgradeSlots = new HashMap<>();

    public UpgradeMenu(AdvancedCoreRealms plugin, Player player, FileConfiguration menuConfig, MenuManager menuManager) {
        super(plugin, player, menuConfig.getString("upgrade_menu.title", "Upgrades"), menuConfig.getInt("upgrade_menu.size", 54));
        this.menuManager = menuManager;
        this.menuConfig = menuConfig;
        setMenuItems();
    }

    private void setMenuItems() {
        ConfigurationSection elements = menuConfig.getConfigurationSection("upgrade_menu.elements");
        if (elements == null) return;

        // Set static items
        for (String key : elements.getKeys(false)) {
            ConfigurationSection itemConfig = elements.getConfigurationSection(key);
            if (itemConfig == null || "glass_panes".equals(key)) continue;

            int itemSlot = itemConfig.getInt("slot", -1);
            if (itemSlot == -1) continue;

            Material material = Material.matchMaterial(itemConfig.getString("material"));
            if (material == null) material = Material.STONE;
            String name = itemConfig.getString("name");
            List<String> lore = itemConfig.getStringList("lore");
            inventory.setItem(itemSlot, createGuiItem(material, name, lore.toArray(new String[0])));
            slotActions.put(itemSlot, key);
        }

        // Set dynamic upgrade items
        Realm playerRealm = plugin.getWorldDataManager().getPlayerRealms(player.getUniqueId()).stream().findFirst().orElse(null);
        if (playerRealm == null) {
            inventory.setItem(22, createGuiItem(Material.BARRIER, "<red>No Realm Found", "<gray>You must own a realm to upgrade it."));
        } else {
            List<RealmUpgrade> upgrades = new ArrayList<>(plugin.getUpgradeManager().getUpgrades());
            int slot = 10; // Starting slot for upgrades
            for (RealmUpgrade upgrade : upgrades) {
                while (inventory.getItem(slot) != null && slot < 45) { // Avoid overwriting static items
                    slot++;
                }
                if (slot >= 45) break;
                inventory.setItem(slot, createUpgradeItem(upgrade, playerRealm));
                upgradeSlots.put(slot, upgrade.getId());
                slot++;
            }
        }

        // Fill remaining
        ConfigurationSection glassPaneConfig = elements.getConfigurationSection("glass_panes");
        if (glassPaneConfig != null && glassPaneConfig.getBoolean("fill_remaining", false)) {
            Material material = Material.matchMaterial(glassPaneConfig.getString("material", "BLACK_STAINED_GLASS_PANE"));
            if (material == null) material = Material.BLACK_STAINED_GLASS_PANE;
            String name = glassPaneConfig.getString("name", " ");
            fillWith(createGuiItem(material, name));
        }
    }

    private ItemStack createUpgradeItem(RealmUpgrade upgrade, Realm realm) {
        int currentLevel = upgrade.getLevel(realm);
        int maxLevel = upgrade.getMaxLevel();
        double cost = upgrade.getCost(currentLevel);

        Material material = Material.matchMaterial(upgrade.getIcon());
        if (material == null) material = Material.STONE;
        String name = "<gold>" + upgrade.getName() + "</gold>";

        List<String> lore = new ArrayList<>();
        lore.add("<gray>" + upgrade.getDescription());
        lore.add(" ");
        lore.add("<gray>Level: <yellow>" + currentLevel + " / " + maxLevel);

        if (currentLevel >= maxLevel) {
            lore.add("<green>Max level reached!");
        } else {
            lore.add("<gray>Cost to upgrade: <green>$" + String.format("%,.2f", cost));
            lore.add(" ");
            lore.add("<yellow>Click to upgrade!");
        }

        return createGuiItem(material, name, lore.toArray(new String[0]));
    }

    @Override
    public void handleMenu(InventoryClickEvent e) {
        ItemStack clickedItem = e.getCurrentItem();
        if (clickedItem == null || clickedItem.getType().isAir()) return;

        int slot = e.getSlot();
        String action = slotActions.get(slot);

        if ("back_button".equals(action)) {
            menuManager.openMainMenu(player);
            return;
        }

        String upgradeId = upgradeSlots.get(slot);
        if (upgradeId != null) {
            handleUpgradeClick(upgradeId);
        }
    }

    private void handleUpgradeClick(String upgradeId) {
        Realm playerRealm = plugin.getWorldDataManager().getPlayerRealms(player.getUniqueId()).stream().findFirst().orElse(null);
        if (playerRealm == null) {
            plugin.getLanguageManager().sendMessage(player, "error.no_realm_to_upgrade");
            return;
        }

        RealmUpgrade clickedUpgrade = plugin.getUpgradeManager().getUpgrade(upgradeId);
        if (clickedUpgrade == null) return;

        int currentLevel = clickedUpgrade.getLevel(playerRealm);
        if (currentLevel >= clickedUpgrade.getMaxLevel()) {
            plugin.getLanguageManager().sendMessage(player, "error.upgrade_max_level");
            return;
        }

        double cost = clickedUpgrade.getCost(currentLevel);
        if (!plugin.getUpgradeManager().hasEnoughMoney(player, cost)) {
            plugin.getLanguageManager().sendMessage(player, "error.not_enough_money", "%cost%", String.format("%,.2f", cost));
            return;
        }

        boolean success = plugin.getUpgradeManager().upgradeRealm(playerRealm, clickedUpgrade.getId(), player);
        if (success) {
            plugin.getLanguageManager().sendMessage(player, "upgrade.success", "%upgrade%", clickedUpgrade.getName());
            new UpgradeMenu(plugin, player, menuConfig, menuManager).open(); // Refresh menu
        } else {
            plugin.getLanguageManager().sendMessage(player, "error.upgrade_failed");
        }
    }
}