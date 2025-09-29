package com.minekarta.advancedcorerealms.menu.menus;

import com.minekarta.advancedcorerealms.AdvancedCoreRealms;
import com.minekarta.advancedcorerealms.data.object.Realm;
import com.minekarta.advancedcorerealms.manager.RealmManager;
import com.minekarta.advancedcorerealms.menu.Menu;
import com.minekarta.advancedcorerealms.menu.MenuManager;
import com.minekarta.advancedcorerealms.upgrades.UpgradeManager;
import com.minekarta.advancedcorerealms.upgrades.definitions.BorderTier;
import com.minekarta.advancedcorerealms.upgrades.definitions.DifficultyUpgrade;
import com.minekarta.advancedcorerealms.upgrades.definitions.KeepLoadedUpgrade;
import com.minekarta.advancedcorerealms.upgrades.definitions.MemberSlotTier;
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
import java.util.Optional;

public class UpgradeMenu extends Menu {

    private final AdvancedCoreRealms plugin;
    private final MenuManager menuManager;
    private final FileConfiguration menuConfig;
    private final UpgradeManager upgradeManager;
    private final RealmManager realmManager;
    private Realm realm; // Will be loaded asynchronously
    private final Map<Integer, Runnable> slotActions = new HashMap<>();

    public UpgradeMenu(AdvancedCoreRealms plugin, Player player, FileConfiguration menuConfig, MenuManager menuManager) {
        super(plugin, player, menuConfig.getString("title", "Realm Upgrades"), menuConfig.getInt("size", 54));
        this.plugin = plugin;
        this.menuManager = menuManager;
        this.menuConfig = menuConfig;
        this.upgradeManager = plugin.getUpgradeManager();
        this.realmManager = plugin.getRealmManager();
        loadAndSetItems();
    }

    private void loadAndSetItems() {
        inventory.setItem(22, createGuiItem(Material.CLOCK, "&7Loading realm data..."));

        realmManager.getRealmsByOwner(player.getUniqueId()).thenAccept(realms -> {
            this.realm = realms.stream().findFirst().orElse(null);
            Bukkit.getScheduler().runTask(plugin, this::setMenuItems);
        });
    }

    private void setMenuItems() {
        inventory.clear(); // Clear loading item
        if (realm == null) {
            inventory.setItem(22, createGuiItem(Material.BARRIER, "<red>No Realm Found", "<gray>You must own a realm to purchase upgrades."));
            // Add a back button even if no realm is found
            ConfigurationSection backButtonConfig = menuConfig.getConfigurationSection("items.back_button");
            if (backButtonConfig != null) {
                int slot = backButtonConfig.getInt("slot");
                Material material = Material.matchMaterial(backButtonConfig.getString("material"));
                String name = backButtonConfig.getString("name");
                List<String> lore = backButtonConfig.getStringList("lore");
                inventory.setItem(slot, createGuiItem(material, name, lore.toArray(new String[0])));
                slotActions.put(slot, () -> menuManager.openMainMenu(player));
            }
            return;
        }

        addCurrentStatsItem();
        addBorderUpgradeItem();
        addMemberSlotUpgradeItem();
        addDifficultyUpgradeItems();
        addKeepLoadedUpgradeItem();

        ConfigurationSection items = menuConfig.getConfigurationSection("items");
        if (items != null) {
            for (String key : items.getKeys(false)) {
                ConfigurationSection itemConfig = items.getConfigurationSection(key);
                if (itemConfig == null) continue;

                if ("filler".equals(key)) {
                    Material material = Material.matchMaterial(itemConfig.getString("material", "GRAY_STAINED_GLASS_PANE"));
                    ItemStack filler = createGuiItem(material, " ");
                    fillWith(filler);
                    continue;
                }

                int slot = itemConfig.getInt("slot");
                Material material = Material.matchMaterial(itemConfig.getString("material"));
                String name = itemConfig.getString("name");
                List<String> lore = itemConfig.getStringList("lore");
                inventory.setItem(slot, createGuiItem(material, name, lore.toArray(new String[0])));

                if (itemConfig.getString("action", "").equals("back")) {
                    slotActions.put(slot, () -> menuManager.openMainMenu(player));
                }
            }
        }
    }

