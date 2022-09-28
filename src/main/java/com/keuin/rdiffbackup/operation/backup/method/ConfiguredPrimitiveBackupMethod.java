package com.keuin.rdiffbackup.operation.backup.method;

import com.keuin.rdiffbackup.backup.BackupFilesystemUtil;
import com.keuin.rdiffbackup.backup.BackupNameTimeFormatter;
import com.keuin.rdiffbackup.backup.name.PrimitiveBackupFilenameEncoder;
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

    private final String filename;
    private final String levelPath;
    private final String backupPath;

    private final Logger LOGGER = Logger.getLogger(ConfiguredPrimitiveBackupMethod.class.getName());

    public ConfiguredPrimitiveBackupMethod(String filename, String levelPath, String backupPath) {
        this.filename = filename;
        this.levelPath = levelPath;
        this.backupPath = backupPath;
    }

    @Deprecated
    private String getBackupFilename(LocalDateTime time, String backupName) {
        String timeString = BackupNameTimeFormatter.localDateTimeToString(time);
        return String.format("%s%s_%s%s", BackupFilesystemUtil.getBackupFilenamePrefix(), timeString, backupName, ".zip");
    }

    @Override
    public PrimitiveBackupFeedback backup() {

        PrimitiveBackupFeedback feedback;

        try {
            String customBackupName = PrimitiveBackupFilenameEncoder.INSTANCE.decode(filename).customName;
            BackupMetadata backupMetadata = new BackupMetadata(System.currentTimeMillis(), customBackupName);
            PrintUtil.info(String.format("zip(srcPath=%s, destPath=%s)", levelPath, backupPath));
            PrintUtil.info("Compressing level ...");
            ZipUtil.makeBackupZip(levelPath, backupPath, filename, backupMetadata);
            feedback = PrimitiveBackupFeedback.createSuccessFeedback(
                    FilesystemUtil.getFileSizeBytes(backupPath, filename));
        } catch (ZipUtilException exception) {
            String msg = "Infinite recursive of directory tree detected, backup was aborted.";
            PrintUtil.info(msg);
            feedback = PrimitiveBackupFeedback.createFailFeedback(msg);
        } catch (IOException e) {
            feedback = PrimitiveBackupFeedback.createFailFeedback(e.getMessage());
        }

        if (!feedback.isSuccess()) {
            // do clean-up if failed
            File backupFile = new File(backupPath, filename);
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
        ZipUtil.unzip(Paths.get(backupPath, filename).toString(), levelPath, false);

        return true;
    }

    @Override
    public boolean touch() {
        File backupSaveDirectoryFile = new File(backupPath);
        return backupSaveDirectoryFile.isDirectory() || backupSaveDirectoryFile.mkdir();
    }

    @Override
    public String getBackupFilename() {
        return filename;
    }

}
