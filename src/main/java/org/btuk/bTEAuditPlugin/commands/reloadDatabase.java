package org.btuk.bTEAuditPlugin.commands;

import org.btuk.bTEAuditPlugin.resources.DatabaseManager;
import org.btuk.bTEAuditPlugin.resources.RegionData;
import org.btuk.bTEAuditPlugin.resources.ReloadDatabase;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Objects;

public class reloadDatabase implements CommandExecutor {

    private final JavaPlugin plugin;

    public reloadDatabase(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String @NotNull [] strings) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {

            DatabaseManager databaseManager = new DatabaseManager(plugin);
            databaseManager.initDatabase();
            Connection databaseConnection = databaseManager.getConnection();

            ReloadDatabase.reloadDatabase(plugin, databaseManager, databaseConnection, commandSender);

            databaseManager.closeDatabase();
        });

        return false;
    }
}