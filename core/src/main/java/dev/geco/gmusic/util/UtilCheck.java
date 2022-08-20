package dev.geco.gmusic.util;

import org.bukkit.Material;
import org.bukkit.block.*;
import org.bukkit.block.data.*;
import org.bukkit.entity.Player;

public class UtilCheck {
	
	public boolean isPlayerSwimming(Player P) {
		
		Block b = P.getEyeLocation().getBlock();
		
		return b.isLiquid() || (b.getBlockData() instanceof Waterlogged && ((Waterlogged) b.getBlockData()).isWaterlogged()) || b.getType() == Material.KELP_PLANT  || b.getType() == Material.SEAGRASS  || b.getType() == Material.TALL_SEAGRASS;
		
	}
	
}