    private void addCurrentStatsItem() {
        int slot = menuConfig.getInt("special-items.current-stats.slot", 4);
        Material material = Material.matchMaterial(menuConfig.getString("special-items.current-stats.material", "BOOK"));
        String name = menuConfig.getString("special-items.current-stats.name", "<green>Current Realm Stats");
        List<String> lore = List.of(
                "<gray>Border Size: <white>" + realm.getBorderSize() + " blocks",
                "<gray>Difficulty: <white>" + realm.getDifficulty(),
                "<gray>Member Limit: <white>" + realm.getMaxPlayers(),
                "<gray>Keep Loaded: <white>" + (realm.isKeepLoaded() ? "<green>Yes" : "<red>No")
        );
        inventory.setItem(slot, createGuiItem(material, name, lore.toArray(new String[0])));
    }

    private void addBorderUpgradeItem() {
        Optional<BorderTier> nextTierOpt = upgradeManager.getNextBorderTier(realm);
        int slot = menuConfig.getInt("special-items.border-upgrade.slot", 20);
        if (nextTierOpt.isPresent()) {
            BorderTier tier = nextTierOpt.get();
            ItemStack item = createGuiItem(Material.GRASS_BLOCK, "<gold>Upgrade Border",
                    "<gray>Next Tier: <white>" + tier.getSize() + " blocks",
                    "<gray>Cost: <green>" + plugin.getEconomyService().format(tier.getPrice()),
                    "",
                    "<yellow>Click to purchase!");
            inventory.setItem(slot, item);
            slotActions.put(slot, () -> {
                String title = "<blue>Confirm: Border Upgrade";
                List<String> summary = List.of(
                        "<gray>Upgrade: <white>World Border",
                        "<gray>New Size: <white>" + tier.getSize() + "x" + tier.getSize() + " blocks",
                        "",
                        "<gray>Price: <green>" + plugin.getEconomyService().format(tier.getPrice())
                );
                Runnable onConfirm = () -> upgradeManager.purchaseBorderUpgrade(player, realm, tier);
                Runnable onCancel = () -> new UpgradeMenu(plugin, player, menuConfig, menuManager).open();
                new UpgradeConfirmMenu(plugin, player, title, summary, onConfirm, onCancel).open();
            });
        } else {
            ItemStack item = createGuiItem(Material.BARRIER, "<gold>Upgrade Border", "<green>You have the maximum border size!");
            inventory.setItem(slot, item);
        }
    }

    private void addMemberSlotUpgradeItem() {
        Optional<MemberSlotTier> nextTierOpt = upgradeManager.getNextMemberSlotTier(realm);
        int slot = menuConfig.getInt("special-items.members-upgrade.slot", 22);
        if (nextTierOpt.isPresent()) {
            MemberSlotTier tier = nextTierOpt.get();
            int newTotal = plugin.getRealmConfig().getBaseMaxPlayers() + tier.getAdditionalSlots();
            ItemStack item = createGuiItem(Material.PLAYER_HEAD, "<gold>Upgrade Member Slots",
                    "<gray>Next Tier: <white>" + newTotal + " members",
                    "<gray>Cost: <green>" + plugin.getEconomyService().format(tier.getPrice()),
                    "",
                    "<yellow>Click to purchase!");
            inventory.setItem(slot, item);
            slotActions.put(slot, () -> {
                String title = "<blue>Confirm: Member Slots";
                List<String> summary = List.of(
                        "<gray>Upgrade: <white>Member Slots",
                        "<gray>New Limit: <white>" + newTotal + " players",
                        "",
                        "<gray>Price: <green>" + plugin.getEconomyService().format(tier.getPrice())
                );
                Runnable onConfirm = () -> upgradeManager.purchaseMemberSlotUpgrade(player, realm, tier);
                Runnable onCancel = () -> new UpgradeMenu(plugin, player, menuConfig, menuManager).open();
                new UpgradeConfirmMenu(plugin, player, title, summary, onConfirm, onCancel).open();
            });
        } else {
            ItemStack item = createGuiItem(Material.BARRIER, "<gold>Upgrade Member Slots", "<green>You have the maximum member slots!");
            inventory.setItem(slot, item);
        }
    }

