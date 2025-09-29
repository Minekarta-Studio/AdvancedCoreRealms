package com.minekarta.advancedcorerealms.transactions;

import com.minekarta.advancedcorerealms.AdvancedCoreRealms;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class TransactionLogger {

    private final AdvancedCoreRealms plugin;
    private final File transactionFile;

    public TransactionLogger(AdvancedCoreRealms plugin) {
        this.plugin = plugin;
        this.transactionFile = new File(plugin.getDataFolder(), "transactions.yml");
        if (!transactionFile.exists()) {
            try {
                transactionFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().log(java.util.logging.Level.SEVERE, "Could not create transactions.yml file!", e);
            }
        }
    }

    public void log(String realmName, UUID payer, String upgradeType, String oldValue, String newValue, double price) {
        TransactionRecord record = new TransactionRecord(realmName, payer, upgradeType, oldValue, newValue, price);
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            synchronized (transactionFile) {
                try {
                    YamlConfiguration config = YamlConfiguration.loadConfiguration(transactionFile);
                    List<Map<?, ?>> transactions = config.getMapList("transactions");
                    if (transactions == null) {
                        transactions = new ArrayList<>();
                    }
                    transactions.add(record.serialize());
                    config.set("transactions", transactions);
                    config.save(transactionFile);
                } catch (IOException e) {
                    plugin.getLogger().log(java.util.logging.Level.SEVERE, "Failed to log transaction: " + e.getMessage(), e);
                }
            }
        });
    }
}