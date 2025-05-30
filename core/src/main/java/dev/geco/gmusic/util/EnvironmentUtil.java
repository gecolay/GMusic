package dev.geco.gmusic.util;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.Waterlogged;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class EnvironmentUtil {

    private final List<Material> WATER_MATERIALS = new ArrayList<>(); {
        WATER_MATERIALS.add(Material.KELP_PLANT);
        WATER_MATERIALS.add(Material.SEAGRASS);
        WATER_MATERIALS.add(Material.TALL_SEAGRASS);
    }

    public boolean isPlayerSwimming(Player player) {
        Block block = player.getEyeLocation().getBlock();
        if(block.isLiquid()) return true;
        if(block.getBlockData() instanceof Waterlogged && ((Waterlogged) block.getBlockData()).isWaterlogged()) return true;
        return WATER_MATERIALS.contains(block.getType());
    }

}