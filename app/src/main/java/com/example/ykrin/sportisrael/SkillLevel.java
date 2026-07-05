package com.example.ykrin.sportisrael;

public enum SkillLevel {
    CASUAL("casual", "Casual"),
    INTERMEDIATE("intermediate", "Intermediate"),
    COMPETITIVE("competitive", "Competitive"),
    ANY("any", "All levels");

    private final String value;
    private final String displayName;

    SkillLevel(String value, String displayName) {
        this.value = value;
        this.displayName = displayName;
    }

    public String getValue() {
        return value;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static SkillLevel fromValue(String value) {
        if (value == null) {
            return ANY;
        }
        for (SkillLevel level : SkillLevel.values()) {
            if (level.value.equals(value)) {
                return level;
            }
        }
        return ANY;
    }
}
