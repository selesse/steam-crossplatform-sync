import { useEffect, useState } from "react";
import { BrowserRouter, Routes as ReactRouterRoutes, Route } from "react-router-dom";
import type { Database } from "sql.js";
import App from "./App.tsx";
import Dashboard from "./Dashboard/Dashboard.tsx";
import GamingCalendar from "./GamingCalendar/GamingCalendar.tsx";
import { MonthStats } from "./MonthStats/index.ts";
import PlaytimeByGame from "./PlaytimeByGame/PlaytimeByGame.tsx";
import PlaytimeByHost from "./PlaytimeByHost/PlaytimeByHost.tsx";
import PlaytimeByHostAndMonth from "./PlaytimeByHostAndMonth/PlaytimeByHostAndMonth.tsx";
import { PlaytimeByHostAndYear } from "./PlaytimeByHostAndYear";
import { PlaytimeByYearAndMonth } from "./PlaytimeByYearAndMonth";
import { DatabaseContext, NoDatabasesError, loadDatabase } from "./database.ts";

type DatabaseState =
  | { status: "loading" }
  | { status: "missing" }
  | { status: "failed"; error: string }
  | { status: "ready"; database: Database };

function DatabaseProvider({ children }: { children: React.ReactNode }) {
  const [state, setState] = useState<DatabaseState>({ status: "loading" });

  useEffect(() => {
    let cancelled = false;
    loadDatabase()
      .then((database) => !cancelled && setState({ status: "ready", database }))
      .catch((err) => {
        if (cancelled) return;
        if (err instanceof NoDatabasesError) {
          setState({ status: "missing" });
        } else {
          console.error("Failed to load database:", err);
          setState({ status: "failed", error: String(err) });
        }
      });
    return () => {
      cancelled = true;
    };
  }, []);

  switch (state.status) {
    case "loading":
      return <div>Loading...</div>;
    case "missing":
      return (
        <div>
          <h2>No playtime databases yet</h2>
          <p>Point the dashboard at one or more of the daemon's databases from the web directory, then reload:</p>
          <pre>bun run use-databases ~/.local/share/steam-crossplatform-sync/steam-logs.sqlite3</pre>
        </div>
      );
    case "failed":
      return <div>Failed to load database: {state.error}</div>;
    case "ready":
      return <DatabaseContext.Provider value={state.database}>{children}</DatabaseContext.Provider>;
  }
}

export default function Routes() {
  return (
    <BrowserRouter>
      <DatabaseProvider>
        <ReactRouterRoutes>
          <Route path="/" element={<App />}>
            <Route index element={<Dashboard />} />
            <Route path="/playtime-by-host-and-year" element={<PlaytimeByHostAndYear />} />
            <Route path="/playtime-by-host-and-month" element={<PlaytimeByHostAndMonth />} />
            <Route path="/playtime-by-host" element={<PlaytimeByHost />} />
            <Route path="/playtime-by-year-and-month" element={<PlaytimeByYearAndMonth />} />
            <Route path="/monthly/:period?" element={<MonthStats />} />
            <Route path="/game/:steamAppId?" element={<PlaytimeByGame />} />
            <Route path="/calendar" element={<GamingCalendar />} />
          </Route>
        </ReactRouterRoutes>
      </DatabaseProvider>
    </BrowserRouter>
  );
}
