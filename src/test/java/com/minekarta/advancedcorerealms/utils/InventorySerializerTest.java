package com.minekarta.advancedcorerealms.utils;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;

// Note: This test requires a mocked Bukkit environment to run properly,
// as it deals with Bukkit classes like ItemStack.
// We assume the test runner (e.g., Maven Surefire) is configured for this.
@Disabled("Requires a mocked Bukkit environment (e.g., MockBukkit) to run.")
public class InventorySerializerTest {

    @Test
    void testSerializationRoundtrip() throws IOException, ClassNotFoundException {
        // This test will likely fail without a MockBukkit environment,
        // but it serves as a structural placeholder.
        // We create a basic item stack.
        ItemStack[] originalItems = new ItemStack[]{
            new ItemStack(Material.STONE, 1),
            null,
            new ItemStack(Material.DIRT, 64)
        };

        // Serialize to Base64
        String serialized = InventorySerializer.toBase64(originalItems);
        assertNotNull(serialized, "Serialized string should not be null.");
        assertFalse(serialized.isEmpty(), "Serialized string should not be empty.");

        // Deserialize from Base64
        ItemStack[] deserializedItems = InventorySerializer.fromBase64(serialized);
        assertNotNull(deserializedItems, "Deserialized array should not be null.");

        // Check if the arrays are equal
        assertEquals(originalItems.length, deserializedItems.length, "Array lengths should match.");
        // This direct comparison will fail without proper mock setup for ItemStack.equals()
        // assertArrayEquals(originalItems, deserializedItems);

        // Manual comparison as a fallback
        for(int i = 0; i < originalItems.length; i++) {
            assertEquals(originalItems[i], deserializedItems[i], "Item at index " + i + " should be equal.");
        }
    }

    @Test
    void testEmptyInventorySerialization() throws IOException, ClassNotFoundException {
        ItemStack[] originalItems = new ItemStack[0];
        String serialized = InventorySerializer.toBase64(originalItems);
        ItemStack[] deserializedItems = InventorySerializer.fromBase64(serialized);
        assertEquals(0, deserializedItems.length, "Deserialized array of an empty inventory should be empty.");
    }

    @Test
    void testNullInventorySerialization() throws IOException, ClassNotFoundException {
        ItemStack[] originalItems = new ItemStack[5]; // All slots are null
        String serialized = InventorySerializer.toBase64(originalItems);
        ItemStack[] deserializedItems = InventorySerializer.fromBase64(serialized);
        assertEquals(5, deserializedItems.length, "Array length should be preserved.");
        for (ItemStack item : deserializedItems) {
            assertNull(item, "All items in the deserialized array should be null.");
        }
    }
}