package me.rosenbach.s3backup.tasks;

import me.rosenbach.s3backup.S3ServerBackup;
import me.rosenbach.s3backup.enums.Configuration;
import org.bukkit.command.CommandSender;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class UploadTask implements Runnable{

    private final S3ServerBackup plugin;
    private final CommandSender sender;
    private final File backup;

    public UploadTask(S3ServerBackup plugin, CommandSender sender, File backup) {
        this.plugin = plugin;
        this.sender = sender;
        this.backup = backup;
    }

    @Override
    public void run() {

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd/HHmmss");
        String backupName = LocalDateTime.now().format(formatter)
                .concat("_")
                .concat(plugin.getConfig().getString(Configuration.POSTFIX.getKey()));

        try {
            plugin.sendMessage(sender, "Upload starting");
            plugin.getS3().putObject(plugin.getConfig().getString(Configuration.BUCKET.getKey()), backupName, backup);
            plugin.sendMessage(sender, "Upload done");
        } finally {
            backup.delete();
            plugin.sendMessage(sender, "Local backup file deleted");
        }

        plugin.sendBroadcast("Backup finished");
    }
}
