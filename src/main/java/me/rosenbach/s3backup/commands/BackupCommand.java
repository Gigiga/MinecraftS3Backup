package me.rosenbach.s3backup.commands;

import me.rosenbach.s3backup.tasks.BackupTask;
import me.rosenbach.s3backup.enums.Permission;
import me.rosenbach.s3backup.S3ServerBackup;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public class BackupCommand implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        if (!sender.hasPermission(Permission.BACKUP.value())) {
            S3ServerBackup.getInstance().sendMessage(sender, "No permission to run command " + label);
        }

        if (args.length == 0) {
            return false;
        }

        switch (args[0]) {
            case "now":
                Bukkit.getScheduler().runTaskAsynchronously(S3ServerBackup.getInstance(),
                        new BackupTask(S3ServerBackup.getInstance(), sender, true));
                break;
            case "pause":
                S3ServerBackup.getInstance().setPaused(true);
                S3ServerBackup.getInstance().sendMessage(sender, "Backups paused. Run " + ChatColor.DARK_PURPLE + " /backup resume"
                        + ChatColor.GRAY + " to resume them");
                break;
            case "resume":
                S3ServerBackup.getInstance().setPaused(false);
                S3ServerBackup.getInstance().sendMessage(sender, "Backups resumed");
                break;
        }

        return false;
    }
}
