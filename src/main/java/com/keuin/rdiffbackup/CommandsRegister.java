package com.keuin.rdiffbackup;

import com.keuin.rdiffbackup.backup.suggestion.BackupNameSuggestionProvider;
import com.keuin.rdiffbackup.ui.Commands;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;

public final class CommandsRegister {

    public static final int DEFAULT_REQUIRED_LEVEL = 2;

    // First make method to register
    public static void registerCommands(CommandDispatcher<ServerCommandSource> dispatcher) {

        // register /rdiff and /rdiff help for help menu
        dispatcher.register(CommandManager.literal("rdiff")
                .requires(Permissions.require("rdiff.root", true))
                .executes(Commands::rdiff));
        dispatcher.register(CommandManager.literal("rdiff")
                .then(CommandManager.literal("help")
                        .requires(Permissions.require("rdiff.help", true))
                        .executes(Commands::help)));

        // register /rdiff list for showing the backup list. OP is required.
        dispatcher.register(CommandManager.literal("rdiff")
                .then(CommandManager.literal("list")
                        .requires(Permissions.require("rdiff.list", DEFAULT_REQUIRED_LEVEL))
                        .executes(Commands::list)));

        // register /rdiff backup full [name] for performing a full backup. OP is required.
        dispatcher.register(CommandManager.literal("rdiff").then(CommandManager.literal("backup").then(CommandManager.literal("full").then(
                        CommandManager.argument("backupName", StringArgumentType.greedyString())
                                .requires(Permissions.require("rdiff.backup", DEFAULT_REQUIRED_LEVEL))
                                .executes(Commands::primitiveBackup)
                ).requires(Permissions.require("rdiff.backup", DEFAULT_REQUIRED_LEVEL))
                .executes(Commands::primitiveBackupWithDefaultName))));

        // register /rdiff backup incremental [name] for performing an incremental backup. OP is required.
        dispatcher.register(CommandManager.literal("rdiff").then(CommandManager.literal("backup").then(CommandManager.literal("incremental")
                        .then(CommandManager.argument("backupName", StringArgumentType.greedyString())
                                .requires(Permissions.require("rdiff.backup", DEFAULT_REQUIRED_LEVEL))
                                .executes(Commands::incrementalBackup))
                        .requires(Permissions.require("rdiff.backup", DEFAULT_REQUIRED_LEVEL))
                        .executes(Commands::incrementalBackupWithDefaultName)
                )));

        // register /rdiff restore <name> for performing restore. OP is required.
        dispatcher.register(CommandManager.literal("rdiff")
                .then(CommandManager.literal("restore")
                        .then(CommandManager.argument("backupName", StringArgumentType.greedyString())
                                .suggests(BackupNameSuggestionProvider.getProvider())
                                .requires(Permissions.require("rdiff.restore", DEFAULT_REQUIRED_LEVEL))
                                .executes(Commands::restore))
                        .requires(Permissions.require("rdiff.list", DEFAULT_REQUIRED_LEVEL))
                        .executes(Commands::list)));

        // register /rdiff delete [name] for deleting an existing backup. OP is required.
        dispatcher.register(CommandManager.literal("rdiff")
                .then(CommandManager.literal("delete")
                        .then(CommandManager.argument("backupName", StringArgumentType.greedyString())
                                .suggests(BackupNameSuggestionProvider.getProvider())
                                .requires(Permissions.require("rdiff.delete", DEFAULT_REQUIRED_LEVEL))
                                .executes(Commands::delete))
                        .requires(Permissions.require("rdiff.delete", DEFAULT_REQUIRED_LEVEL))));

        // register /rdiff confirm for confirming the execution. OP is required.
        dispatcher.register(CommandManager.literal("rdiff")
                .then(CommandManager.literal("confirm")
                        .requires(Permissions.require("rdiff.confirm", DEFAULT_REQUIRED_LEVEL))
                        .executes(Commands::confirm)));

        // register /rdiff cancel for cancelling the execution to be confirmed. OP is required.
        dispatcher.register(CommandManager.literal("rdiff")
                .then(CommandManager.literal("cancel")
                        .requires(Permissions.require("rdiff.cancel", DEFAULT_REQUIRED_LEVEL))
                        .executes(Commands::cancel)));

        // register /rdiff recent for showing the most recent backup. OP is required.
        dispatcher.register(CommandManager.literal("rdiff")
                .then(CommandManager.literal("recent")
                        .requires(Permissions.require("rdiff.recent", DEFAULT_REQUIRED_LEVEL))
                        .executes(Commands::prev)));

//        // register /rdiff setMethod for selecting backup method (zip, incremental)
//        dispatcher.register(CommandManager.literal("rdiff").then(CommandManager.literal("setMethod").then(CommandManager.argument("backupMethod", StringArgumentType.string()).suggests(BackupMethodSuggestionProvider.getProvider()).requires(PermissionValidator::op).executes(KBCommands::setMethod))));
    }
}
