package dev.geco.gmusic.util;

import java.util.*;

import org.bukkit.*;
import org.bukkit.block.*;
import org.bukkit.block.data.*;
import org.bukkit.entity.*;

public class MusicUtil {

    private final SteroNoteUtil steroNoteUtil = new SteroNoteUtil();

    private final List<Material> WATER_MATERIALS = new ArrayList<>(); {
        WATER_MATERIALS.add(Material.KELP_PLANT);
        WATER_MATERIALS.add(Material.SEAGRASS);
        WATER_MATERIALS.add(Material.TALL_SEAGRASS);
    }

    public boolean isPlayerSwimming(Player Player) {

        Block block = Player.getEyeLocation().getBlock();

        return block.isLiquid() || (block.getBlockData() instanceof Waterlogged && ((Waterlogged) block.getBlockData()).isWaterlogged()) || WATER_MATERIALS.contains(block.getType());
    }

    public SteroNoteUtil getSteroNoteUtil() { return steroNoteUtil; }

    public static class SteroNoteUtil {

        private final double[] cos = new double[360];
        private final double[] sin = new double[360];

        public SteroNoteUtil() {
            for(int angdeg = 0; angdeg < 360; angdeg++) {
                cos[angdeg] = Math.cos(Math.toRadians(angdeg));
                sin[angdeg] = Math.sin(Math.toRadians(angdeg));
            }
        }

        public Location convertToStero(Location Location, float Shift) {
            float yaw = Location.getYaw();
            return Location.clone().add(cos[(int) (yaw + 360) % 360] * Shift, 0, sin[(int) (yaw + 360) % 360] * Shift);
        }

    }

}