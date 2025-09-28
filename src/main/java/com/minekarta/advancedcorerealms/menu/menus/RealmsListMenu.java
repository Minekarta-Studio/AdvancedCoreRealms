package com.minekarta.advancedcorerealms.menu.menus;

import com.minekarta.advancedcorerealms.AdvancedCoreRealms;
import com.minekarta.advancedcorerealms.data.object.Realm;
import com.minekarta.advancedcorerealms.menu.Menu;
import com.minekarta.advancedcorerealms.menu.MenuManager;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.stream.Collectors;

public class RealmsListMenu extends Menu {

    private final FileConfiguration menuConfig;
    private final MenuManager menuManager;
    private final boolean ownRealms;
    private final int page;

    public RealmsListMenu(AdvancedCoreRealms plugin, Player player, FileConfiguration menuConfig, MenuManager menuManager, boolean ownRealms, int page) {
        super(plugin, player, menuConfig.getString(ownRealms ? "realms_list.my_realms_title" : "realms_list.accessible_realms_title", "Realms"), menuConfig.getInt("realms_list.size", 54));
        this.menuConfig = menuConfig;
        this.menuManager = menuManager;
        this.ownRealms = ownRealms;
        this.page = page;
        setMenuItems();
    }

    private void setMenuItems() {
        ConfigurationSection elements = menuConfig.getConfigurationSection("realms_list.elements");
        if (elements == null) return;

        List<Realm> realms;
        if (ownRealms) {
            realms = plugin.getWorldDataManager().getPlayerRealms(player.getUniqueId());
        } else {
            realms = plugin.getWorldDataManager().getPlayerInvitedRealms(player.getUniqueId());
        }

        int itemsPerPage = 36; // Example: 4 rows of 9 slots
        int startIndex = (page - 1) * itemsPerPage;
        List<Realm> pageRealms = realms.stream().skip(startIndex).limit(itemsPerPage).collect(Collectors.toList());

        int slot = 0;
        for (Realm realm : pageRealms) {
            // Find a free slot, skipping over predefined items
            while (inventory.getItem(slot) != null && slot < inventory.getSize()) {
                slot++;
            }
            if (slot >= inventory.getSize()) break;

            Material mat = realm.isFlat() ? Material.GRASS_BLOCK : Material.STONE;
            String name = "<green>" + realm.getName() + "</green>";
            String status = realm.getBukkitWorld() != null ? "<gray>Status: <green>Loaded" : "<gray>Status: <red>Unloaded";
            inventory.setItem(slot, createGuiItem(mat, name, status));
        }

        // Set static items like back button, next/prev page buttons
        for (String key : elements.getKeys(false)) {
            ConfigurationSection itemConfig = elements.getConfigurationSection(key);
            if (itemConfig == null) continue;

            int itemSlot = itemConfig.getInt("slot", -1);
            if(itemSlot == -1) continue;

            Material material = Material.matchMaterial(itemConfig.getString("material"));
             if (material == null) material = Material.STONE;
            String name = itemConfig.getString("name");
            List<String> lore = itemConfig.getStringList("lore");
            inventory.setItem(itemSlot, createGuiItem(material, name, lore.toArray(new String[0])));
        }

        // Fill remaining slots
        ConfigurationSection glassPaneConfig = elements.getConfigurationSection("glass_panes");
        if (glassPaneConfig != null && glassPaneConfig.getBoolean("fill_remaining", false)) {
            Material material = Material.matchMaterial(glassPaneConfig.getString("material", "BLACK_STAINED_GLASS_PANE"));
            if (material == null) material = Material.BLACK_STAINED_GLASS_PANE;
            String name = glassPaneConfig.getString("name", " ");
            fillWith(createGuiItem(material, name));
        }
    }

    @Override
    public void handleMenu(InventoryClickEvent e) {
        ItemStack clickedItem = e.getCurrentItem();
        if (clickedItem == null || clickedItem.getType().isAir() || clickedItem.getItemMeta() == null) return;

        String displayName = PlainTextComponentSerializer.plainText().serialize(clickedItem.getItemMeta().displayName());

        if (displayName.equalsIgnoreCase("Back")) {
            menuManager.openMainMenu(player);
        } else if (displayName.equalsIgnoreCase("Previous Page")) {
            if (page > 1) {
                new RealmsListMenu(plugin, player, menuConfig, menuManager, ownRealms, page - 1).open();
            }
        } else if (displayName.equalsIgnoreCase("Next Page")) {
            // We need a way to know the max pages to prevent going to an empty page
            // For now, just incrementing. A real implementation needs to check against total realms.
            new RealmsListMenu(plugin, player, menuConfig, menuManager, ownRealms, page + 1).open();
        } else if (clickedItem.getType() == Material.GRASS_BLOCK || clickedItem.getType() == Material.STONE) {
            // It's a realm item, open the management menu for it
            String realmName = PlainTextComponentSerializer.plainText().serialize(clickedItem.getItemMeta().displayName());
            menuManager.openRealmManagementMenu(player, realmName);
        }
    }
}