    private void addDifficultyUpgradeItems() {
        int peacefulSlot = menuConfig.getInt("special-items.difficulty-peaceful.slot", 38);
        int easySlot = menuConfig.getInt("special-items.difficulty-easy.slot", 39);
        int normalSlot = menuConfig.getInt("special-items.difficulty-normal.slot", 40);
        int hardSlot = menuConfig.getInt("special-items.difficulty-hard.slot", 41);
        Map<String, Integer> difficultySlots = Map.of("peaceful", peacefulSlot, "easy", easySlot, "normal", normalSlot, "hard", hardSlot);

        for (Map.Entry<String, DifficultyUpgrade> entry : upgradeManager.getDifficultyUpgrades().entrySet()) {
            String diff = entry.getKey();
            DifficultyUpgrade upgrade = entry.getValue();
            if (!difficultySlots.containsKey(diff)) continue;
            int slot = difficultySlots.get(diff);

            if (realm.getDifficulty().equalsIgnoreCase(diff)) {
                ItemStack item = createGuiItem(Material.GREEN_WOOL, "<green>Difficulty: " + diff, "<gray>This is your current difficulty.");
                inventory.setItem(slot, item);
            } else {
                ItemStack item = createGuiItem(Material.WHITE_WOOL, "<gold>Change Difficulty to " + diff,
                        "<gray>Cost: <green>" + plugin.getEconomyService().format(upgrade.getPrice()), "", "<yellow>Click to purchase!");
                inventory.setItem(slot, item);
                slotActions.put(slot, () -> {
                    String title = "<blue>Confirm: Difficulty";
                    List<String> summary = List.of(
                            "<gray>Upgrade: <white>Difficulty",
                            "<gray>New Difficulty: <white>" + upgrade.getId(),
                            "",
                            "<gray>Price: <green>" + plugin.getEconomyService().format(upgrade.getPrice())
                    );
                    Runnable onConfirm = () -> upgradeManager.purchaseDifficultyUpgrade(player, realm, upgrade);
                    Runnable onCancel = () -> new UpgradeMenu(plugin, player, menuConfig, menuManager).open();
                    new UpgradeConfirmMenu(plugin, player, title, summary, onConfirm, onCancel).open();
                });
            }
        }
    }

    private void addKeepLoadedUpgradeItem() {
        KeepLoadedUpgrade upgrade = upgradeManager.getKeepLoadedUpgrade();
        int slot = menuConfig.getInt("special-items.keep-loaded-upgrade.slot", 24);
        if (upgrade == null) return;

        if (realm.isKeepLoaded()) {
            ItemStack item = createGuiItem(Material.ENDER_EYE, "<gold>Keep Loaded", "<green>This feature is already enabled.");
            inventory.setItem(slot, item);
        } else {
            ItemStack item = createGuiItem(Material.ENDER_PEARL, "<gold>Enable Keep Loaded",
                    "<gray>Cost: <green>" + plugin.getEconomyService().format(upgrade.getPrice()),
                    "<gray>This keeps your realm loaded even when empty.",
                    "", "<yellow>Click to purchase!");
            inventory.setItem(slot, item);
            slotActions.put(slot, () -> {
                String title = "<blue>Confirm: Keep Loaded";
                List<String> summary = List.of(
                        "<gray>Upgrade: <white>Keep Loaded",
                        "<gray>New State: <green>Enabled",
                        "",
                        "<gray>Price: <green>" + plugin.getEconomyService().format(upgrade.getPrice())
                );
                Runnable onConfirm = () -> upgradeManager.purchaseKeepLoadedUpgrade(player, realm, upgrade);
                Runnable onCancel = () -> new UpgradeMenu(plugin, player, menuConfig, menuManager).open();
                new UpgradeConfirmMenu(plugin, player, title, summary, onConfirm, onCancel).open();
            });
        }
    }

    @Override
    public void handleMenu(InventoryClickEvent e) {
        if (!e.getInventory().equals(inventory)) return;
        e.setCancelled(true);

        if (this.realm == null) {
            if (slotActions.containsKey(e.getSlot())) {
                slotActions.get(e.getSlot()).run();
            }
            return;
        }

        if (slotActions.containsKey(e.getSlot())) {
            slotActions.get(e.getSlot()).run();
        }
    }
}