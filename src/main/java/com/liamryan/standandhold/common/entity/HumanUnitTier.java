package com.liamryan.standandhold.common.entity;

import java.util.Locale;

public enum HumanUnitTier {
    SURVIVOR_DEFENDER("survivor_defender", "survivor_defender", "Survivor Defender", 0, 16.0D, 2.0D, 0x5A4A32, 0xB0A080),
    ARMY_RIFLEMAN("army_rifleman", "soldier", "Army Rifleman", 1, 20.0D, 4.0D, 0x2F4F3A, 0xC8C0A8),
    HEAVY_SOLDIER("heavy_soldier", "heavy_soldier", "Heavy Soldier", 2, 28.0D, 6.0D, 0x303640, 0x9A9A8A),
    ELITE_SOLDIER("elite_soldier", "elite_soldier", "Elite Soldier", 3, 36.0D, 8.0D, 0x1E3A5F, 0xC0C8D0),
    SUPER_ELITE_SOLDIER("super_elite_soldier", "super_elite_soldier", "Super Elite Soldier", 4, 48.0D, 11.0D, 0x2B1E3F, 0xD0D0E8),
    SPECIAL_PARASITE_DIVISION_OPERATIVE("special_parasite_division_operative", "special_parasite_division_operative", "Special Parasite Division Operative", 5, 60.0D, 14.0D, 0x1A1A1A, 0x50C878);

    private final String id;
    private final String registryName;
    private final String displayName;
    private final int defaultRequiredStage;
    private final double defaultHealth;
    private final double defaultDamage;
    private final int eggPrimaryColor;
    private final int eggSecondaryColor;

    HumanUnitTier(String id, String registryName, String displayName, int defaultRequiredStage, double defaultHealth, double defaultDamage, int eggPrimaryColor, int eggSecondaryColor) {
        this.id = id;
        this.registryName = registryName;
        this.displayName = displayName;
        this.defaultRequiredStage = defaultRequiredStage;
        this.defaultHealth = defaultHealth;
        this.defaultDamage = defaultDamage;
        this.eggPrimaryColor = eggPrimaryColor;
        this.eggSecondaryColor = eggSecondaryColor;
    }

    public String getId() {
        return id;
    }

    public String getRegistryName() {
        return registryName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public int getDefaultRequiredStage() {
        return defaultRequiredStage;
    }

    public double getDefaultHealth() {
        return defaultHealth;
    }

    public double getDefaultDamage() {
        return defaultDamage;
    }

    public int getEggPrimaryColor() {
        return eggPrimaryColor;
    }

    public int getEggSecondaryColor() {
        return eggSecondaryColor;
    }

    public static HumanUnitTier fromId(String id) {
        if (id == null) {
            return null;
        }

        String normalizedId = id.trim().toLowerCase(Locale.ROOT);
        for (HumanUnitTier tier : values()) {
            if (tier.id.equals(normalizedId) || tier.registryName.equals(normalizedId)) {
                return tier;
            }
        }
        return null;
    }

    public static HumanUnitTier getLowestTier() {
        return SURVIVOR_DEFENDER;
    }
}
