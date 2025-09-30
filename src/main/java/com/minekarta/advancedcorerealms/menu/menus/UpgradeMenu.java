package com.minekarta.advancedcorerealms.menu.menus;

import com.minekarta.advancedcorerealms.AdvancedCoreRealms;
import com.minekarta.advancedcorerealms.data.object.Realm;
import com.minekarta.advancedcorerealms.manager.RealmManager;
import com.minekarta.advancedcorerealms.menu.Menu;
import com.minekarta.advancedcorerealms.menu.MenuManager;
import com.minekarta.advancedcorerealms.worldborder.WorldBorderConfig;
import com.minekarta.advancedcorerealms.worldborder.WorldBorderManager;
import com.minekarta.advancedcorerealms.worldborder.WorldBorderTier;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class UpgradeMenu extends Menu {

    private final AdvancedCoreRealms plugin;
    private final MenuManager menuManager;
    private final FileConfiguration menuConfig;
    private final WorldBorderManager worldBorderManager;
    private final RealmManager realmManager;
    private final WorldBorderConfig worldBorderConfig;
    private Realm realm;
    private final Map<Integer, Runnable> slotActions = new HashMap<>();

    public UpgradeMenu(AdvancedCoreRealms plugin, Player player, FileConfiguration menuConfig, MenuManager menuManager) {
        super(plugin, player, menuConfig.getString("title", "Realm Upgrades"), menuConfig.getInt("size", 54));
        this.plugin = plugin;
        this.menuManager = menuManager;
        this.menuConfig = menuConfig;
        this.worldBorderManager = plugin.getWorldBorderManager();
        this.realmManager = plugin.getRealmManager();
        this.worldBorderConfig = plugin.getWorldBorderConfig();
        loadAndSetItems();
    }

    private void loadAndSetItems() {
        // Since a player can only manage their own realms from this menu, we get the first one.
        this.realm = realmManager.getRealmsByOwner(player.getUniqueId()).stream().findFirst().orElse(null);
        setMenuItems();
    }

    private void setMenuItems() {
        inventory.clear();
        slotActions.clear(); // Clear actions when rebuilding menu
        if (realm == null) {
            inventory.setItem(22, createGuiItem(Material.BARRIER, "<red>No Realm Found", "<gray>You must own a realm to purchase upgrades."));
            addBackButton();
            fillEmptySlots();
            return;
        }

        // Add items to the menu
        addCurrentStatsItem();
        addBorderUpgradeItem();
        addBackButton();
        fillEmptySlots();
    }

    private void addCurrentStatsItem() {
        int slot = 4;
        WorldBorderTier currentTier = worldBorderConfig.getTier(realm.getBorderTierId());
        String borderTierName = (currentTier != null) ? currentTier.getId() : "Unknown";
        double borderSize = (currentTier != null) ? currentTier.getSize() : realm.getBorderSize();

        ItemStack item = createGuiItem(Material.BOOK, "<green>Current Realm Stats",
                "<gray>Border Tier: <white>" + borderTierName,
                "<gray>Border Size: <white>" + borderSize + " blocks",
                "<gray>Difficulty: <white>" + realm.getDifficulty()
        );
        inventory.setItem(slot, item);
    }

    private void addBorderUpgradeItem() {
        int slot = 22;
        WorldBorderTier currentTier = worldBorderConfig.getTier(realm.getBorderTierId());
        if (currentTier == null) {
            currentTier = worldBorderConfig.getDefaultTier();
        }

        final double currentSize = (currentTier != null) ? currentTier.getSize() : 0;

        Optional<WorldBorderTier> nextTierOpt = worldBorderConfig.getAllTiers().values().stream()
                .filter(tier -> tier.getSize() > currentSize)
                .min(Comparator.comparingDouble(WorldBorderTier::getSize));

        if (nextTierOpt.isPresent()) {
            WorldBorderTier nextTier = nextTierOpt.get();
            ItemStack item = createGuiItem(Material.GRASS_BLOCK, "<gold>Upgrade Border",
                    "<gray>Next Tier: <white>" + nextTier.getSize() + " blocks",
                    "<gray>Cost: <green>" + plugin.getEconomyService().format(nextTier.getCostToUpgrade()),
                    "",
                    "<yellow>Click to purchase!");
            inventory.setItem(slot, item);
            slotActions.put(slot, () -> {
                // Directly call the world border manager to handle the upgrade
                worldBorderManager.upgradeBorder(player, realm, nextTier.getId());
                player.closeInventory();
            });
        } else {
            ItemStack item = createGuiItem(Material.BARRIER, "<gold>Upgrade Border", "<green>You have the maximum border size!");
            inventory.setItem(slot, item);
        }
    }

    private void addBackButton() {
        int slot = 49; // Center bottom
        ItemStack item = createGuiItem(Material.ARROW, "<red>Back", "<gray>Return to the main menu.");
        inventory.setItem(slot, item);
        slotActions.put(slot, () -> menuManager.openMainMenu(player));
    }

    private void fillEmptySlots() {
        ItemStack filler = createGuiItem(Material.GRAY_STAINED_GLASS_PANE, " ");
        for (int i = 0; i < inventory.getSize(); i++) {
            if (inventory.getItem(i) == null) {
                inventory.setItem(i, filler);
            }
        }
    }

    @Override
    public void handleMenu(InventoryClickEvent e) {
        if (!e.getInventory().equals(inventory)) return;
        e.setCancelled(true);

        if (slotActions.containsKey(e.getSlot())) {
            slotActions.get(e.getSlot()).run();
        }
    }
}