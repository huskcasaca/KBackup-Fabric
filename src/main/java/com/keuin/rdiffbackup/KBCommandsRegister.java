package com.keuin.rdiffbackup;

import com.keuin.rdiffbackup.backup.suggestion.BackupNameSuggestionProvider;
import com.keuin.rdiffbackup.ui.KBCommands;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;

public final class KBCommandsRegister {

    public static final int DEFAULT_REQUIRED_LEVEL = 2;

    // First make method to register
    public static void registerCommands(CommandDispatcher<ServerCommandSource> dispatcher) {

        // register /kb and /kb help for help menu
        dispatcher.register(CommandManager.literal("kb")
                .requires(Permissions.require("kb.root", true))
                .executes(KBCommands::kb));
        dispatcher.register(CommandManager.literal("kb")
                .then(CommandManager.literal("help")
                        .requires(Permissions.require("kb.help", true))
                        .executes(KBCommands::help)));

        // register /kb list for showing the backup list. OP is required.
        dispatcher.register(CommandManager.literal("kb")
                .then(CommandManager.literal("list")
                        .requires(Permissions.require("kb.list", DEFAULT_REQUIRED_LEVEL))
                        .executes(KBCommands::list)));

        // register /kb backup full [name] for performing a full backup. OP is required.
        dispatcher.register(CommandManager.literal("kb").then(CommandManager.literal("backup").then(CommandManager.literal("full").then(
                        CommandManager.argument("backupName", StringArgumentType.greedyString())
                                .requires(Permissions.require("kb.backup", DEFAULT_REQUIRED_LEVEL))
                                .executes(KBCommands::primitiveBackup)
                ).requires(Permissions.require("kb.backup", DEFAULT_REQUIRED_LEVEL))
                .executes(KBCommands::primitiveBackupWithDefaultName))));

        // register /kb backup incremental [name] for performing an incremental backup. OP is required.
        dispatcher.register(CommandManager.literal("kb").then(CommandManager.literal("backup").then(CommandManager.literal("incremental")
                        .then(CommandManager.argument("backupName", StringArgumentType.greedyString())
                                .requires(Permissions.require("kb.backup", DEFAULT_REQUIRED_LEVEL))
                                .executes(KBCommands::incrementalBackup))
                        .requires(Permissions.require("kb.backup", DEFAULT_REQUIRED_LEVEL))
                        .executes(KBCommands::incrementalBackupWithDefaultName)
                )));

        // register /kb restore <name> for performing restore. OP is required.
        dispatcher.register(CommandManager.literal("kb")
                .then(CommandManager.literal("restore")
                        .then(CommandManager.argument("backupName", StringArgumentType.greedyString())
                                .suggests(BackupNameSuggestionProvider.getProvider())
                                .requires(Permissions.require("kb.restore", DEFAULT_REQUIRED_LEVEL))
                                .executes(KBCommands::restore))
                        .requires(Permissions.require("kb.list", DEFAULT_REQUIRED_LEVEL))
                        .executes(KBCommands::list)));

        // register /kb delete [name] for deleting an existing backup. OP is required.
        dispatcher.register(CommandManager.literal("kb")
                .then(CommandManager.literal("delete")
                        .then(CommandManager.argument("backupName", StringArgumentType.greedyString())
                                .suggests(BackupNameSuggestionProvider.getProvider())
                                .requires(Permissions.require("kb.delete", DEFAULT_REQUIRED_LEVEL))
                                .executes(KBCommands::delete))
                        .requires(Permissions.require("kb.delete", DEFAULT_REQUIRED_LEVEL))));

        // register /kb confirm for confirming the execution. OP is required.
        dispatcher.register(CommandManager.literal("kb")
                .then(CommandManager.literal("confirm")
                        .requires(Permissions.require("kb.confirm", DEFAULT_REQUIRED_LEVEL))
                        .executes(KBCommands::confirm)));

        // register /kb cancel for cancelling the execution to be confirmed. OP is required.
        dispatcher.register(CommandManager.literal("kb")
                .then(CommandManager.literal("cancel")
                        .requires(Permissions.require("kb.cancel", DEFAULT_REQUIRED_LEVEL))
                        .executes(KBCommands::cancel)));

        // register /kb recent for showing the most recent backup. OP is required.
        dispatcher.register(CommandManager.literal("kb")
                .then(CommandManager.literal("recent")
                        .requires(Permissions.require("kb.recent", DEFAULT_REQUIRED_LEVEL))
                        .executes(KBCommands::prev)));

//        // register /kb setMethod for selecting backup method (zip, incremental)
//        dispatcher.register(CommandManager.literal("kb").then(CommandManager.literal("setMethod").then(CommandManager.argument("backupMethod", StringArgumentType.string()).suggests(BackupMethodSuggestionProvider.getProvider()).requires(PermissionValidator::op).executes(KBCommands::setMethod))));
    }
}
