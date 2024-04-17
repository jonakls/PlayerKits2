package pk.ajneb97.database;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.scheduler.BukkitRunnable;
import pk.ajneb97.PlayerKits2;
import pk.ajneb97.managers.MessagesManager;
import pk.ajneb97.api.model.player.PlayerModel;
import pk.ajneb97.api.model.player.PlayerDataKit;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MySQLConnection {

    private final PlayerKits2 plugin;
    private HikariConnection connection;
    private String host;
    private String database;
    private String username;
    private String password;
    private int port;

    public MySQLConnection(PlayerKits2 plugin) {
        this.plugin = plugin;
    }

    public void setupMySql() {
        FileConfiguration config = plugin.getConfigsManager().getMainConfigManager().getConfig();
        try {
            host = config.getString("mysql_database.host", "localhost");
            port = Integer.parseInt(config.getString("mysql_database.port", "3306"));
            database = config.getString("mysql_database.database", "player_kits");
            username = config.getString("mysql_database.username", "root");
            password = config.getString("mysql_database.password", "password");
            connection = new HikariConnection(host, port, database, username, password);
            connection.getHikari().getConnection();
            createTables();
            loadData();
            Bukkit.getConsoleSender().sendMessage(MessagesManager.getColoredMessage(plugin.prefix + " &aSuccessfully connected to the Database."));
        } catch (Exception e) {
            Bukkit.getConsoleSender().sendMessage(MessagesManager.getColoredMessage(plugin.prefix + " &cError while connecting to the Database."));
        }
    }


    public String getDatabase() {
        return this.database;
    }

    public Connection getConnection() {
        try {
            return connection.getHikari().getConnection();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public void loadData() {
        ArrayList<PlayerModel> players = new ArrayList<>();
        try (Connection connection = getConnection()) {
            PreparedStatement statement = connection.prepareStatement(
                    "SELECT playerkits_players.UUID, playerkits_players.PLAYER_NAME, " +
                            "playerkits_players_kits.NAME, " +
                            "playerkits_players_kits.COOLDOWN, " +
                            "playerkits_players_kits.ONE_TIME, " +
                            "playerkits_players_kits.BOUGHT " +
                            "FROM playerkits_players LEFT JOIN playerkits_players_kits " +
                            "ON playerkits_players.UUID = playerkits_players_kits.UUID");

            ResultSet result = statement.executeQuery();

            Map<String, PlayerModel> playerMap = new HashMap<>();
            while (result.next()) {
                String uuid = result.getString("UUID");
                String playerName = result.getString("PLAYER_NAME");
                String kitName = result.getString("NAME");
                long cooldown = result.getLong("COOLDOWN");
                boolean oneTime = result.getBoolean("ONE_TIME");
                boolean bought = result.getBoolean("BOUGHT");

                PlayerModel player = playerMap.get(uuid);

                if (player == null) {
                    //Create and add it
                    player = new PlayerModel(playerName, uuid);
                    players.add(player);
                    playerMap.put(uuid, player);
                }

                if (kitName != null) {
                    PlayerDataKit playerDataKit = new PlayerDataKit(kitName);
                    playerDataKit.setCooldown(cooldown);
                    playerDataKit.setOneTime(oneTime);
                    playerDataKit.setBought(bought);
                    player.addKit(playerDataKit);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        plugin.getPlayerDataManager().setPlayers(players);
    }

    public void createTables() {
        try (Connection connection = getConnection()) {
            PreparedStatement playersTable = connection.prepareStatement(
                    "CREATE TABLE IF NOT EXISTS PLAYERKITS_PLAYERS (" +
                            " UUID VARCHAR(200) PRIMARY KEY," +
                            " PLAYER_NAME VARCHAR(50))"
            );
            playersTable.executeUpdate();
            PreparedStatement playersKitsTable = connection.prepareStatement(
                    "CREATE TABLE IF NOT EXISTS PLAYERKITS_PLAYERS_KITS (" +
                            " ID INT AUTO_INCREMENT PRIMARY KEY," +
                            " UUID VARCHAR(200) NOT NULL," +
                            " NAME VARCHAR(100)," +
                            " COOLDOWN BIGINT," +
                            " ONE_TIME BOOLEAN," +
                            " BOUGHT BOOLEAN," +
                            "  FOREIGN KEY (UUID) REFERENCES PLAYERKITS_PLAYERS(UUID)" +
                            ")");
            playersKitsTable.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void getPlayer(String uuid, PlayerCallback callback) {
        new BukkitRunnable() {
            @Override
            public void run() {
                PlayerModel player = null;
                try (Connection connection = getConnection()) {
                    PreparedStatement statement = connection.prepareStatement(
                            "SELECT pp.UUID," +
                                    "       pp.PLAYER_NAME," +
                                    "       ppk.NAME," +
                                    "       ppk.COOLDOWN," +
                                    "       ppk.ONE_TIME," +
                                    "       ppk.BOUGHT " +
                                    "FROM playerkits_players pp " +
                                    "         LEFT JOIN playerkits_players_kits ppk" +
                                    "                   ON pp.UUID = ppk.UUID " +
                                    "WHERE pp.UUID = ?");

                    statement.setString(1, uuid);
                    ResultSet result = statement.executeQuery();

                    boolean firstFind = true;
                    while (result.next()) {
                        String playerName = result.getString("PLAYER_NAME");
                        String kitName = result.getString("NAME");
                        long cooldown = result.getLong("COOLDOWN");
                        boolean oneTime = result.getBoolean("ONE_TIME");
                        boolean bought = result.getBoolean("BOUGHT");
                        if (firstFind) {
                            firstFind = false;
                            player = new PlayerModel(playerName, uuid);
                        }
                        if (kitName != null) {
                            PlayerDataKit playerDataKit = new PlayerDataKit(kitName);
                            playerDataKit.setCooldown(cooldown);
                            playerDataKit.setOneTime(oneTime);
                            playerDataKit.setBought(bought);
                            player.addKit(playerDataKit);
                        }
                    }

                    PlayerModel finalPlayer = player;
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            callback.onDone(finalPlayer);
                        }
                    }.runTask(plugin);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }.runTaskAsynchronously(plugin);
    }

    public void createPlayer(PlayerModel player) {
        new BukkitRunnable() {
            @Override
            public void run() {
                try (Connection connection = getConnection()) {
                    PreparedStatement statement = connection.prepareStatement(
                            "INSERT INTO playerkits_players VALUES (?,?)");

                    statement.setString(1, player.id());
                    statement.setString(2, player.getName());
                    statement.executeUpdate();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }.runTaskAsynchronously(plugin);
    }

    public void updatePlayerName(PlayerModel player) {
        new BukkitRunnable() {
            @Override
            public void run() {
                try (Connection connection = getConnection()) {
                    PreparedStatement statement = connection.prepareStatement(
                            "UPDATE playerkits_players SET PLAYER_NAME = ? WHERE UUID = ?");

                    statement.setString(1, player.getName());
                    statement.setString(2, player.id());
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }.runTaskAsynchronously(plugin);
    }

    public void updateKit(PlayerModel player, PlayerDataKit kit, boolean mustCreate) {
        new BukkitRunnable() {
            @Override
            public void run() {
                try (Connection connection = getConnection()) {
                    PreparedStatement statement = null;
                    if (mustCreate) {
                        // Insert
                        statement = connection.prepareStatement(
                                "INSERT INTO playerkits_players_kits VALUES (?,?,?,?,?)");

                        statement.setString(1, player.id());
                        statement.setString(2, kit.getName());
                        statement.setLong(3, kit.getCooldown());
                        statement.setBoolean(4, kit.isOneTime());
                        statement.setBoolean(5, kit.isBought());
                    } else {
                        // Update
                        statement = connection.prepareStatement(
                                "UPDATE playerkits_players_kits\n" +
                                        "SET COOLDOWN = ?, " +
                                        "    ONE_TIME = ?, " +
                                        "    BOUGHT = ? " +
                                        "WHERE UUID = ? " +
                                        "  AND NAME = ?");

                        statement.setLong(1, kit.getCooldown());
                        statement.setBoolean(2, kit.isOneTime());
                        statement.setBoolean(3, kit.isBought());
                        statement.setString(4, player.id());
                        statement.setString(5, kit.getName());
                    }
                    statement.executeUpdate();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }.runTaskAsynchronously(plugin);
    }

    public void resetKit(String uuid, String kitName) {
        new BukkitRunnable() {
            @Override
            public void run() {
                try (Connection connection = getConnection()) {
                    PreparedStatement statement = connection.prepareStatement(
                            "DELETE " +
                                    "FROM playerkits_players_kits " +
                                    "WHERE UUID = ? " +
                                    "  AND NAME = ? ");

                    statement.setString(1, uuid);
                    statement.setString(2, kitName);
                    statement.executeUpdate();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }.runTaskAsynchronously(plugin);
    }
}
