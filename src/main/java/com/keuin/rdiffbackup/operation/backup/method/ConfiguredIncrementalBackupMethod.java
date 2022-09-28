package com.keuin.rdiffbackup.operation.backup.method;

import com.keuin.rdiffbackup.backup.incremental.ObjectCollection2;
import com.keuin.rdiffbackup.backup.incremental.ObjectCollectionFactory;
import com.keuin.rdiffbackup.backup.incremental.ObjectCollectionSerializer;
import com.keuin.rdiffbackup.backup.incremental.identifier.Sha256Identifier;
import com.keuin.rdiffbackup.backup.incremental.manager.IncCopyResult;
import com.keuin.rdiffbackup.backup.incremental.manager.IncrementalBackupStorageManager;
import com.keuin.rdiffbackup.backup.incremental.serializer.IncBackupInfoSerializer;
import com.keuin.rdiffbackup.backup.incremental.serializer.SavedIncrementalBackup;
import com.keuin.rdiffbackup.backup.name.BackupFilenameEncoder;
import com.keuin.rdiffbackup.backup.name.IncrementalBackupFilenameEncoder;
import com.keuin.rdiffbackup.metadata.BackupMetadata;
import com.keuin.rdiffbackup.operation.backup.feedback.IncrementalBackupFeedback;
import com.keuin.rdiffbackup.util.FilesystemUtil;
import com.keuin.rdiffbackup.util.PrintUtil;
import com.keuin.rdiffbackup.util.ThreadingUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.HashSet;
import java.util.logging.Logger;

public class ConfiguredIncrementalBackupMethod implements ConfiguredBackupMethod {

    private final String backupIndexFilename;
    private final String levelPath;
    private final String backupIndexFileSaveDirectory;
    private final String backupBaseDirectory;

    private static final Logger LOGGER = Logger.getLogger(ConfiguredIncrementalBackupMethod.class.getName());

    public ConfiguredIncrementalBackupMethod(String backupIndexFilename, String levelPath, String backupIndexFileSaveDirectory, String backupBaseDirectory) {
        this.backupIndexFilename = backupIndexFilename;
        this.levelPath = levelPath;
        this.backupIndexFileSaveDirectory = backupIndexFileSaveDirectory;
        this.backupBaseDirectory = backupBaseDirectory;
    }

