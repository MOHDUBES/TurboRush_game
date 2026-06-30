package com.turborush.game.models;

/** VehicleStats — Normalized performance stats (0–100) for a vehicle. */
public class VehicleStats {
    public int speed;
    public int handling;
    public int fuelEfficiency;
    public int durability;

    public VehicleStats(int speed, int handling, int fuelEfficiency, int durability) {
        this.speed          = speed;
        this.handling       = handling;
        this.fuelEfficiency = fuelEfficiency;
        this.durability     = durability;
    }
}
