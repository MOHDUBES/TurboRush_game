package com.turborush.game.models;

/** PlayerProgress — All persistent player data. */
public class PlayerProgress {
    public int    totalCoins          = 0;
    public long   bestScore           = 0;
    public long[] topScores           = new long[5];
    public String[] topScoreDates     = new String[5];
    public String ownedVehicleIds     = "0";
    public int    selectedVehicleId   = 0;
    public String ownedTrackIds       = "0";
    public int    selectedTrackId     = 0;
    public String vehicleColorMap     = "";
    public String unlockedColorsMap   = ""; // Format: "vehicleId:0_1_2,vehicleId:0"
    public boolean isMuted            = false; // Actually used for Music
    public boolean isSfxMuted         = false;
    public boolean isNightMode        = false;
    
    // Profile Fields
    public String playerName          = "Racer";
    public int    avatarId            = 0;
    public String avatarUri           = ""; // Local URI for custom photo
    public boolean isLoggedIn         = false;
    public String loginProvider       = ""; // "Google", "Facebook", "Email"
    public int    playerLevel         = 1;
    public long   playerXp            = 0;
    
    // Lifetime Stats
    public int    totalRaces          = 0;
    public long   totalDistance       = 0;
    public float  topSpeedReached     = 0f;

    public PlayerProgress() {}
}
