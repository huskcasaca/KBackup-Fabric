package com.keuin.rdiffbackup.operation;

import com.keuin.rdiffbackup.backup.BackupFilesystemUtil;
import com.keuin.rdiffbackup.backup.incremental.ObjectCollection2;
import com.keuin.rdiffbackup.backup.incremental.ObjectCollectionSerializer;
import com.keuin.rdiffbackup.backup.incremental.manager.IncrementalBackupStorageManager;
import com.keuin.rdiffbackup.backup.incremental.serializer.IncBackupInfoSerializer;
import com.keuin.rdiffbackup.backup.incremental.serializer.SavedIncrementalBackup;
import com.keuin.rdiffbackup.backup.suggestion.BackupNameSuggestionProvider;
import com.keuin.rdiffbackup.operation.abstracts.InvokableAsyncBlockingOperation;
import com.keuin.rdiffbackup.util.PrintUtil;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;

import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;

import static com.keuin.rdiffbackup.backup.BackupFilesystemUtil.getBackupSaveDirectory;
import static com.keuin.rdiffbackup.backup.BackupFilesystemUtil.getIncrementalBackupBaseDirectory;
import static com.keuin.rdiffbackup.util.PrintUtil.msgErr;
import static com.keuin.rdiffbackup.util.PrintUtil.msgInfo;
import static org.apache.commons.io.FileUtils.forceDelete;

public class DeleteOperation extends InvokableAsyncBlockingOperation {

    private static final Logger LOGGER = Logger.getLogger(DeleteOperation.class.getName());
    private final String backupFilename;
    private final CommandContext<ServerCommandSource> context;

    public DeleteOperation(CommandContext<ServerCommandSource> context, String backupFilename) {
        super("BackupDeletingWorker");
        this.backupFilename = backupFilename;
        this.context = context;
    }

    @Override
    public String toString() {
        return String.format("deletion of %s", backupFilename);
    }

    @Override
    protected void async() {
        delete();
        BackupNameSuggestionProvider.updateCandidateList();
    }

    private void delete() {
        try {
            MinecraftServer server = context.getSource().getServer();
            PrintUtil.info("Deleting backup file " + this.backupFilename);
            File backupFile = new File(getBackupSaveDirectory(server), backupFilename);
            SavedIncrementalBackup incrementalBackup = null;
            if (backupFile.getName().endsWith(".kbi")) {
                incrementalBackup = IncBackupInfoSerializer.fromFile(backupFile);
            }

            // remove .zip or .kbi file
            PrintUtil.info("Deleting file " + backupFilename + "...");
            int tryCounter = 0;
            do {
                if (tryCounter == 5) {
                    String msg = "Failed to delete file " + backupFilename;
                    PrintUtil.error(msg);
                    msgErr(context, msg);
                    return;
                }
                try {
                    if (!backupFile.delete())
                        forceDelete(backupFile);
                } catch (SecurityException | NullPointerException | IOException ignored) {
                }
                ++tryCounter;
            } while (backupFile.exists());


            // If it is an incremental backup, do clean-up
            if (incrementalBackup != null) {
                PrintUtil.info("Cleaning up...");
                IncrementalBackupStorageManager manager =
                        new IncrementalBackupStorageManager(getIncrementalBackupBaseDirectory(server).toPath());
                Iterable<ObjectCollection2> backups = ObjectCollectionSerializer
                        .fromDirectory(BackupFilesystemUtil
                                .getBackupSaveDirectory(context.getSource().getServer()));
                int deleted = manager.deleteObjectCollection(incrementalBackup.getObjectCollection(), backups);
                PrintUtil.info("Deleted " + deleted + " unused file(s).");
            }

            PrintUtil.info("Successfully deleted backup file " + this.backupFilename);
            msgInfo(context, "Successfully deleted backup file " + this.backupFilename);
        } catch (IOException e) {
            LOGGER.severe("Failed to delete backup: " + e);
        }
    }
}
