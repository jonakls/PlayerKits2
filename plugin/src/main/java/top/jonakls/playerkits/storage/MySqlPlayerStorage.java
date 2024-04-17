package top.jonakls.playerkits.storage;

import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pk.ajneb97.api.model.player.PlayerDataKit;
import pk.ajneb97.api.model.player.PlayerModel;
import pk.ajneb97.database.MySQLConnection;
import top.jonakls.playerkits.api.storage.ObjectStorage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class MySqlPlayerStorage implements ObjectStorage<PlayerModel, PlayerDataKit> {

    private static final Executor EXECUTOR = Executors.newCachedThreadPool();
    private final MySQLConnection connection;
    private final ComponentLogger logger;

    public MySqlPlayerStorage(final @NotNull MySQLConnection connection, final @NotNull ComponentLogger logger) {
        this.connection = connection;
        this.logger = logger;
    }

    @Override
    public void create(PlayerModel object) {
        try {
            final Connection connection = this.connection.getConnection();
            final PreparedStatement statement = connection.prepareStatement(
                    "INSERT INTO playerkits_players (UUID, PLAYER_NAME) VALUE (?, ?)"
            );

            statement.setString(1, object.id());
            statement.setString(2, object.getName());

            statement.executeUpdate();
        } catch (SQLException e) {
            logger.error("An error occurred while creating player data", e);
        }
    }

    @Override
    public void saveOne(PlayerModel object, PlayerDataKit kit) {
        CompletableFuture.runAsync(() -> this.saveOneSync(object, kit), EXECUTOR);
    }

    @Override
    public void saveOneSync(PlayerModel object, PlayerDataKit kit) {
        try (final Connection connection = this.connection.getConnection()) {
            PreparedStatement statement = connection.prepareStatement(
                    "SELECT NAME FROM playerkits_players_kits WHERE UUID = ? AND NAME = ?"
            );

            statement.setString(1, object.id());
            statement.setString(2, kit.id());

            if (statement.executeQuery().next()) {
                PreparedStatement updateStatement = connection.prepareStatement(
                        "UPDATE playerkits_players_kits SET COOLDOWN = ?, ONE_TIME = ?, BOUGHT = ? WHERE UUID = ? AND NAME = ?;"
                );

                updateStatement.setLong(1, kit.getCooldown());
                updateStatement.setBoolean(2, kit.isOneTime());
                updateStatement.setBoolean(3, kit.isBought());
                updateStatement.setString(4, object.id());
                updateStatement.setString(5, kit.id());

                updateStatement.executeUpdate();
            } else {
                PreparedStatement insertStatement = connection.prepareStatement(
                        "INSERT INTO playerkits_players_kits (UUID, NAME, COOLDOWN, ONE_TIME, BOUGHT) VALUE (?, ?, ?, ?, ?)"
                );

                insertStatement.setString(1, object.id());
                insertStatement.setString(2, kit.id());
                insertStatement.setLong(3, kit.getCooldown());
                insertStatement.setBoolean(4, kit.isOneTime());
                insertStatement.setBoolean(5, kit.isBought());

                insertStatement.executeUpdate();
            }

        } catch (SQLException e) {
            logger.error("An error occurred while saving player data", e);
        }

    }

    @Override
    public void deleteOne(PlayerModel object, PlayerDataKit kit) {

    }

    @Override
    public void deleteOneSync(PlayerModel object, PlayerDataKit kit) {

    }

    @Override
    public PlayerModel findSync(String id) {
        try (final Connection connection = this.connection.getConnection()) {

            PreparedStatement statement = connection.prepareStatement(
                    "SELECT pp.UUID, " +
                            "       pp.PLAYER_NAME, " +
                            "       ppk.NAME, " +
                            "       ppk.COOLDOWN, " +
                            "       ppk.ONE_TIME, " +
                            "       ppk.BOUGHT " +
                            "FROM playerkits_players pp " +
                            "         LEFT JOIN playerkits_players_kits ppk " +
                            "                   ON pp.UUID = ppk.UUID " +
                            "WHERE pp.UUID = ?"
            );

            statement.setString(1, id);

            final ResultSet resultSet = statement.executeQuery();
            final PlayerModel playerModel = new PlayerModel(
                    resultSet.getString("UUID"),
                    resultSet.getString("PLAYER_NAME")
            );

            while (resultSet.next()) {
                PlayerDataKit dataKit = new PlayerDataKit(resultSet.getString("NAME"));
                dataKit.setCooldown(resultSet.getLong("COOLDOWN"));
                dataKit.setOneTime(resultSet.getBoolean("ONE_TIME"));
                dataKit.setBought(resultSet.getBoolean("BOUGHT"));

                playerModel.addKit(dataKit);
            }

            return playerModel;

        } catch (SQLException e) {
            logger.error("An error occurred while finding player data", e);
        }
        return null;
    }

    @Override
    @Nullable
    public PlayerModel find(String id) {
        return CompletableFuture.supplyAsync(() -> this.findSync(id), EXECUTOR)
                .exceptionally(throwable -> {
                    logger.error("An error occurred while finding player data", throwable);
                    return null;
                }).whenComplete((playerModel, __) -> {
                    if (playerModel == null) {
                        logger.error("Player data not found");
                    }
                }).join();
    }

    @Override
    public boolean contains(String id) {
        return false;
    }

    @Override
    public void clear() {

    }

    @Override
    public void clearSync() {

    }
}
