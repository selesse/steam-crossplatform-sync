import { Bar, BarChart, Legend, ResponsiveContainer, Tooltip, XAxis, YAxis } from "recharts";
import HourSumTooltip from "../Tooltip/HourSumTooltip";
import { pickColor } from "../color-wheel";
import type { GameByMonth } from "../types/playtime";

interface Props {
  gameByMonth: GameByMonth[];
}

export default function PlaytimeByGameGraph({ gameByMonth }: Props) {
  const uniqueHosts = [...new Set(gameByMonth.map((x) => x.host))].sort();

  const graphFormat: any[] = [];
  gameByMonth.map((gameStats) => {
    const month = gameStats.month;
    const gameMaybe = graphFormat.find((x) => x.name === gameStats.gameName && x.month === month);
    if (!gameMaybe) {
      graphFormat.push({
        name: gameStats.gameName,
        [gameStats.host]: gameStats.hours,
        month: gameStats.month,
      });
    }
    if (gameMaybe) {
      gameMaybe[gameStats.host] = gameStats.hours;
    }
  });

  return (
    <ResponsiveContainer width="100%" height="100%" minWidth={200} maxHeight={600}>
      <BarChart data={graphFormat}>
        <XAxis dataKey="month" />
        <YAxis />
        <Tooltip content={<HourSumTooltip />} />
        <Legend />
        {uniqueHosts.map((host) => {
          return <Bar key={host} dataKey={host} stackId="a" fill={pickColor(host)} />;
        })}
      </BarChart>
    </ResponsiveContainer>
  );
}
