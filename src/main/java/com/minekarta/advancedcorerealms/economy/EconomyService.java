package com.minekarta.advancedcorerealms.economy;

import org.bukkit.entity.Player;

import java.util.UUID;

/**
 * An interface for handling economy transactions within the AdvancedCoreRealms plugin.
 * This provides an abstraction layer over a potential economy provider like Vault.
 */
public interface EconomyService {

    /**
     * Checks if the economy service is enabled and functional.
     *
     * @return true if the economy service is enabled, false otherwise.
     */
    boolean isEnabled();

    /**
     * Checks if a player has at least a certain amount in their account.
     *
     * @param player The player to check the balance of.
     * @param amount The amount to check for.
     * @return true if the player has enough, false otherwise.
     */
    boolean hasBalance(Player player, double amount);

    /**
     * Withdraws a specified amount from a player's account.
     *
     * @param player The player to withdraw from.
     * @param amount The amount to withdraw.
     * @return true if the withdrawal was successful, false otherwise.
     */
    boolean withdraw(Player player, double amount);

    /**
     * Deposits a specified amount into a player's account.
     *
     * @param player The player to deposit to.
     * @param amount The amount to deposit.
     * @return true if the deposit was successful, false otherwise.
     */
    boolean deposit(Player player, double amount);

    /**
     * Gets the balance of a player.
     *
     * @param player The player to get the balance of.
     * @return The player's balance.
     */
    double getBalance(Player player);

    /**
     * Formats the given amount into a currency string.
     *
     * @param amount The amount to format.
     * @return A formatted currency string (e.g., "$1,234.56").
     */
    String format(double amount);
}