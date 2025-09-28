package com.minekarta.advancedcorerealms.worldborder;

/**
 * Enum representing the available world border colors
 */
public enum BorderColor {
    BLUE,
    GREEN,
    RED;
    
    /**
     * Get the border color from a string name
     */
    public static BorderColor fromString(String name) {
        for (BorderColor color : values()) {
            if (color.name().equalsIgnoreCase(name)) {
                return color;
            }
        }
        return BLUE; // Default to BLUE
    }
}