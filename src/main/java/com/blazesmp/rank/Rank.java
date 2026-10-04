package com.blazesmp.rank;

import net.kyori.adventure.text.format.NamedTextColor;

public enum Rank {
    MEMBER(0, "MEMBER", NamedTextColor.GRAY),
    MEDIA(1, "MEDIA", NamedTextColor.LIGHT_PURPLE),
    MOD(2, "MOD", NamedTextColor.RED),
    DEV(3, "DEV", NamedTextColor.GOLD),
    OWNER(4, "OWNER", NamedTextColor.YELLOW);

    private final int priority;
    private final String displayName;
    private final NamedTextColor color;

    Rank(int priority, String displayName, NamedTextColor color) {
        this.priority = priority;
        this.displayName = displayName;
        this.color = color;
    }

    public int priority() { return priority; }
    public String displayName() { return displayName; }
    public NamedTextColor color() { return color; }

    public boolean canManage(Rank target) {
        return priority > target.priority;
    }

    public static Rank parse(String value) {
        for (Rank rank : values()) {
            if (rank.name().equalsIgnoreCase(value)) return rank;
        }
        return null;
    }
}
