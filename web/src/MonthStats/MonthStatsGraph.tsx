import { Bar, BarChart, Legend, ResponsiveContainer, Tooltip, XAxis, YAxis } from "recharts";
import HourSumTooltip from "../Tooltip/HourSumTooltip";
import { pickColor } from "../color-wheel";
import { useChartDimensions } from "../hooks";
import type { HoursPlayed } from "../types/playtime";
import ResizingXAxisLabels from "./ResizingXAxisLabels";

interface Props {
  hoursPlayed: HoursPlayed[];
}

export default function MonthStatsGraph({ hoursPlayed }: Props) {
  const { yAxisWidth, showAxisLabels } = useChartDimensions();

  const uniqueHosts = [...new Set(hoursPlayed.map((x) => x.host))].sort();

  const graphFormat: any[] = [];
  hoursPlayed.map((hoursPlayed) => {
    const gameMaybe = graphFormat.find((x) => x.name === hoursPlayed.gameName);
    if (!gameMaybe) {
      graphFormat.push({
        name: hoursPlayed.gameName,
        [hoursPlayed.host]: hoursPlayed.hours,
      });
    }
    if (gameMaybe) {
      gameMaybe[hoursPlayed.host] = hoursPlayed.hours;
    }
  });

  return (
    <ResponsiveContainer width="100%" height="100%" minWidth={200} maxHeight={600}>
      <BarChart data={graphFormat}>
        <XAxis
          dataKey="name"
          tick={<ResizingXAxisLabels games={graphFormat} />}
          height={100}
          tickFormatter={() => ""}
        />
        <YAxis
          label={showAxisLabels ? { value: "Hours", angle: -90, position: "insideLeft" } : undefined}
          width={yAxisWidth}
        />
        <Tooltip content={<HourSumTooltip />} />
        <Legend />
        {uniqueHosts.map((host) => {
          return <Bar key={host} dataKey={host} stackId="a" fill={pickColor(host)} />;
        })}
      </BarChart>
    </ResponsiveContainer>
  );
}
