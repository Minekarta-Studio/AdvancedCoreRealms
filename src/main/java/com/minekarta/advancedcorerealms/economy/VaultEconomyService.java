package com.minekarta.advancedcorerealms.economy;

import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;

public class VaultEconomyService implements EconomyService {

    private Economy economy;

    public VaultEconomyService() {
        this.setupEconomy();
    }

    private void setupEconomy() {
        if (Bukkit.getServer().getPluginManager().getPlugin("Vault") == null) {
            this.economy = null;
            return;
        }
        RegisteredServiceProvider<Economy> rsp = Bukkit.getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            this.economy = null;
            return;
        }
        this.economy = rsp.getProvider();
    }

    @Override
    public boolean isEnabled() {
        return this.economy != null;
    }

    @Override
    public boolean hasBalance(Player player, double amount) {
        if (!isEnabled()) return false;
        return economy.has(player, amount);
    }

    @Override
    public boolean withdraw(Player player, double amount) {
        if (!isEnabled()) return false;
        EconomyResponse response = economy.withdrawPlayer(player, amount);
        return response.transactionSuccess();
    }

    @Override
    public boolean deposit(Player player, double amount) {
        if (!isEnabled()) return false;
        EconomyResponse response = economy.depositPlayer(player, amount);
        return response.transactionSuccess();
    }

    @Override
    public double getBalance(Player player) {
        if (!isEnabled()) return 0.0;
        return economy.getBalance(player);
    }

    @Override
    public String format(double amount) {
        if (!isEnabled()) return String.valueOf(amount);
        return economy.format(amount);
    }
}