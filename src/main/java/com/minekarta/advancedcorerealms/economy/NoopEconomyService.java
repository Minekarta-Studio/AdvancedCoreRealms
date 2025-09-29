package com.minekarta.advancedcorerealms.economy;

import com.minekarta.advancedcorerealms.AdvancedCoreRealms;
import org.bukkit.entity.Player;

import java.util.logging.Logger;

public class NoopEconomyService implements EconomyService {

    private final Logger logger;

    public NoopEconomyService(AdvancedCoreRealms plugin) {
        this.logger = plugin.getLogger();
    }

    @Override
    public boolean isEnabled() {
        return false;
    }

    @Override
    public boolean hasBalance(Player player, double amount) {
        warn();
        return false;
    }

    @Override
    public boolean withdraw(Player player, double amount) {
        warn();
        return false;
    }

    @Override
    public boolean deposit(Player player, double amount) {
        warn();
        return false;
    }

    @Override
    public double getBalance(Player player) {
        warn();
        return 0.0;
    }

    @Override
    public String format(double amount) {
        return String.valueOf(amount);
    }

    private void warn() {
        logger.warning("An economy transaction was attempted, but Vault is not installed or no economy plugin is present.");
        logger.warning("Please install Vault and an economy plugin to enable economy features.");
    }
}