package dev.geco.gmusic.cmd.tab;

import java.util.*;

import org.bukkit.command.*;
import org.bukkit.entity.Player;

import dev.geco.gmusic.main.GMusicMain;
import dev.geco.gmusic.objects.Song;

public class GMusicTabCompleter implements TabCompleter {
	
	private GMusicMain GPM;
	
    public GMusicTabCompleter(GMusicMain GPluginMain) { GPM = GPluginMain; }
	
	@Override
	public List<String> onTabComplete(CommandSender s, Command c, String l, String[] a) {
		List<String> Tab = new ArrayList<>();
		List<String> Tab1 = new ArrayList<>();
		try {
			if(s instanceof Player) {
				Player p = (Player) s;
				if(a.length == 1) {
					if(p.hasPermission(GPM.NAME + ".Music.Play") || p.hasPermission(GPM.NAME + ".Music.*") || p.hasPermission(GPM.NAME + ".*")) Tab.add("play");
					if(p.hasPermission(GPM.NAME + ".Music.Playing") || p.hasPermission(GPM.NAME + ".Music.*") || p.hasPermission(GPM.NAME + ".*")) Tab.add("playing");
					if(p.hasPermission(GPM.NAME + ".Music.Random") || p.hasPermission(GPM.NAME + ".Music.*") || p.hasPermission(GPM.NAME + ".*")) Tab.add("random");
					if(p.hasPermission(GPM.NAME + ".Music.Stop") || p.hasPermission(GPM.NAME + ".Music.*") || p.hasPermission(GPM.NAME + ".*")) Tab.add("stop");
					if(p.hasPermission(GPM.NAME + ".Music.Pause") || p.hasPermission(GPM.NAME + ".Music.*") || p.hasPermission(GPM.NAME + ".*")) Tab.add("pause");
					if(p.hasPermission(GPM.NAME + ".Music.Resume") || p.hasPermission(GPM.NAME + ".Music.*") || p.hasPermission(GPM.NAME + ".*")) Tab.add("resume");
					if(p.hasPermission(GPM.NAME + ".Music.Skip") || p.hasPermission(GPM.NAME + ".Music.*") || p.hasPermission(GPM.NAME + ".*")) Tab.add("skip");
					if(p.hasPermission(GPM.NAME + ".Music.Toggle") || p.hasPermission(GPM.NAME + ".Music.*") || p.hasPermission(GPM.NAME + ".*")) Tab.add("toggle");
					if(!a[0].isEmpty()) {
						for(String rt : Tab) if(rt.toLowerCase().startsWith(a[0].toLowerCase())) Tab1.add(rt);
						Tab.clear();
					}
				} else if(a.length == 2) {
					if(a[0].equalsIgnoreCase("play") && (p.hasPermission(GPM.NAME + ".Music.Play") || p.hasPermission(GPM.NAME + ".Music.*") || p.hasPermission(GPM.NAME + ".*"))) for(Song so : GPM.getValues().getSongs()) Tab.add(so.getId());
					if(!a[1].isEmpty()) {
						for(String rt : Tab) if(rt.toLowerCase().startsWith(a[1].toLowerCase())) Tab1.add(rt);
						Tab.clear();
					}
				}
			}
			return Tab.size() == 0 ? GPM.getUtilFormat().sortList(Tab1) : GPM.getUtilFormat().sortList(Tab);
		} catch (Exception e) { }
		return null;
	}
	
}