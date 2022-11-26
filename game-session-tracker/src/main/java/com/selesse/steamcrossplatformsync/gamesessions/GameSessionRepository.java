package com.selesse.steamcrossplatformsync.gamesessions;

import com.selesse.steamcrossplatformsync.gamesessions.database.Database;
import com.selesse.steamcrossplatformsync.gamesessions.database.SqliteDatabaseLocation;
import com.selesse.steamcrossplatformsync.gamesessions.database.SqliteFile;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;

class GameSessionRepository {
    private static GameSessionRepository instance;

    public static GameSessionRepository getInstance() {
        if (instance == null) {
            instance = new GameSessionRepository();
        }
        return instance;
    }

    static GameSessionRepository getInstance(SqliteFile sqliteFile) {
        instance = new GameSessionRepository(sqliteFile);
        return instance;
    }

    private final SqliteFile sqliteFile;

    GameSessionRepository() {
        this.sqliteFile = SqliteDatabaseLocation.get();
    }

    GameSessionRepository(SqliteFile sqliteFile) {
        this.sqliteFile = sqliteFile;
    }

    private static final String INSERT_GAME = "INSERT OR IGNORE INTO GAMES (NAME, STEAM_APP_ID) VALUES (?, ?)";
    private static final String FETCH_GAME = "SELECT steam_app_id FROM GAMES WHERE NAME = ?";
    private static final String SESSION_INSERT =
            "INSERT INTO GAMING_SESSIONS (HOST, STARTED_AT, FINISHED_AT, STEAM_APP_ID) VALUES (?, ?, ?, ?)";

    public void save(GameSessionRecord gameSessionRecord) {
        try (Connection connection = Database.getConnection(sqliteFile)) {
            long gameId = insertOrFetchGameId(connection, gameSessionRecord);
            insertGamingSession(connection, gameId, gameSessionRecord);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private static long insertOrFetchGameId(Connection connection, GameSessionRecord gameSessionRecord)
            throws SQLException {
        PreparedStatement preparedStatement = connection.prepareStatement(INSERT_GAME);
        preparedStatement.setString(1, gameSessionRecord.game().getName());
        preparedStatement.setLong(2, gameSessionRecord.game().getId());
        preparedStatement.executeUpdate();
        long gameId = preparedStatement.getGeneratedKeys().getLong(1);
        if (gameId == 0) {
            var statement = connection.prepareStatement(FETCH_GAME);
            statement.setString(1, gameSessionRecord.game().getName());
            gameId = statement.executeQuery().getLong(1);
        }
        return gameId;
    }

    private void insertGamingSession(Connection connection, long gameId, GameSessionRecord gameSessionRecord)
            throws SQLException {
        var preparedStatement = connection.prepareStatement(SESSION_INSERT);
        preparedStatement.setString(1, gameSessionRecord.hostname());
        preparedStatement.setString(2, DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(gameSessionRecord.startedAt()));
        preparedStatement.setString(3, DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(gameSessionRecord.finishedAt()));
        preparedStatement.setLong(4, gameId);
        preparedStatement.executeUpdate();
    }
}
