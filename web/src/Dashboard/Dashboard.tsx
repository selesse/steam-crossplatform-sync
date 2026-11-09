import { Link } from "react-router-dom";
import { useDatabase } from "../database";
import { getDashboardStats, getRecentSessions } from "../services/dashboard";
import "./Dashboard.css";

function formatHours(hours: number): string {
  if (hours >= 1000) return `${(hours / 1000).toFixed(1)}k`;
  return `${Math.round(hours)}`;
}

function formatDuration(minutes: number): string {
  const h = Math.floor(minutes / 60);
  const m = Math.round(minutes % 60);
  if (h === 0) return `${m}m`;
  if (m === 0) return `${h}h`;
  return `${h}h ${m}m`;
}

function formatDate(instant: Temporal.Instant): string {
  return instant
    .toZonedDateTimeISO(Temporal.Now.timeZoneId())
    .toLocaleString(undefined, { month: "short", day: "numeric", year: "numeric" });
}

export default function Dashboard() {
  const db = useDatabase();
  const stats = getDashboardStats(db);
  const recentSessions = getRecentSessions(db, 15);

  const today = Temporal.Now.plainDateISO();
  const thisMonthLabel = today.toLocaleString(undefined, { month: "long", year: "numeric" });

  return (
    <div className="dashboard">
      <div className="statCards">
        <div className="statCard">
          <div className="statCard-value">{formatHours(stats.totalHours)}h</div>
          <div className="statCard-label">Hours logged in {today.year}</div>
        </div>
        <div className="statCard">
          <div className="statCard-value">{stats.totalGames}</div>
          <div className="statCard-label">Games played in {today.year}</div>
        </div>
        {stats.thisYearMostPlayed && (
          <div className="statCard">
            <div className="statCard-value statCard-value--game">{stats.thisYearMostPlayed}</div>
            <div className="statCard-label">
              Most played in {today.year} &middot; {formatHours(stats.thisYearMostPlayedHours)}h
            </div>
          </div>
        )}
        {stats.thisMonthMostPlayed && (
          <div className="statCard">
            <div className="statCard-value statCard-value--game">{stats.thisMonthMostPlayed}</div>
            <div className="statCard-label">
              Top game in {thisMonthLabel} &middot; {formatHours(stats.thisMonthMostPlayedHours)}h
            </div>
          </div>
        )}
      </div>

      <h2>Recent sessions</h2>
      {recentSessions.length === 0 ? (
        <p>No sessions found.</p>
      ) : (
        <table className="recentSessions">
          <thead>
            <tr>
              <th>Game</th>
              <th>Host</th>
              <th>Date</th>
              <th>Duration</th>
            </tr>
          </thead>
          <tbody>
            {recentSessions.map((s) => (
              <tr key={`${s.host} ${s.startTime}`}>
                <td>
                  <Link to={`/game?steam_app_id=${s.steamAppId}`}>{s.name}</Link>
                </td>
                <td className="muted">{s.host}</td>
                <td className="muted">{formatDate(s.startTime)}</td>
                <td className="muted">{formatDuration(s.durationMinutes)}</td>
              </tr>
            ))}
          </tbody>
        </table>
      )}
    </div>
  );
}
