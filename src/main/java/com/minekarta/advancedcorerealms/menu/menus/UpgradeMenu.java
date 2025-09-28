package com.minekarta.advancedcorerealms.menu.menus;

import com.minekarta.advancedcorerealms.AdvancedCoreRealms;
import com.minekarta.advancedcorerealms.data.object.Realm;
import com.minekarta.advancedcorerealms.menu.Menu;
import com.minekarta.advancedcorerealms.menu.MenuManager;
import com.minekarta.advancedcorerealms.upgrades.RealmUpgrade;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class UpgradeMenu extends Menu {

    private final MenuManager menuManager;
    private final FileConfiguration menuConfig;

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
        }

        // Set dynamic upgrade items
        Realm playerRealm = plugin.getWorldDataManager().getPlayerRealms(player.getUniqueId()).stream().findFirst().orElse(null);
        if (playerRealm == null) {
            inventory.setItem(22, createGuiItem(Material.BARRIER, "<red>No Realm Found", "<gray>You must own a realm to upgrade it."));
        } else {
            List<RealmUpgrade> upgrades = new ArrayList<>(plugin.getUpgradeManager().getUpgrades());
            int slot = 10; // Starting slot for upgrades
            for (RealmUpgrade upgrade : upgrades) {
                if (slot > 43) break; // Don't override bottom bar
                inventory.setItem(slot, createUpgradeItem(upgrade, playerRealm));
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
        if (material == null) {
            material = Material.STONE;
        }
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
        if (clickedItem == null || clickedItem.getType().isAir() || clickedItem.getItemMeta() == null) return;

        String displayName = PlainTextComponentSerializer.plainText().serialize(clickedItem.getItemMeta().displayName());

        if (displayName.equalsIgnoreCase("Back")) {
            menuManager.openMainMenu(player);
            return;
        }

        Realm playerRealm = plugin.getWorldDataManager().getPlayerRealms(player.getUniqueId()).stream().findFirst().orElse(null);
        if (playerRealm == null) {
            player.sendMessage(ChatColor.RED + "You do not own a realm to upgrade.");
            return;
        }

        // Find the upgrade that was clicked
        RealmUpgrade clickedUpgrade = null;
        for (RealmUpgrade upgrade : plugin.getUpgradeManager().getUpgrades()) {
            if (displayName.equalsIgnoreCase(upgrade.getName())) {
                clickedUpgrade = upgrade;
                break;
            }
        }

        if (clickedUpgrade != null) {
            int currentLevel = clickedUpgrade.getLevel(playerRealm);
            if (currentLevel >= clickedUpgrade.getMaxLevel()) {
                player.sendMessage(ChatColor.RED + "This upgrade is already at the maximum level.");
                return;
            }

            double cost = clickedUpgrade.getCost(currentLevel);
            if (!plugin.getUpgradeManager().hasEnoughMoney(player, cost)) {
                player.sendMessage(ChatColor.RED + "You do not have enough money. Cost: $" + String.format("%,.2f", cost));
                return;
            }

            // Attempt to purchase
            boolean success = plugin.getUpgradeManager().upgradeRealm(playerRealm, clickedUpgrade.getId(), player);
            if (success) {
                player.sendMessage(ChatColor.GREEN + "Successfully upgraded " + clickedUpgrade.getName() + "!");
                // Refresh the menu
                new UpgradeMenu(plugin, player, menuConfig, menuManager).open();
            } else {
                player.sendMessage(ChatColor.RED + "There was an error purchasing the upgrade.");
            }
        }
    }
}