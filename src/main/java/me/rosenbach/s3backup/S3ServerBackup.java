package me.rosenbach.s3backup;

import lombok.Getter;
import lombok.Setter;
import me.rosenbach.s3backup.aws.AwsS3Client;
import me.rosenbach.s3backup.commands.BackupCommand;
import me.rosenbach.s3backup.enums.Configuration;
import me.rosenbach.s3backup.tasks.BackupTask;
import net.kyori.adventure.audience.MessageType;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public final class S3ServerBackup extends JavaPlugin {

    private static S3ServerBackup INSTANCE;

    @Getter
    @Setter
    private boolean running;

    @Getter
    @Setter
    private boolean paused;

    @Getter
    @Setter
    private long lastBackup;

    @Getter
    private AwsS3Client s3;

    @Getter
    private final ScheduledExecutorService scheduler;

    public S3ServerBackup() {
        INSTANCE = this;
        scheduler = Executors.newScheduledThreadPool(1);
    }

    @Override
    public void onEnable() {
        this.saveDefaultConfig();

        if(!minimalConfigValid()) {
            sendMessage(Bukkit.getConsoleSender(), "Your config is invalid. Please check!");
            return;
        }

        String region = this.getConfig().getString(Configuration.REGION.getKey());
        double uploadSpeed = this.getConfig().getDouble(Configuration.UPLOAD_SPEED.getKey());

        if(useCredentialsFile()) {
            s3 = new AwsS3Client(uploadSpeed);
        } else {
            s3 = new AwsS3Client(
                    region,
                    this.getConfig().getString(Configuration.ACCESS_KEY_ID.getKey()),
                    this.getConfig().getString(Configuration.ACCESS_KEY_SECRET.getKey()),
                    uploadSpeed);
        }

        registerCommands();

        if (intervalConfigured()) {
            for (String time : this.getConfig().getStringList(Configuration.BACKUP_TIMES.getKey())) {
                LocalDateTime runAt = LocalDateTime.of(LocalDate.now(),LocalTime.parse(time));

                long secondsUntilFirstRun;
                if (runAt.isBefore(LocalDateTime.now())) {
                    secondsUntilFirstRun = LocalDateTime.now().until(
                            runAt.plus(1, ChronoUnit.DAYS), ChronoUnit.SECONDS);
                } else {
                    secondsUntilFirstRun = LocalDateTime.now().until(runAt, ChronoUnit.SECONDS);
                }

                BackupTask backupTask = new BackupTask(this, Bukkit.getConsoleSender(), false);
                scheduler.scheduleAtFixedRate(
                        backupTask, secondsUntilFirstRun, TimeUnit.DAYS.toSeconds(1), TimeUnit.SECONDS);

            }
            sendMessage(Bukkit.getConsoleSender(),
                    "Started, running backups at " +
                            this.getConfig().getStringList(Configuration.BACKUP_TIMES.getKey()));
        } else {
            sendMessage(Bukkit.getConsoleSender(), "Started, no automatic backup configured");
        }
    }

    @Override
    public void onDisable() {
        scheduler.shutdown();
        Bukkit.getScheduler().cancelTasks(this);
        sendMessage(Bukkit.getConsoleSender(), "Stopped");
    }

    public static S3ServerBackup getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new S3ServerBackup();
        }

        return INSTANCE;
    }

    private boolean useCredentialsFile() {
        return Objects.requireNonNull(
                this.getConfig().getString(Configuration.ACCESS_KEY_ID.getKey())).isEmpty() &&
                Objects.requireNonNull(
                        this.getConfig().getString(Configuration.ACCESS_KEY_SECRET.getKey())).isEmpty() &&
                Objects.requireNonNull(
                this.getConfig().getString(Configuration.REGION.getKey())).isEmpty();
    }

    private boolean minimalConfigValid() {
        boolean bucketFilled = !Objects.requireNonNull(
                this.getConfig().getString(Configuration.BUCKET.getKey())).isEmpty();
        boolean uploadSpeedFilled = !Objects.requireNonNull(
                this.getConfig().getString(Configuration.UPLOAD_SPEED.getKey())).isEmpty();

        return bucketFilled && uploadSpeedFilled;
    }

    private boolean intervalConfigured() {
        return !Objects.requireNonNull(
                this.getConfig().getStringList(Configuration.BACKUP_TIMES.getKey())).isEmpty();
    }

    private void registerCommands() {
        Objects.requireNonNull(Bukkit.getPluginCommand("backup")).setExecutor(new BackupCommand());
    }

    public void sendMessage(CommandSender sender, String text) {
        String prefix = ChatColor.DARK_GREEN + "Minecraft S3 Backup: " + ChatColor.GRAY;
        sender.sendMessage(prefix.concat(" ").concat(text));
    }

    public void sendBroadcast(String broadcast) {
        this.getServer().sendMessage(Component.text().content(broadcast).color(TextColor.color(11141120)), MessageType.SYSTEM);
    }
}
