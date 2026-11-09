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
import { DatabaseContext, loadDatabase } from "./database.ts";

function DatabaseProvider({ children }: { children: React.ReactNode }) {
  const [database, setDatabase] = useState<Database | null>(null);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    loadDatabase()
      .then(setDatabase)
      .catch((err) => {
        console.error("Failed to load database:", err);
        setError(String(err));
      });
  }, []);

  if (error) return <div>Failed to load database: {error}</div>;
  if (!database) return <div>Loading...</div>;

  return <DatabaseContext.Provider value={database}>{children}</DatabaseContext.Provider>;
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
