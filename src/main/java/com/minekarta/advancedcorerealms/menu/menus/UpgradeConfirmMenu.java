package com.minekarta.advancedcorerealms.menu.menus;

import com.minekarta.advancedcorerealms.AdvancedCoreRealms;
import com.minekarta.advancedcorerealms.menu.Menu;
import com.minekarta.advancedcorerealms.menu.MenuManager;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.function.Consumer;

public class UpgradeConfirmMenu extends Menu {

    private final Runnable onConfirm;
    private final Runnable onCancel;

    public UpgradeConfirmMenu(AdvancedCoreRealms plugin, Player player, String title, List<String> summary, Runnable onConfirm, Runnable onCancel) {
        super(plugin, player, title, 27); // 3-row inventory for confirmation
        this.onConfirm = onConfirm;
        this.onCancel = onCancel;
        setMenuItems(summary);
    }

    private void setMenuItems(List<String> summary) {
        // Confirmation Item (e.g., the upgrade itself)
        ItemStack summaryItem = createGuiItem(Material.PAPER, "<yellow>Purchase Summary", summary.toArray(new String[0]));
        inventory.setItem(13, summaryItem); // Center slot

        // Confirm Button
        ItemStack confirmItem = createGuiItem(Material.GREEN_WOOL, "<green><bold>CONFIRM</bold>", "<gray>Click to complete your purchase.");
        inventory.setItem(11, confirmItem);

        // Cancel Button
        ItemStack cancelItem = createGuiItem(Material.RED_WOOL, "<red><bold>CANCEL</bold>", "<gray>Click to go back.");
        inventory.setItem(15, cancelItem);

        // Filler Glass
        ItemStack filler = createGuiItem(Material.BLACK_STAINED_GLASS_PANE, " ");
        fillWith(filler);
    }

    @Override
    public void handleMenu(InventoryClickEvent e) {
        e.setCancelled(true);
        if (e.getSlot() == 11) { // Confirm
            onConfirm.run();
            player.closeInventory();
        } else if (e.getSlot() == 15) { // Cancel
            onCancel.run();
        }
    }
}