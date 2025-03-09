package dev.geco.gmusic.util;

import org.bukkit.Location;

public class SteroNoteUtil {

    private final double[] cos = new double[360];
    private final double[] sin = new double[360];

    public SteroNoteUtil() {
        for(int angdeg = 0; angdeg < 360; angdeg++) {
            cos[angdeg] = Math.cos(Math.toRadians(angdeg));
            sin[angdeg] = Math.sin(Math.toRadians(angdeg));
        }
    }

    public Location convertToStero(Location location, float offset) {
        float yaw = location.getYaw();
        return location.clone().add(cos[(int) (yaw + 360) % 360] * offset, 0, sin[(int) (yaw + 360) % 360] * offset);
    }

}