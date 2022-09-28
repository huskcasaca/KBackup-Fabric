package com.keuin.rdiffbackup.operation.backup.method;

import com.keuin.rdiffbackup.backup.BackupFilesystemUtil;
import com.keuin.rdiffbackup.backup.BackupNameTimeFormatter;
import com.keuin.rdiffbackup.backup.name.PrimitiveBackupFileNameEncoder;
import com.keuin.rdiffbackup.exception.ZipUtilException;
import com.keuin.rdiffbackup.metadata.BackupMetadata;
import com.keuin.rdiffbackup.operation.backup.feedback.PrimitiveBackupFeedback;
import com.keuin.rdiffbackup.util.FilesystemUtil;
import com.keuin.rdiffbackup.util.PrintUtil;
import com.keuin.rdiffbackup.util.ZipUtil;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.logging.Logger;

public class ConfiguredPrimitiveBackupMethod implements ConfiguredBackupMethod {

    private final String backupFileName;
    private final String levelPath;
    private final String backupSavePath;

    private final Logger LOGGER = Logger.getLogger(ConfiguredPrimitiveBackupMethod.class.getName());

    public ConfiguredPrimitiveBackupMethod(String backupFileName, String levelPath, String backupSavePath) {
        this.backupFileName = backupFileName;
        this.levelPath = levelPath;
        this.backupSavePath = backupSavePath;
    }

    @Deprecated
    private String getBackupFileName(LocalDateTime time, String backupName) {
        String timeString = BackupNameTimeFormatter.localDateTimeToString(time);
        return String.format("%s%s_%s%s", BackupFilesystemUtil.getBackupFileNamePrefix(), timeString, backupName, ".zip");
    }

    @Override
    public PrimitiveBackupFeedback backup() {

        PrimitiveBackupFeedback feedback;

        try {
            String customBackupName = PrimitiveBackupFileNameEncoder.INSTANCE.decode(backupFileName).customName;
            BackupMetadata backupMetadata = new BackupMetadata(System.currentTimeMillis(), customBackupName);
            PrintUtil.info(String.format("zip(srcPath=%s, destPath=%s)", levelPath, backupSavePath));
            PrintUtil.info("Compressing level ...");
            ZipUtil.makeBackupZip(levelPath, backupSavePath, backupFileName, backupMetadata);
            feedback = PrimitiveBackupFeedback.createSuccessFeedback(
                    FilesystemUtil.getFileSizeBytes(backupSavePath, backupFileName));
        } catch (ZipUtilException exception) {
            String msg = "Infinite recursive of directory tree detected, backup was aborted.";
            PrintUtil.info(msg);
            feedback = PrimitiveBackupFeedback.createFailFeedback(msg);
        } catch (IOException e) {
            feedback = PrimitiveBackupFeedback.createFailFeedback(e.getMessage());
        }

        if (!feedback.isSuccess()) {
            // do clean-up if failed
            File backupFile = new File(backupSavePath, backupFileName);
            if (backupFile.exists()) {
                LOGGER.info(String.format("Deleting incomplete backup file \"%s\"...", backupFile.getPath()));
                try {
                    Files.delete(backupFile.toPath());
                    LOGGER.info("Failed to backup, all files are cleaned up.");
                } catch (IOException e) {
                    LOGGER.warning("Cannot delete: " + e.getMessage());
                }
            }
        }
        return feedback;
    }

    @Override
    public boolean restore() throws IOException {
        // Delete old level
        PrintUtil.info("Server stopped. Deleting old level ...");
        if (!FilesystemUtil.forceDeleteDirectory(new File(levelPath))) {
            PrintUtil.info("Failed to delete old level!");
            return false;
        }

        // Decompress archive
        PrintUtil.info("Decompressing archived level ...");
        ZipUtil.unzip(Paths.get(backupSavePath, backupFileName).toString(), levelPath, false);

        return true;
    }

    @Override
    public boolean touch() {
        File backupSaveDirectoryFile = new File(backupSavePath);
        return backupSaveDirectoryFile.isDirectory() || backupSaveDirectoryFile.mkdir();
    }

    @Override
    public String getBackupFileName() {
        return backupFileName;
    }

}
