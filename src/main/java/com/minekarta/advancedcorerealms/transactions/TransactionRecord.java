package com.minekarta.advancedcorerealms.transactions;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

public class TransactionRecord {
    private final UUID transactionId;
    private final String realmName;
    private final UUID payer;
    private final String upgradeType;
    private final String oldValue;
    private final String newValue;
    private final double price;
    private final Instant timestamp;

    public TransactionRecord(String realmName, UUID payer, String upgradeType, String oldValue, String newValue, double price) {
        this.transactionId = UUID.randomUUID();
        this.realmName = realmName;
        this.payer = payer;
        this.upgradeType = upgradeType;
        this.oldValue = oldValue;
        this.newValue = newValue;
        this.price = price;
        this.timestamp = Instant.now();
    }

    /**
     * Serializes the record to a map for easy writing to YAML.
     * @return A map representation of the transaction.
     */
    public Map<String, Object> serialize() {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", transactionId.toString());
        map.put("realmId", realmName); // Using realmName as the identifier
        map.put("payer", payer.toString());
        map.put("upgradeType", upgradeType);
        map.put("oldValue", oldValue);
        map.put("newValue", newValue);
        map.put("price", price);
        map.put("timestamp", timestamp.toString());
        return map;
    }
}