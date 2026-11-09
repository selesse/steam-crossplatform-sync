package com.selesse.steamcrossplatformsync.gamesessions;

import com.selesse.steamcrossplatformsync.gamesessions.database.Database;
import com.selesse.steamcrossplatformsync.gamesessions.database.SqliteDatabaseLocation;
import com.selesse.steamcrossplatformsync.gamesessions.database.SqliteFile;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class GameSessionRepository {
    private final SqliteFile sqliteFile;

    /** Opens the repository at this machine's default location. */
    public GameSessionRepository() {
        this(SqliteDatabaseLocation.get());
    }

    public GameSessionRepository(SqliteFile sqliteFile) {
        this.sqliteFile = sqliteFile;
        Database.migrate(sqliteFile);
    }

    private static final String INSERT_GAME = "INSERT OR IGNORE INTO GAMES (NAME, STEAM_APP_ID) VALUES (?, ?)";
    private static final String FETCH_GAME = "SELECT steam_app_id FROM GAMES WHERE NAME = ?";
    private static final String SESSION_INSERT =
            "INSERT INTO GAMING_SESSIONS (HOST, STARTED_AT, FINISHED_AT, STEAM_APP_ID, ACTIVE_PLAYTIME_SECONDS, STEAM_ACCOUNT_ID)"
                    + " VALUES (?, ?, ?, ?, ?, ?)";

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

    private static final String REMOTE_PLAY_INSERT = "INSERT OR IGNORE INTO REMOTE_PLAY_SESSIONS"
            + " (HOST, ROLE, REMOTE_HOST, STARTED_AT, FINISHED_AT, STEAM_APP_ID, REMOTE_PLAY_TOGETHER)"
            + " VALUES (?, ?, ?, ?, ?, ?, ?)";

    /**
     * Records the streams not already recorded, so re-reading the same logs is harmless.
     *
     * @return how many were new
     */
    public int saveRemotePlaySessions(List<RemotePlaySessionRecord> records) {
        try (Connection connection = Database.open(sqliteFile);
                PreparedStatement ps = connection.prepareStatement(REMOTE_PLAY_INSERT)) {
            connection.setAutoCommit(false);
            int inserted = 0;
            for (RemotePlaySessionRecord record : records) {
                ps.setString(1, record.host());
                ps.setString(2, record.role().databaseValue());
                ps.setString(3, record.remoteHost());
                ps.setString(4, DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(record.startedAt()));
                ps.setString(5, DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(record.finishedAt()));
                if (record.steamAppId() != null) {
                    ps.setLong(6, record.steamAppId());
                } else {
                    ps.setNull(6, Types.INTEGER);
                }
                if (record.remotePlayTogether() != null) {
                    ps.setInt(7, record.remotePlayTogether() ? 1 : 0);
                } else {
                    ps.setNull(7, Types.INTEGER);
                }
                inserted += ps.executeUpdate();
            }
            connection.commit();
            return inserted;
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
        if (gameSessionRecord.steamAccountId() != null) {
            preparedStatement.setLong(6, gameSessionRecord.steamAccountId());
        } else {
            preparedStatement.setNull(6, Types.INTEGER);
        }
        preparedStatement.executeUpdate();
    }
}