    @Override
    public IncrementalBackupFeedback backup() {
        final int hashFactoryThreads = ThreadingUtil.getRecommendedThreadCount(); // how many threads do we use to generate the hash tree
        LOGGER.info("Threads: " + hashFactoryThreads);

        // needed in abort progress
        File levelPathFile = new File(levelPath);
        IncrementalBackupFeedback feedback;
        IncrementalBackupStorageManager storageManager = null;

        ObjectCollection2 collection = null; // this backup's collection
        try {
            // construct incremental backup index
            PrintUtil.info("Hashing files...");
            collection = new ObjectCollectionFactory<>(Sha256Identifier.getFactory(), hashFactoryThreads, 16)
                    .fromDirectory(levelPathFile, new HashSet<>(Arrays.asList("session.lock", "kbackup_metadata")));

            // update storage
            PrintUtil.info("Copying files...");
            storageManager = new IncrementalBackupStorageManager(Paths.get(backupBaseDirectory));
            IncCopyResult copyResult = storageManager.addObjectCollection(collection, levelPathFile);
            if (copyResult == null) {
                PrintUtil.info("Failed to backup. No further information.");
                return new IncrementalBackupFeedback(false, null);
            }

            // save index file
            PrintUtil.info("Saving index file...");

            // legacy index file
//            ObjectCollectionSerializer.toFile(collection, new File(backupIndexFileSaveDirectory, backupIndexFilename));

            // create directory
            final File indexDirectoryFile = new File(backupIndexFileSaveDirectory);
            if (indexDirectoryFile.isFile())
                throw new IOException("There is a file which has the same name with index directory");
            if (!indexDirectoryFile.isDirectory() && !indexDirectoryFile.mkdirs())
                throw new IOException("Backup index save directory does not exist and we failed to create it");

            // newer saved info (with metadata)
            File indexFile = new File(backupIndexFileSaveDirectory, backupIndexFilename);
            BackupFilenameEncoder.BackupBasicInformation info = IncrementalBackupFilenameEncoder.INSTANCE.decode(backupIndexFilename);
            IncBackupInfoSerializer.toFile(indexFile, SavedIncrementalBackup.newLatest(
                    collection,
                    info.customName,
                    info.time.atZone(ZoneId.systemDefault()),
                    copyResult.getTotalBytes(),
                    copyResult.getCopiedBytes(),
                    copyResult.getCopiedFiles(),
                    copyResult.getTotalFiles()
            ));

            // return result
            PrintUtil.info("Incremental backup finished.");
            feedback = new IncrementalBackupFeedback(true, copyResult);
        } catch (IOException e) {
//            e.printStackTrace(); // stack trace has been passed to backup feedback. No need to print here.
            feedback = new IncrementalBackupFeedback(e);
        }

        // do clean-up if failed
        if (!feedback.isSuccess()) {
            LOGGER.severe("Failed to backup. Cleaning up...");

            // remove index file
            File backupIndexFile = new File(backupIndexFileSaveDirectory, backupIndexFilename);
            if (backupIndexFile.exists()) {
                if (!backupIndexFile.delete()) {
                    LOGGER.warning("Failed to clean up: cannot delete file " + backupIndexFile.getName());
                    return feedback; // not try to remove unused files
                }
            }

            // remove unused object files in the base
            if (collection != null) {
                try {
                    // collection may have been copied (partially) to the base, but we may not need them
                    // so we perform a clean here
                    // perform a clean-up
                    Iterable<ObjectCollection2> backups = ObjectCollectionSerializer.fromDirectory(new File(backupIndexFileSaveDirectory));
                    storageManager.deleteObjectCollection(collection, backups);
                } catch (IOException e) {
                    LOGGER.warning("An exception occurred while cleaning up: " + e);
                }
                LOGGER.info("Backup aborted.");
            }
        }

        return feedback;
    }

    @Override
    public boolean restore() throws IOException {
        // load collection
        PrintUtil.info("Loading file list...");
        SavedIncrementalBackup info = IncBackupInfoSerializer.fromFile(
                new File(backupIndexFileSaveDirectory, backupIndexFilename)
        );

        PrintUtil.info("Backup Info: " + info);

        // delete old level
        File levelPathFile = new File(levelPath);
        PrintUtil.info("Deleting old level...");
        if (!FilesystemUtil.forceDeleteDirectory(levelPathFile)) {
            PrintUtil.info("Failed to delete old level!");
            return false;
        }

        // restore file
        PrintUtil.info("Copying files...");
        IncrementalBackupStorageManager storageManager = new IncrementalBackupStorageManager(Paths.get(backupBaseDirectory));
        int restoreObjectCount = storageManager.restoreObjectCollection(info.getObjectCollection(), levelPathFile);

        // write metadata file
        File metadataFile = new File(levelPathFile, BackupMetadata.METADATA_FILENAME);
        try (FileOutputStream fos = new FileOutputStream(metadataFile)) {
            try (ObjectOutputStream oos = new ObjectOutputStream(fos)) {
                oos.writeObject(new BackupMetadata(info.getBackupTime().toEpochSecond() * 1000, info.getBackupName()));
            }
        } catch (IOException e) {
            PrintUtil.warn("Failed to write restore metadata: " + e + ". Rdiff Backup won't print restoration information during the next startup.");
            try {
                Files.deleteIfExists(metadataFile.toPath());
            } catch (IOException ignored) {
            }
        }

        PrintUtil.info(String.format("%d file(s) restored.", restoreObjectCount));
        return true;
    }

    @Override
    public boolean touch() {
        File baseDirectoryFile = new File(backupBaseDirectory);
        return baseDirectoryFile.isDirectory() || baseDirectoryFile.mkdir();
    }

    @Override
    public String getBackupFilename() {
        return backupIndexFilename;
    }


}
