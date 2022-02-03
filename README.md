# Minecraft S3 Backup

This plugin aims to provide a simple way to backup your PaperMC Minecraft server into an Amazon S3 Bucket.

## Setup

To use it, you need an AWS-Developer account with an IAM user and a S3-Bucket created. Please refer to the Amazon documentation fot this steps.

### Login options

This plugin supports both login to AWS via credentials file or by providing the creditials in the plugin configuration.

If you want to use the plugin configuration file add your IAM account credentials to the config.yml in the S3ServerBackup plugin folder.

```
access-key-id: <your access-key>
access-key-secret: <your access-key-secret>
bucket: <name of your bucket>
region: <the region your bucket belongs to>
upload-speed: 0.05
backup-times:
  - "12:00"
  - "22:00"

```

The upload-speed config indicates which bandwith the plugin should use to upload the backup. The number is in Gbps. Adjust it according to the upload speed of your connection.

The backup-times configuration defines the time (in 24h format) at which a backup is executed. Leave it emtpy to disable automatic backups.

### Minecraft command

The plugin provides the /backup command to use in Minecraft. You need to be OP to use it.
The following parameters are supported:

* **now** Starts an backup process
* **pause** Pauses the automatic backups until they are resumed or the server is restarted
* **resume** Resumes the automatic backups

## Disclaimer

Please keep in mind, that Amazon AWS is a paid service and there will be costs for using it.
