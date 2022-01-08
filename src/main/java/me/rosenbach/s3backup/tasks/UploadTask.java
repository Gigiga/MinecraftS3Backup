package me.rosenbach.s3backup.tasks;

import me.rosenbach.s3backup.MinecraftS3Backup;
import me.rosenbach.s3backup.enums.Configuration;
import org.bukkit.command.CommandSender;

import java.io.File;

public class UploadTask implements Runnable{

    private final MinecraftS3Backup plugin;
    private final CommandSender sender;
    private final File backup;

    public UploadTask(MinecraftS3Backup plugin, CommandSender sender, File backup) {
        this.plugin = plugin;
        this.sender = sender;
        this.backup = backup;
    }

    @Override
    public void run() {
        plugin.sendMessage(sender, "Upload starting");
        plugin.getS3().putObject(plugin.getConfig().getString(Configuration.BUCKET.getKey()), backup);
        plugin.sendMessage(sender, "Upload done");

        boolean localBackupDeleted = backup.delete();
        if (localBackupDeleted) {
            plugin.sendMessage(sender, "Local backup file deleted");
        }

        plugin.sendBroadcast("Backup finished");
    }
}
