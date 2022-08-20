package dev.geco.gmusic.cmd;

import org.bukkit.command.*;
import org.bukkit.entity.Player;

import dev.geco.gmusic.main.GMusicMain;

public class GMusicReloadCommand implements CommandExecutor {
	
	private final GMusicMain GPM;
	
    public GMusicReloadCommand(GMusicMain GPluginMain) { GPM = GPluginMain; }
	
	@Override
	public boolean onCommand(CommandSender s, Command c, String l, String[] a) {
		if(s instanceof Player) {
			Player p = (Player) s;
			if(p.hasPermission(GPM.NAME + "." + GPM.NAME + "Reload") || p.hasPermission(GPM.NAME + ".*")) {
				GPM.reload(s);
				GPM.getMManager().sendMessage(p, "Messages.command-config-reload");
			} else GPM.getMManager().sendMessage(p, "Messages.command-permission-error");
		} else {
			GPM.reload(s);
			GPM.getMManager().sendMessage(s, "Messages.command-config-reload");
		}
		return true;
	}
	
}