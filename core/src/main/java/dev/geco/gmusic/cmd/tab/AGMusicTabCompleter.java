package dev.geco.gmusic.cmd.tab;

import java.util.*;

import org.bukkit.Bukkit;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

import dev.geco.gmusic.main.GMusicMain;
import dev.geco.gmusic.objects.Song;
import dev.geco.gmusic.values.Values;

public class AGMusicTabCompleter implements TabCompleter {
	
	private GMusicMain GPM;
	
    public AGMusicTabCompleter(GMusicMain GPluginMain) { GPM = GPluginMain; }
	
	@Override
	public List<String> onTabComplete(CommandSender s, Command c, String l, String[] a) {
		List<String> Tab = new ArrayList<>();
		List<String> Tab1 = new ArrayList<>();
		try {
			if(s instanceof Player) {
				Player p = (Player) s;
				if(a.length == 1) {
					if(p.hasPermission(GPM.NAME + ".AMusic.Download") || p.hasPermission(GPM.NAME + ".AMusic.*") || p.hasPermission(GPM.NAME + ".*")) Tab.add("download");
					if(p.hasPermission(GPM.NAME + ".AMusic.JukeBox") || p.hasPermission(GPM.NAME + ".AMusic.*") || p.hasPermission(GPM.NAME + ".*")) Tab.add("jukebox");
					if(p.hasPermission(GPM.NAME + ".AMusic.Disc") || p.hasPermission(GPM.NAME + ".AMusic.*") || p.hasPermission(GPM.NAME + ".*")) Tab.add("disc");
					if(p.hasPermission(GPM.NAME + ".AMusic.Play") || p.hasPermission(GPM.NAME + ".AMusic.*") || p.hasPermission(GPM.NAME + ".*")) Tab.add("play");
					if(p.hasPermission(GPM.NAME + ".AMusic.Stop") || p.hasPermission(GPM.NAME + ".AMusic.*") || p.hasPermission(GPM.NAME + ".*")) Tab.add("stop");
					if(p.hasPermission(GPM.NAME + ".AMusic.RadioPlay") || p.hasPermission(GPM.NAME + ".AMusic.*") || p.hasPermission(GPM.NAME + ".*")) Tab.add("radioplay");
					if(p.hasPermission(GPM.NAME + ".AMusic.Edit") || p.hasPermission(GPM.NAME + ".AMusic.*") || p.hasPermission(GPM.NAME + ".*")) Tab.add("edit");
					if(!a[0].isEmpty()) {
						for(String rt : Tab) if(rt.toLowerCase().startsWith(a[0].toLowerCase())) Tab1.add(rt);
						Tab.clear();
					}
				} else if(a.length == 2) {
					if(a[0].equalsIgnoreCase("download") && (p.hasPermission(GPM.NAME + ".AMusic.Download") || p.hasPermission(GPM.NAME + ".AMusic.*") || p.hasPermission(GPM.NAME + ".*"))) {
						Tab.add(Values.NBS_EXT);
						Tab.add(Values.GNBS_EXT);
						Tab.add(Values.MIDI_EXT);
					}
					if(a[0].equalsIgnoreCase("jukebox") && (p.hasPermission(GPM.NAME + ".AMusic.JukeBox") || p.hasPermission(GPM.NAME + ".AMusic.*") || p.hasPermission(GPM.NAME + ".*"))) for(Player t : Bukkit.getOnlinePlayers()) Tab.add(t.getName());
					if(a[0].equalsIgnoreCase("disc") && (p.hasPermission(GPM.NAME + ".AMusic.Disc") || p.hasPermission(GPM.NAME + ".AMusic.*") || p.hasPermission(GPM.NAME + ".*"))) for(Player t : Bukkit.getOnlinePlayers()) Tab.add(t.getName());
					if(a[0].equalsIgnoreCase("play") && (p.hasPermission(GPM.NAME + ".AMusic.Play") || p.hasPermission(GPM.NAME + ".AMusic.*") || p.hasPermission(GPM.NAME + ".*"))) {
						for(Player t : Bukkit.getOnlinePlayers()) Tab.add(t.getName());
						Tab.add(Values.PLAYERS_ALL);
					}
					if(a[0].equalsIgnoreCase("stop") && (p.hasPermission(GPM.NAME + ".AMusic.Stop") || p.hasPermission(GPM.NAME + ".AMusic.*") || p.hasPermission(GPM.NAME + ".*"))) {
						for(Player t : Bukkit.getOnlinePlayers()) Tab.add(t.getName());
						Tab.add(Values.PLAYERS_ALL);
					}
					if(a[0].equalsIgnoreCase("radioplay") && (p.hasPermission(GPM.NAME + ".AMusic.RadioPlay") || p.hasPermission(GPM.NAME + ".AMusic.*") || p.hasPermission(GPM.NAME + ".*"))) for(Song so : GPM.getValues().getSongs()) Tab.add(so.getId());
					if(a[0].equalsIgnoreCase("edit") && (p.hasPermission(GPM.NAME + ".AMusic.Edit") || p.hasPermission(GPM.NAME + ".AMusic.*") || p.hasPermission(GPM.NAME + ".*"))) for(Song so : GPM.getValues().getSongs()) Tab.add(so.getId());
					if(!a[1].isEmpty()) {
						for(String rt : Tab) if(rt.toLowerCase().startsWith(a[1].toLowerCase())) Tab1.add(rt);
						Tab.clear();
					}
				} else if(a.length == 3) {
					if(a[0].equalsIgnoreCase("disc") && (p.hasPermission(GPM.NAME + ".AMusic.Disc") || p.hasPermission(GPM.NAME + ".AMusic.*") || p.hasPermission(GPM.NAME + ".*"))) for(Song so : GPM.getValues().getSongs()) Tab.add(so.getId());
					if(a[0].equalsIgnoreCase("play") && (p.hasPermission(GPM.NAME + ".AMusic.Play") || p.hasPermission(GPM.NAME + ".AMusic.*") || p.hasPermission(GPM.NAME + ".*"))) for(Song so : GPM.getValues().getSongs()) Tab.add(so.getId());
					if(a[0].equalsIgnoreCase("edit") && (p.hasPermission(GPM.NAME + ".AMusic.Edit") || p.hasPermission(GPM.NAME + ".AMusic.*") || p.hasPermission(GPM.NAME + ".*"))) {
						Tab.add("title");
						Tab.add("author");
						Tab.add("oauthor");
						Tab.add("description");
					}
					if(!a[2].isEmpty()) {
						for(String rt : Tab) if(rt.toLowerCase().startsWith(a[2].toLowerCase())) Tab1.add(rt);
						Tab.clear();
					}
				}
			}
			return Tab.size() == 0 ? GPM.getUtilFormat().sortList(Tab1) : GPM.getUtilFormat().sortList(Tab);
		} catch (Exception e) { }
		return null;
	}
	
}