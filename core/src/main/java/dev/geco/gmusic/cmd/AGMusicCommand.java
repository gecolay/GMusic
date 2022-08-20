package dev.geco.gmusic.cmd;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map.Entry;

import org.bukkit.Bukkit;
import org.bukkit.command.*;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import dev.geco.gmusic.main.GMusicMain;
import dev.geco.gmusic.objects.*;
import dev.geco.gmusic.values.Values;

public class AGMusicCommand implements CommandExecutor {
	
	private final GMusicMain GPM;
	
    public AGMusicCommand(GMusicMain GPluginMain) { GPM = GPluginMain; }
	
	@Override
	public boolean onCommand(CommandSender s, Command c, String l, String[] a) {
		if(a.length == 0) GPM.getMManager().sendMessage(s, "Messages.command-agmusic-use-error");
		else {
			switch(a[0].toLowerCase()) {
			case "download":
				if(s instanceof Player) {
					Player p = (Player) s;
					if(!p.hasPermission(GPM.NAME + ".AMusic.Download") && !p.hasPermission(GPM.NAME + ".AMusic.*") && !p.hasPermission(GPM.NAME + ".*")) {
						GPM.getMManager().sendMessage(p, "Messages.command-permission-error");
						return true;
					}
				}
				if(a.length > 3) {
					String t = a[1].toLowerCase();
					if(t.equalsIgnoreCase(Values.NBS_EXT) || t.equalsIgnoreCase(Values.GNBS_EXT) || t.equalsIgnoreCase(Values.MIDI_EXT)) {
						File f = new File("plugins/" + GPM.NAME, t.equalsIgnoreCase(Values.NBS_EXT) ? Values.CONVERT_PATH + "/" + a[2] + Values.NBS_FILETYP : t.equalsIgnoreCase(Values.GNBS_EXT) ? Values.SONGS_PATH + "/" + a[2] + Values.GNBS_FILETYP : Values.MIDI_PATH + "/" + a[2] + Values.MID_FILETYP);
						try {
							if(!f.exists()) {
								GPM.getUtilFormat().downloadFile(a[3], f);
								GPM.getMManager().sendMessage(s, "Messages.command-agmusic-download");
							} else GPM.getMManager().sendMessage(s, "Messages.command-agmusic-download-name-error", "%Name%", a[2]);
						} catch(Exception e) {
							GPM.getMManager().sendMessage(s, "Messages.command-agmusic-download-error", "%Error%", e.getMessage());
							if(f.exists()) f.delete();
						}
					} else GPM.getMManager().sendMessage(s, "Messages.command-agmusic-download-folder-error", "%Folder%", t);
				} else GPM.getMManager().sendMessage(s, "Messages.command-agmusic-download-use-error");
				break;
			case "jukebox":
				if(s instanceof Player) {
					Player p = (Player) s;
					if(!p.hasPermission(GPM.NAME + ".AMusic.JukeBox") && !p.hasPermission(GPM.NAME + ".AMusic.*") && !p.hasPermission(GPM.NAME + ".*")) {
						GPM.getMManager().sendMessage(p, "Messages.command-permission-error");
						return true;
					}
				}
				if(a.length > 1) {
					Player t = Bukkit.getPlayer(a[1]);
					if(t != null) {
						long am = 1;
						if(a.length > 2) {
							try {
								am = Long.parseLong(a[2]);
								if(am <= 0) throw new NumberFormatException();
							} catch(NumberFormatException e) {
								GPM.getMManager().sendMessage(s, "Messages.command-agmusic-jukebox-amount-error", "%Amount%", a[2]);
								return true;
							}
						}
						GPM.getUtilInventory().addPlayerInventoryItem(t.getInventory(), GPM.getMusicManager().getJukeBox(), am);
						GPM.getMManager().sendMessage(s, "Messages.command-agmusic-jukebox", "%Player%", a[1], "%Amount%", "" + am);
					} else GPM.getMManager().sendMessage(s, "Messages.command-agmusic-jukebox-online-error", "%Player%", a[1]);
				} else GPM.getMManager().sendMessage(s, "Messages.command-agmusic-jukebox-use-error");
				break;
			case "disc":
				if(s instanceof Player) {
					Player p = (Player) s;
					if(!p.hasPermission(GPM.NAME + ".AMusic.Disc") && !p.hasPermission(GPM.NAME + ".AMusic.*") && !p.hasPermission(GPM.NAME + ".*")) {
						GPM.getMManager().sendMessage(p, "Messages.command-permission-error");
						return true;
					}
				}
				if(a.length > 2) {
					Player t = Bukkit.getPlayer(a[1]);
					if(t != null) {
						Song S = GPM.getSongManager().getSongById(a[2]);
						if(S != null) {
							int am = 1;
							if(a.length > 3) {
								try {
									am = Integer.parseInt(a[3]);
									if(am <= 0) throw new NumberFormatException();
								} catch(NumberFormatException e) {
									GPM.getMManager().sendMessage(s, "Messages.command-agmusic-disc-amount-error", "%Amount%", a[3]);
									return true;
								}
							}
							for(Entry<ItemStack, Song> m : GPM.getValues().getDiscItems().entrySet()) if(m.getValue().equals(S)) GPM.getUtilInventory().addPlayerInventoryItem(t.getInventory(), m.getKey(), am);
							GPM.getMManager().sendMessage(s, "Messages.command-agmusic-disc", "%Player%", a[1], "%Amount%", "" + am);
						} else GPM.getMManager().sendMessage(s, "Messages.command-agmusic-disc-id-error", "%ID%", a[2]);
					} else GPM.getMManager().sendMessage(s, "Messages.command-agmusic-disc-online-error", "%Player%", a[1]);
				} else GPM.getMManager().sendMessage(s, "Messages.command-agmusic-disc-use-error");
				break;
			case "play":
				if(s instanceof Player) {
					Player p = (Player) s;
					if(!p.hasPermission(GPM.NAME + ".AMusic.Play") && !p.hasPermission(GPM.NAME + ".AMusic.*") && !p.hasPermission(GPM.NAME + ".*")) {
						GPM.getMManager().sendMessage(p, "Messages.command-permission-error");
						return true;
					}
				}
				if(a.length > 2) {
					Player t = Bukkit.getPlayer(a[1]);
					if(t != null) {
						Song S = GPM.getSongManager().getSongById(a[2]);
						if(S != null) {
							GPM.getSongManager().playSong(t, S);
						} else GPM.getMManager().sendMessage(s, "Messages.command-agmusic-play-id-error", "%ID%", a[2]);
					} else if(a[1].equalsIgnoreCase(Values.PLAYERS_ALL)) {
						Song S = GPM.getSongManager().getSongById(a[2]);
						if(S != null) {
							for(Player i : Bukkit.getOnlinePlayers()) GPM.getSongManager().playSong(i, S);
						} else GPM.getMManager().sendMessage(s, "Messages.command-agmusic-play-id-error", "%ID%", a[2]);
					} else GPM.getMManager().sendMessage(s, "Messages.command-agmusic-play-online-error", "%Player%", a[1]);
				} else GPM.getMManager().sendMessage(s, "Messages.command-agmusic-play-use-error");
				break;
			case "stop":
				if(s instanceof Player) {
					Player p = (Player) s;
					if(!p.hasPermission(GPM.NAME + ".AMusic.Stop") && !p.hasPermission(GPM.NAME + ".AMusic.*") && !p.hasPermission(GPM.NAME + ".*")) {
						GPM.getMManager().sendMessage(p, "Messages.command-permission-error");
						return true;
					}
				}
				if(a.length > 1) {
					Player t = Bukkit.getPlayer(a[1]);
					if(t != null) {
						GPM.getSongManager().stopSong(t);
					} else if(a[1].equalsIgnoreCase(Values.PLAYERS_ALL)) {
						for(Player i : Bukkit.getOnlinePlayers()) GPM.getSongManager().stopSong(i);
					} else GPM.getMManager().sendMessage(s, "Messages.command-agmusic-stop-online-error", "%Player%", a[1]);
				} else GPM.getMManager().sendMessage(s, "Messages.command-agmusic-stop-use-error");
				break;
			case "radioplay":
				if(s instanceof Player) {
					Player p = (Player) s;
					if(!p.hasPermission(GPM.NAME + ".AMusic.RadioPlay") && !p.hasPermission(GPM.NAME + ".AMusic.*") && !p.hasPermission(GPM.NAME + ".*")) {
						GPM.getMManager().sendMessage(p, "Messages.command-permission-error");
						return true;
					}
				}
				if(a.length > 1) {
					Song S = GPM.getSongManager().getSongById(a[1]);
					if(S != null) {
						GPM.getRadioManager().playRadioSong(S, 0);
					} else GPM.getMManager().sendMessage(s, "Messages.command-agmusic-radioplay-id-error", "%ID%", a[1]);
				} else GPM.getMManager().sendMessage(s, "Messages.command-agmusic-radioplay-use-error");
				break;
			case "edit":
				if(s instanceof Player) {
					Player p = (Player) s;
					if(!p.hasPermission(GPM.NAME + ".AMusic.Edit") && !p.hasPermission(GPM.NAME + ".AMusic.*") && !p.hasPermission(GPM.NAME + ".*")) {
						GPM.getMManager().sendMessage(p, "Messages.command-permission-error");
						return true;
					}
				}
				if(a.length > 2) {
					Song S = GPM.getSongManager().getSongById(a[1]);
					if(S != null) {
						switch(a[2].toLowerCase()) {
						case "title":
							if(a.length > 3) {
								File f = new File("plugins/" + GPM.NAME + "/" + Values.SONGS_PATH + "/" + S.getFileName());
								YamlConfiguration fc = YamlConfiguration.loadConfiguration(f);
								String cm = "";
								for (int m = 3; m <= a.length - 1; m++) cm += a[m] + " ";
								cm = cm.substring(0, cm.length() - 1);
								fc.set("Song.Title", cm);
								try { fc.save(f); } catch (IOException e) { }
								GPM.getMManager().sendMessage(s, "Messages.command-agmusic-edit-title", "%ID%", S.getId(), "%Title%", cm);
							} else GPM.getMManager().sendMessage(s, "Messages.command-agmusic-edit-get-title", "%ID%", S.getId(), "%Title%", S.getTitle());
							break;
						case "author":
							if(a.length > 3) {
								File f = new File("plugins/" + GPM.NAME + "/" + Values.SONGS_PATH + "/" + S.getFileName());
								YamlConfiguration fc = YamlConfiguration.loadConfiguration(f);
								String cm = "";
								for (int m = 3; m <= a.length - 1; m++) cm += a[m] + " ";
								cm = cm.substring(0, cm.length() - 1);
								fc.set("Song.Author", cm);
								try { fc.save(f); } catch (IOException e) { }
								GPM.getMManager().sendMessage(s, "Messages.command-agmusic-edit-author", "%ID%", S.getId(), "%Author%", cm);
							} else GPM.getMManager().sendMessage(s, "Messages.command-agmusic-edit-get-author", "%ID%", S.getId(), "%Author%", S.getAuthor());
							break;
						case "oauthor":
							if(a.length > 3) {
								File f = new File("plugins/" + GPM.NAME + "/" + Values.SONGS_PATH + "/" + S.getFileName());
								YamlConfiguration fc = YamlConfiguration.loadConfiguration(f);
								String cm = "";
								for (int m = 3; m <= a.length - 1; m++) cm += a[m] + " ";
								cm = cm.substring(0, cm.length() - 1);
								fc.set("Song.OAuthor", cm);
								try { fc.save(f); } catch (IOException e) { }
								GPM.getMManager().sendMessage(s, "Messages.command-agmusic-edit-oauthor", "%ID%", S.getId(), "%OAuthor%", cm);
							} else GPM.getMManager().sendMessage(s, "Messages.command-agmusic-edit-get-oauthor", "%ID%", S.getId(), "%OAuthor%", S.getOriginalAuthor());
							break;
						case "description":
							if(a.length > 3) {
								File f = new File("plugins/" + GPM.NAME + "/" + Values.SONGS_PATH + "/" + S.getFileName());
								YamlConfiguration fc = YamlConfiguration.loadConfiguration(f);
								String cm = "";
								for (int m = 3; m <= a.length - 1; m++) cm += a[m] + " ";
								cm = cm.substring(0, cm.length() - 1);
								fc.set("Song.Description", cm.replace(" ", "").equals("") ? new ArrayList<>() : Arrays.asList(cm.toString().split("\\\\n")));
								try { fc.save(f); } catch (IOException e) { }
								GPM.getMManager().sendMessage(s, "Messages.command-agmusic-edit-description", "%ID%", S.getId(), "%Description%", cm);
							} else GPM.getMManager().sendMessage(s, "Messages.command-agmusic-edit-get-description", "%ID%", S.getId(), "%Description%", S.getDescription().toString());
							break;
						default:
							GPM.getMManager().sendMessage(s, "Messages.command-agmusic-edit-use-error");
							break;
						}
					} else GPM.getMManager().sendMessage(s, "Messages.command-agmusic-edit-id-error", "%ID%", a[1]);
				} else GPM.getMManager().sendMessage(s, "Messages.command-agmusic-edit-use-error");
				break;
			default:
				GPM.getMManager().sendMessage(s, "Messages.command-agmusic-use-error");
				break;
			}
		}
		return true;
	}
	
}