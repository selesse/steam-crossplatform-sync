package com.selesse.steamcrossplatformsync.gamesessions;

import com.selesse.steamcrossplatformsync.gamesessions.database.Database;
import com.selesse.steamcrossplatformsync.gamesessions.database.SqliteDatabaseLocation;
import com.selesse.steamcrossplatformsync.gamesessions.database.SqliteFile;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class GameSessionRepository {
    private static GameSessionRepository instance;

    public static GameSessionRepository getInstance() {
        if (instance == null) {
            instance = new GameSessionRepository();
        }
        return instance;
    }

    public static GameSessionRepository getInstance(SqliteFile sqliteFile) {
        instance = new GameSessionRepository(sqliteFile);
        return instance;
    }

    private final SqliteFile sqliteFile;

    GameSessionRepository() {
        this(SqliteDatabaseLocation.get());
    }

    GameSessionRepository(SqliteFile sqliteFile) {
        this.sqliteFile = sqliteFile;
        Database.migrate(sqliteFile);
    }

    private static final String INSERT_GAME = "INSERT OR IGNORE INTO GAMES (NAME, STEAM_APP_ID) VALUES (?, ?)";
    private static final String FETCH_GAME = "SELECT steam_app_id FROM GAMES WHERE NAME = ?";
    private static final String SESSION_INSERT =
            "INSERT INTO GAMING_SESSIONS (HOST, STARTED_AT, FINISHED_AT, STEAM_APP_ID, ACTIVE_PLAYTIME_SECONDS) VALUES (?, ?, ?, ?, ?)";

    public void save(GameSessionRecord gameSessionRecord) {
        try (Connection connection = Database.open(sqliteFile)) {
            insertOrIgnoreGame(connection, gameSessionRecord);
            insertGamingSession(connection, gameSessionRecord.gameId(), gameSessionRecord);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private static void insertOrIgnoreGame(Connection connection, GameSessionRecord gameSessionRecord)
            throws SQLException {
        PreparedStatement preparedStatement = connection.prepareStatement(INSERT_GAME);
        preparedStatement.setString(1, gameSessionRecord.gameName());
        preparedStatement.setLong(2, gameSessionRecord.gameId());
        preparedStatement.executeUpdate();
    }

    public List<Long> findUnknownGameIds() {
        String sql = "SELECT STEAM_APP_ID FROM GAMES WHERE NAME IS NULL OR NAME = ''";
        try (Connection connection = Database.open(sqliteFile);
                PreparedStatement ps = connection.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            List<Long> ids = new ArrayList<>();
            while (rs.next()) {
                ids.add(rs.getLong(1));
            }
            return ids;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void updateGameName(long steamAppId, String name) {
        String sql = "UPDATE GAMES SET NAME = ? WHERE STEAM_APP_ID = ?";
        try (Connection connection = Database.open(sqliteFile);
                PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, name);
            ps.setLong(2, steamAppId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void insertGamingSession(Connection connection, long gameId, GameSessionRecord gameSessionRecord)
            throws SQLException {
        var preparedStatement = connection.prepareStatement(SESSION_INSERT);
        preparedStatement.setString(1, gameSessionRecord.hostname());
        preparedStatement.setString(2, DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(gameSessionRecord.startedAt()));
        preparedStatement.setString(3, DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(gameSessionRecord.finishedAt()));
        preparedStatement.setLong(4, gameId);
        preparedStatement.setLong(5, gameSessionRecord.activePlaytimeSeconds());
        preparedStatement.executeUpdate();
    }
}
