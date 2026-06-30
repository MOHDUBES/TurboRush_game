package com.turborush.game.models;

public class Track {
    public final int id;
    public final String name;
    public final int unlockCost;
    public final GameState.WorldTheme theme;
    
    // 0 = Locked, 1 = Owned, 2 = Selected
    public int ownershipState = 0;

    public Track(int id, String name, int unlockCost, GameState.WorldTheme theme) {
        this.id = id;
        this.name = name;
        this.unlockCost = unlockCost;
        this.theme = theme;
    }
}
