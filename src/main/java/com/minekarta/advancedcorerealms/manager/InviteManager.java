package com.minekarta.advancedcorerealms.manager;

import com.minekarta.advancedcorerealms.AdvancedCoreRealms;
import com.minekarta.advancedcorerealms.data.object.Realm;
import com.minekarta.advancedcorerealms.utils.MessageUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class InviteManager {
    
    private final AdvancedCoreRealms plugin;
    // Map to store pending invites: inviter UUID -> (invitee UUID -> realm name)
    private final Map<UUID, Map<UUID, String>> pendingInvites;
    
    public InviteManager(AdvancedCoreRealms plugin) {
        this.plugin = plugin;
        this.pendingInvites = new HashMap<>();
        
        // Start a repeating task to clean up expired invites
        Bukkit.getScheduler().runTaskTimer(plugin, this::cleanupExpiredInvites, 20*60, 20*60); // Every minute
    }
    
    public void sendInvite(Player sender, Player target, String realmName) {
        // Check if the realm exists and the sender is the owner
        Realm realm = plugin.getWorldDataManager().getRealm(realmName);
        if (realm == null) {
            sender.sendMessage(ChatColor.RED + "Realm does not exist!");
            return;
        }
        
        if (!realm.getOwner().equals(sender.getUniqueId())) {
            MessageUtils.sendMessage(sender, "error.not-owner");
            return;
        }
        
        // Create the invite
        pendingInvites.computeIfAbsent(sender.getUniqueId(), k -> new HashMap<>())
                      .put(target.getUniqueId(), realmName);
        
        // Send invitation message to the target player
        target.sendMessage(ChatColor.GOLD + sender.getName() + " has invited you to join their realm '" + 
                          ChatColor.AQUA + realmName + ChatColor.GOLD + "'!");
        target.sendMessage(ChatColor.YELLOW + "Type " + ChatColor.WHITE + "/realms accept" + 
                          ChatColor.YELLOW + " to join or " + ChatColor.WHITE + "/realms deny" + ChatColor.YELLOW + " to decline.");
        
        sender.sendMessage(ChatColor.GREEN + "Invitation sent to " + target.getName());
    }
    
    public void acceptInvite(Player player) {
        UUID playerId = player.getUniqueId();
        
        // Find the first pending invite for this player
        for (Map.Entry<UUID, Map<UUID, String>> outerEntry : pendingInvites.entrySet()) {
            Map<UUID, String> innerMap = outerEntry.getValue();
            if (innerMap.containsKey(playerId)) {
                String realmName = innerMap.get(playerId);
                
                // Add player to the realm
                Realm realm = plugin.getWorldDataManager().getRealm(realmName);
                if (realm != null) {
                    if (!realm.getMembers().contains(playerId)) {
                        realm.getMembers().add(playerId);
                        plugin.getWorldDataManager().saveData();
                        
                        player.sendMessage(ChatColor.GREEN + "You have joined realm '" + 
                                         ChatColor.AQUA + realmName + ChatColor.GREEN + "'!");
                        
                        // Remove the invite
                        innerMap.remove(playerId);
                        if (innerMap.isEmpty()) {
                            pendingInvites.remove(outerEntry.getKey());
                        }
                        
                        // Teleport to the realm
                        plugin.getWorldManager().teleportToRealm(player, realmName);
                        return;
                    }
                }
            }
        }
        
        player.sendMessage(ChatColor.RED + "You have no pending invitations.");
    }
    
    public void denyInvite(Player player) {
        UUID playerId = player.getUniqueId();
        
        // Find the first pending invite for this player
        for (Map.Entry<UUID, Map<UUID, String>> outerEntry : pendingInvites.entrySet()) {
            Map<UUID, String> innerMap = outerEntry.getValue();
            if (innerMap.containsKey(playerId)) {
                String realmName = innerMap.get(playerId);
                
                player.sendMessage(ChatColor.RED + "You have declined the invitation to join realm '" + 
                                 ChatColor.AQUA + realmName + ChatColor.RED + "'.");
                
                // Remove the invite
                innerMap.remove(playerId);
                if (innerMap.isEmpty()) {
                    pendingInvites.remove(outerEntry.getKey());
                }
                
                return;
            }
        }
        
        player.sendMessage(ChatColor.RED + "You have no pending invitations.");
    }
    
    private void cleanupExpiredInvites() {
        long currentTime = System.currentTimeMillis();
        int timeoutMs = plugin.getConfig().getInt("invite-timeout-seconds", 60) * 1000;
        
        // We'll use a simple approach and clean all invites periodically since
        // we don't have creation timestamps stored for each invite
        // For a more robust solution, we'd need to store timestamps with each invite
    }
    
    public boolean hasPendingInvite(Player player) {
        UUID playerId = player.getUniqueId();
        
        for (Map<UUID, String> invites : pendingInvites.values()) {
            if (invites.containsKey(playerId)) {
                return true;
            }
        }
        
        return false;
    }
}