package dev.geco.gmusic.cmd;

import dev.geco.gmusic.GMusicMain;
import dev.geco.gmusic.model.PlaySettings;
import dev.geco.gmusic.model.PlayType;
import dev.geco.gmusic.model.Song;
import dev.geco.gmusic.model.gui.MusicGUI;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class GMusicCommand implements CommandExecutor {

    private final GMusicMain gMusicMain;

    public GMusicCommand(GMusicMain gMusicMain) {
        this.gMusicMain = gMusicMain;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        if(!(sender instanceof Player player)) {
            gMusicMain.getMessageService().sendMessage(sender, "Messages.command-sender-error");
            return true;
        }

        if(!gMusicMain.getPermissionService().hasPermission(sender, "Music", "Music.*")) {
            gMusicMain.getMessageService().sendMessage(sender, "Messages.command-permission-error");
            return true;
        }

        if(!gMusicMain.getEnvironmentUtil().isEntityInAllowedWorld(player)) {
            gMusicMain.getMessageService().sendMessage(sender, "Messages.command-gmusic-world-error");
            return true;
        }

        if(args.length == 0) {
            MusicGUI musicGUI = new MusicGUI(player.getUniqueId(), PlayType.DEFAULT);
            player.openInventory(musicGUI.getInventory());
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "play" -> {
                if(!gMusicMain.getPermissionService().hasPermission(sender, "Music.Play", "Music.*")) {
                    gMusicMain.getMessageService().sendMessage(sender, "Messages.command-permission-error");
                    return true;
                }
                if(args.length == 1) {
                    gMusicMain.getMessageService().sendMessage(sender, "Messages.command-gmusic-play-use-error");
                    return true;
                }
                Song song = gMusicMain.getSongService().getSongById(args[1]);
                if(song == null) {
                    gMusicMain.getMessageService().sendMessage(sender, "Messages.command-gmusic-song-error", "%Song%", args[1]);
                    return true;
                }
                gMusicMain.getPlayService().playSong(player, song);
                gMusicMain.getMessageService().sendMessage(sender, "Messages.command-gmusic-play", "%Song%", song.getId(), "%SongTitle%", song.getTitle());
            }
            case "playing" -> {
                if(!gMusicMain.getPermissionService().hasPermission(sender, "Music.Playing", "Music.*")) {
                    gMusicMain.getMessageService().sendMessage(sender, "Messages.command-permission-error");
                    return true;
                }
                if(!gMusicMain.getPlayService().hasPlayingSong(player.getUniqueId()) || gMusicMain.getPlayService().hasPausedSong(player.getUniqueId())) {
                    gMusicMain.getMessageService().sendMessage(sender, "Messages.command-gmusic-playing-error");
                    return true;
                }
                Song song = gMusicMain.getPlayService().getPlayingSong(player.getUniqueId());
                gMusicMain.getMessageService().sendMessage(sender, "Messages.command-gmusic-playing", "%Song%", song.getId(), "%SongTitle%", song.getTitle());
            }
            case "random" -> {
                if(!gMusicMain.getPermissionService().hasPermission(sender, "Music.Random", "Music.*")) {
                    gMusicMain.getMessageService().sendMessage(sender, "Messages.command-permission-error");
                    return true;
                }
                Song song = gMusicMain.getPlayService().getRandomSong(player.getUniqueId(), PlayType.DEFAULT);
                if(song == null) {
                    gMusicMain.getMessageService().sendMessage(sender, "Messages.command-gmusic-no-song-error");
                    return true;
                }
                gMusicMain.getPlayService().playSong(player, song);
                gMusicMain.getMessageService().sendMessage(sender, "Messages.command-gmusic-play", "%Song%", song.getId(), "%SongTitle%", song.getTitle());
            }
            case "stop" -> {
                if(!gMusicMain.getPermissionService().hasPermission(sender, "Music.Stop", "Music.*")) {
                    gMusicMain.getMessageService().sendMessage(sender, "Messages.command-permission-error");
                    return true;
                }
                if(!gMusicMain.getPlayService().hasPlayingSong(player.getUniqueId())) {
                    gMusicMain.getMessageService().sendMessage(sender, "Messages.command-gmusic-playing-error");
                    return true;
                }
                gMusicMain.getPlayService().stopSong(player);
                gMusicMain.getMessageService().sendMessage(sender, "Messages.command-gmusic-stop");
            }
            case "pause" -> {
                if(!gMusicMain.getPermissionService().hasPermission(sender, "Music.Pause", "Music.*")) {
                    gMusicMain.getMessageService().sendMessage(sender, "Messages.command-permission-error");
                    return true;
                }
                if(!gMusicMain.getPlayService().hasPlayingSong(player.getUniqueId()) || gMusicMain.getPlayService().hasPausedSong(player.getUniqueId())) {
                    gMusicMain.getMessageService().sendMessage(sender, "Messages.command-gmusic-playing-error");
                    return true;
                }
                gMusicMain.getPlayService().pauseSong(player);
                gMusicMain.getMessageService().sendMessage(sender, "Messages.command-gmusic-pause");
            }
            case "resume" -> {
                if(!gMusicMain.getPermissionService().hasPermission(sender, "Music.Resume", "Music.*")) {
                    gMusicMain.getMessageService().sendMessage(sender, "Messages.command-permission-error");
                    return true;
                }
                if(!gMusicMain.getPlayService().hasPausedSong(player.getUniqueId())) {
                    gMusicMain.getMessageService().sendMessage(sender, "Messages.command-gmusic-paused-error");
                    return true;
                }
                gMusicMain.getPlayService().resumeSong(player);
                gMusicMain.getMessageService().sendMessage(sender, "Messages.command-gmusic-resume");
            }
            case "skip" -> {
                if(!gMusicMain.getPermissionService().hasPermission(sender, "Music.Skip", "Music.*")) {
                    gMusicMain.getMessageService().sendMessage(sender, "Messages.command-permission-error");
                    return true;
                }
                Song song = gMusicMain.getPlayService().getNextSong(player);
                if(song == null) {
                    gMusicMain.getMessageService().sendMessage(sender, "Messages.command-gmusic-no-song-error");
                    return true;
                }
                gMusicMain.getPlayService().playSong(player, song);
                gMusicMain.getMessageService().sendMessage(sender, "Messages.command-gmusic-play", "%Song%", song.getId(), "%SongTitle%", song.getTitle());
            }
            case "toggle" -> {
                if(!gMusicMain.getPermissionService().hasPermission(sender, "Music.Toggle", "Music.*")) {
                    gMusicMain.getMessageService().sendMessage(sender, "Messages.command-permission-error");
                    return true;
                }
                PlaySettings playSettings = gMusicMain.getPlaySettingsService().getPlaySettings(player.getUniqueId(), PlayType.DEFAULT);
                playSettings.setToggleMode(!playSettings.isToggleMode());
                if(playSettings.isToggleMode()) {
                    gMusicMain.getMessageService().sendMessage(sender, "Messages.command-gmusic-toggle-disabled");
                } else {
                    gMusicMain.getMessageService().sendMessage(sender, "Messages.command-gmusic-toggle-enabled");
                }
            }
            default -> gMusicMain.getMessageService().sendMessage(sender, "Messages.command-gmusic-use-error");
        }

        return true;
    }

}