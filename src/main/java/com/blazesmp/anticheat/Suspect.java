package com.blazesmp.anticheat;

import java.util.LinkedHashMap;
import java.util.Map;

public final class Suspect {
    private final String playerName;
    private int score;
    private long lastFlag;
    private final Map<String, Integer> reasons = new LinkedHashMap<>();

    public Suspect(String playerName) {
        this.playerName = playerName;
    }

    public void flag(String reason, int points) {
        score += Math.max(1, points);
        reasons.merge(reason, 1, Integer::sum);
        lastFlag = System.currentTimeMillis();
    }

    public String getPlayerName() { return playerName; }
    public int getScore() { return score; }
    public long getLastFlag() { return lastFlag; }
    public Map<String, Integer> getReasons() { return reasons; }
}
