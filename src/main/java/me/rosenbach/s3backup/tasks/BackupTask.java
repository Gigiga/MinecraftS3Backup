package me.rosenbach.s3backup.tasks;

import me.rosenbach.s3backup.MinecraftS3Backup;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.tinyzip.TinyZip;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.TimerTask;

public class BackupTask extends TimerTask {

    private final MinecraftS3Backup plugin;
    private final CommandSender sender;

    public BackupTask(MinecraftS3Backup plugin, CommandSender sender) {
        this.plugin = plugin;
        this.sender = sender;
    }

    @Override
    public void run() {
        if (plugin.isRunning()) {
            plugin.sendMessage(sender, "Backup already running. Please try again in a few minutes.");
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
        }
    }

    private void disableWorldSaving() {
        plugin.sendMessage(sender, "Saving. This might take some time");
        Bukkit.getScheduler().callSyncMethod(plugin, () -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "save-off"));
        Bukkit.getScheduler().callSyncMethod(plugin, () -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "save-all"));
    }

    private File createBackupZip() throws IOException {
        plugin.sendMessage(sender, "Creating backup");
        String backupName = LocalDateTime.now().toString();
        String tempPath = System.getProperty("java.io.tmpdir");

        Path path = Paths.get(tempPath.concat("/").concat(backupName).concat(".zip"));
        TinyZip.zip(path, Paths.get(""));

        plugin.sendMessage(sender, "Backup created");

        return path.toFile();
    }
}
