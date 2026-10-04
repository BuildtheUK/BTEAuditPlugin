package org.btuk.bTEAuditPlugin.resources;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Objects;
import java.util.logging.Level;

public class ReloadDatabase {
    public static void reloadDatabase (JavaPlugin plugin, DatabaseManager databaseManager, Connection databaseConnection, CommandSender commandSender) {
        ArrayList<String> overworldRegionFiles = new ArrayList<>();

        File worldContainer = Bukkit.getWorldContainer();
        File regionFolder = new File(
                worldContainer,
                Objects.requireNonNull(plugin.getConfig().getString("Earth-World-Name")) + "/region"
        );

        if (!regionFolder.exists() || !regionFolder.isDirectory()) {
            sendMessage("§4Region folder not found at: " + regionFolder.getAbsolutePath(), commandSender, plugin, Level.SEVERE);
            databaseManager.closeDatabase();
            return;
        }

        File[] files = regionFolder.listFiles((dir, name) -> name.endsWith(".mca"));
        if (files == null) {
            databaseManager.closeDatabase();
            return;
        }

        for (File file : files) {
            overworldRegionFiles.add(file.getName());
        }

        ArrayList<RegionData> regionDataList = new ArrayList<>();

        sendMessage("§2Found filepath! Finding regions!", commandSender, plugin, Level.INFO);

        for (String file : overworldRegionFiles) {
            String name = file;
            file = file.substring(2);

            int xPos = Integer.parseInt(file.substring(0, file.indexOf(".")));
            file = file.substring(file.indexOf(".") + 1);
            int zPos = Integer.parseInt(file.substring(0, file.indexOf(".")));

            regionDataList.add(new RegionData(name, xPos, zPos, "Unchecked"));
        }

        sendMessage("§2Found all regions! Adding regions to Database!", commandSender, plugin, Level.INFO);

        for (RegionData regionData : regionDataList) {
            String sql;

            try {
                if (databaseConnection.getMetaData().getDriverName().equalsIgnoreCase("sqlite jdbc")) {
                    sql = "INSERT OR IGNORE INTO regions (name, x, z, status) VALUES (?, ?, ?, ?)";
                } else {
                    sql = "INSERT IGNORE INTO regions (name, x, z, status) VALUES (?, ?, ?, ?)";
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }

            try (PreparedStatement ps = databaseConnection.prepareStatement(sql)) {
                ps.setString(1, regionData.getName());
                ps.setInt(2, regionData.getX());
                ps.setInt(3, regionData.getZ());
                ps.setString(4, "Unchecked");
                ps.executeUpdate();
            } catch (SQLException e) {
                databaseConnection = databaseManager.getConnection();
                sendMessage("§4Error in adding a region to the database, see console for more info",  commandSender, plugin, Level.SEVERE, e);
            }
        }

        sendMessage("§2Successfully added all regions to the database!",  commandSender, plugin, Level.INFO);
        sendMessage("§2Finished reloading the database!",  commandSender, plugin, Level.INFO);
    }

    private static void sendMessage(String message, CommandSender commandSender, Plugin plugin, Level level, Exception... e) {
        Bukkit.getScheduler().runTask(plugin, () -> {
                    if (commandSender != null)
                        commandSender.sendMessage(message);

                    if (e != null && e.length > 0)
                        plugin.getLogger().log(level, message, e);
                    else
                        plugin.getLogger().log(level, message);
                }
        );
    }
}
