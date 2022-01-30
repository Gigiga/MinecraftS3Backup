package me.rosenbach.s3backup.tasks;

import me.rosenbach.s3backup.S3ServerBackup;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.tinyzip.TinyZip;
import org.tinyzip.parameters.ZipParameters;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.TimerTask;

public class BackupTask extends TimerTask {

    private final S3ServerBackup plugin;
    private final CommandSender sender;
    private final boolean manuallyTriggered;

    public BackupTask(S3ServerBackup plugin, CommandSender sender, boolean manuallyTriggered) {
        this.plugin = plugin;
        this.sender = sender;
        this.manuallyTriggered = manuallyTriggered;
    }

    @Override
    public void run() {

        if (!manuallyTriggered) {
            if(plugin.isPaused()) {
                plugin.sendMessage(sender, "Backups are paused. Please run " + ChatColor.DARK_PURPLE +
                        "/backup resume" + ChatColor.GRAY + " to resume them");
                return;
            } else if (!wasPlayerOnlineSinceLastUpdate()) {
                plugin.sendMessage(sender, "No player was online since the last backup. Skipping this one");
                return;
            }
        }

        if (plugin.isRunning()) {
            plugin.sendMessage(sender, "Backup already running. Please try again in a few minutes");
            return;
        }

        plugin.sendBroadcast("Starting backup. Performance might be worse for the next couple of minutes");
        plugin.setRunning(true);

        try {
            disableWorldSaving();

            File backup = createBackupZip();
            Bukkit.getScheduler().runTaskAsynchronously(plugin, new UploadTask(plugin, sender, backup));
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            Bukkit.getScheduler().callSyncMethod(plugin, () -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "save-on"));
            plugin.setRunning(false);
            plugin.setLastBackup(System.currentTimeMillis());
        }
    }

    private void disableWorldSaving() {
        plugin.sendMessage(sender, "Saving. This might take some time");
        Bukkit.getScheduler().callSyncMethod(plugin, () -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "save-off"));
        Bukkit.getScheduler().callSyncMethod(plugin, () -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "save-all"));
    }

    private File createBackupZip() throws IOException {
        plugin.sendMessage(sender, "Creating backup");
        String tempPath = System.getProperty("java.io.tmpdir");

        Path path = Paths.get(tempPath.concat("/").concat("backup").concat(".zip"));

        try {
            Files.deleteIfExists(path);
        } catch (IOException exception) {
            plugin.sendMessage(sender, "There was an error deleting the previous file.");
        }

        ZipParameters parameters = new ZipParameters((progress, filename) ->
                plugin.sendMessage(sender, "Progress: " + String.format("%.2f", progress) + "%"));

        TinyZip.zip(path, parameters, Paths.get(""));

        plugin.sendMessage(sender, "Backup created");

        return path.toFile();
    }

    private boolean wasPlayerOnlineSinceLastUpdate() {
        return Arrays.stream(plugin.getServer().getOfflinePlayers())
                .anyMatch(offlinePlayer -> offlinePlayer.getLastSeen() > plugin.getLastBackup());
    }
}
