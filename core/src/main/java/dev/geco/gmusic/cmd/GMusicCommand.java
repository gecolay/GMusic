package dev.geco.gmusic.cmd;

import org.bukkit.command.*;
import org.bukkit.entity.Player;
import dev.geco.gmusic.main.GMusicMain;
import dev.geco.gmusic.objects.*;
import dev.geco.gmusic.objects.MusicGUI.MenuType;

public class GMusicCommand implements CommandExecutor {
	
	private final GMusicMain GPM;
	
    public GMusicCommand(GMusicMain GPluginMain) { GPM = GPluginMain; }
	
	@Override
	public boolean onCommand(CommandSender s, Command c, String l, String[] a) {
		if(s instanceof Player) {
			Player p = (Player) s;
			if(a.length == 0) {
				if(p.hasPermission(GPM.NAME + ".Music.GUI") || p.hasPermission(GPM.NAME + ".Music.*") || p.hasPermission(GPM.NAME + ".*")) {
					p.openInventory(GPM.getMusicManager().getMusicGUI(p.getUniqueId(), MenuType.DEFAULT).getInventory());
				} else GPM.getMManager().sendMessage(p, "Messages.command-permission-error");
			} else {
				switch(a[0].toLowerCase()) {
				case "play":
					if(p.hasPermission(GPM.NAME + ".Music.Play") || p.hasPermission(GPM.NAME + ".Music.*") || p.hasPermission(GPM.NAME + ".*")) {
						if(a.length > 1) {
							Song S = GPM.getSongManager().getSongById(a[1]);
							if(S != null) {
								PlaySettings ps = GPM.getValues().getPlaySettings().get(p.getUniqueId());
								if(ps.getPlayList() != 2) GPM.getSongManager().playSong(p, S);
								else GPM.getMManager().sendMessage(p, "Messages.command-gmusic-error");
							} else GPM.getMManager().sendMessage(p, "Messages.command-gmusic-play-id-error", "%ID%", a[1]);
						} else GPM.getMManager().sendMessage(p, "Messages.command-gmusic-play-use-error");
					} else GPM.getMManager().sendMessage(p, "Messages.command-permission-error");
					break;
				case "playing":
					if(p.hasPermission(GPM.NAME + ".Music.Playing") || p.hasPermission(GPM.NAME + ".Music.*") || p.hasPermission(GPM.NAME + ".*")) {
						SongSettings t = GPM.getValues().getSongSettings().get(p.getUniqueId());
						if(t != null && !t.isPaused()) {
							Song S = t.getSong();
							GPM.getMManager().sendMessage(p, "Messages.command-gmusic-playing", "%Title%", S.getTitle(), "%Author%", S.getAuthor().equals("") ? GPM.getMManager().getMessage("MusicGUI.disc-empty-author") : S.getAuthor(), "%OAuthor%", S.getOriginalAuthor().equals("") ? GPM.getMManager().getMessage("MusicGUI.disc-empty-oauthor") : S.getOriginalAuthor());
						} else GPM.getMManager().sendMessage(p, "Messages.command-gmusic-playing-none");
					} else GPM.getMManager().sendMessage(p, "Messages.command-permission-error");
					break;
				case "random":
					if(p.hasPermission(GPM.NAME + ".Music.Random") || p.hasPermission(GPM.NAME + ".Music.*") || p.hasPermission(GPM.NAME + ".*")) {
						PlaySettings ps = GPM.getValues().getPlaySettings().get(p.getUniqueId());
						if(ps.getPlayList() != 2) GPM.getSongManager().playSong(p, GPM.getSongManager().getRandomSong(p.getUniqueId()));
						else GPM.getMManager().sendMessage(p, "Messages.command-gmusic-error");
					} else GPM.getMManager().sendMessage(p, "Messages.command-permission-error");
					break;
				case "stop":
					if(p.hasPermission(GPM.NAME + ".Music.Stop") || p.hasPermission(GPM.NAME + ".Music.*") || p.hasPermission(GPM.NAME + ".*")) {
						PlaySettings ps = GPM.getValues().getPlaySettings().get(p.getUniqueId());
						if(ps.getPlayList() != 2) GPM.getSongManager().stopSong(p);
						else GPM.getMManager().sendMessage(p, "Messages.command-gmusic-error");
					} else GPM.getMManager().sendMessage(p, "Messages.command-permission-error");
					break;
				case "pause":
					if(p.hasPermission(GPM.NAME + ".Music.Pause") || p.hasPermission(GPM.NAME + ".Music.*") || p.hasPermission(GPM.NAME + ".*")) {
						GPM.getSongManager().pauseSong(p);
					} else GPM.getMManager().sendMessage(p, "Messages.command-permission-error");
					break;
				case "resume":
					if(p.hasPermission(GPM.NAME + ".Music.Resume") || p.hasPermission(GPM.NAME + ".Music.*") || p.hasPermission(GPM.NAME + ".*")) {
						GPM.getSongManager().resumeSong(p);
					} else GPM.getMManager().sendMessage(p, "Messages.command-permission-error");
					break;
				case "skip":
					if(p.hasPermission(GPM.NAME + ".Music.Skip") || p.hasPermission(GPM.NAME + ".Music.*") || p.hasPermission(GPM.NAME + ".*")) {
						PlaySettings ps = GPM.getValues().getPlaySettings().get(p.getUniqueId());
						if(ps.getPlayList() != 2) GPM.getSongManager().skipSong(p);
						else GPM.getMManager().sendMessage(p, "Messages.command-gmusic-error");
					} else GPM.getMManager().sendMessage(p, "Messages.command-permission-error");
					break;
				case "toggle":
					if(p.hasPermission(GPM.NAME + ".Music.Toggle") || p.hasPermission(GPM.NAME + ".Music.*") || p.hasPermission(GPM.NAME + ".*")) {
						PlaySettings z = GPM.getValues().getPlaySettings().get(p.getUniqueId());
						z.setToggleMode(!z.isToggleMode());
						GPM.getMManager().sendMessage(p, z.isToggleMode() ? "Messages.command-gmusic-toggle-off" : "Messages.command-gmusic-toggle-on");
					} else GPM.getMManager().sendMessage(p, "Messages.command-permission-error");
					break;
				default:
					p.performCommand(c.getName());
					break;
				}
			}
		} else GPM.getMManager().sendMessage(s, "Messages.command-consol-error");
		return true;
	}
	
}