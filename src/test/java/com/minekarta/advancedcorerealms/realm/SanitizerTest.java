package com.minekarta.advancedcorerealms.realm;

import com.minekarta.advancedcorerealms.config.RealmConfig;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SanitizerTest {

    private RealmCreator realmCreator;
    private Method sanitizeNameMethod;

    @BeforeEach
    void setUp() throws NoSuchMethodException {
        // Mock the config
        RealmConfig mockRealmConfig = mock(RealmConfig.class);
        when(mockRealmConfig.getSanitizeAllowedRegex()).thenReturn("[a-z0-9_-]");
        when(mockRealmConfig.getSanitizeMaxLength()).thenReturn(15);

        // We don't need a full plugin mock, just the config holder
        com.minekarta.advancedcorerealms.AdvancedCoreRealms mockPlugin = mock(com.minekarta.advancedcorerealms.AdvancedCoreRealms.class);
        when(mockPlugin.getRealmConfig()).thenReturn(mockRealmConfig);

        realmCreator = new RealmCreator(mockPlugin);

        // Use reflection to access the private method
        sanitizeNameMethod = RealmCreator.class.getDeclaredMethod("sanitizeName", String.class);
        sanitizeNameMethod.setAccessible(true);
    }

    @ParameterizedTest
    @CsvSource({
            "valid_name, valid_name",
            "MyAwesomeRealm, myawesomrealm", // Test lowercase and length
            "Invalid!@#Name, invalidname",    // Test invalid character removal
            "a-really-long-name-that-is-way-too-big, a-really-long-n", // Test truncation
            "UPPER_CASE, upper_case",      // Test uppercase
            "__--__, __--__",              // Test allowed special chars
            "!@#$%, realm",               // Test empty after sanitization
            "'', realm"                   // Test empty input
    })
    void testSanitizeName(String input, String expected) throws Exception {
        String result = (String) sanitizeNameMethod.invoke(realmCreator, input);
        assertEquals(expected, result);
    }
}