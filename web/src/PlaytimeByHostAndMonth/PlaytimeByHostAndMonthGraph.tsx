import { useState } from "react";
import { Bar, BarChart, Legend, ResponsiveContainer, Tooltip, XAxis, YAxis } from "recharts";
import HourSumTooltip from "../Tooltip/HourSumTooltip";
import { pickColor } from "../color-wheel";
import { useChartDimensions } from "../hooks";
import type { MonthlyPlaytime } from "../types/playtime";

interface Props {
  playtimeByMonthAndHost: MonthlyPlaytime[];
  uniqueHosts: string[];
}

export default function PlaytimeByHostAndMonthGraph({ uniqueHosts, playtimeByMonthAndHost }: Props) {
  const [activeHost, setActiveHost] = useState<string | null>(null);
  const { yAxisWidth, xAxisHeight, showAxisLabels } = useChartDimensions();

  const dataFormat = {};

  for (const playtime of playtimeByMonthAndHost) {
    (dataFormat as any)[playtime.month] ||= {};
    (dataFormat as any)[playtime.month][playtime.host] = playtime.numberOfHours;
  }

  const data = Object.keys(dataFormat).map((chunk) => {
    const month = (dataFormat as any)[chunk];
    const playtimePerHost = uniqueHosts.map((host) => {
      return { [host]: month[host] || 0 };
    });

    return {
      month: chunk,
      ...Object.assign({}, ...playtimePerHost),
    };
  });

  const renderLegend = () => (
    <div style={{ display: "flex", justifyContent: "center", gap: "16px", flexWrap: "wrap", marginTop: "8px" }}>
      {uniqueHosts.map((host) => {
        const isActive = activeHost === null || activeHost === host;
        return (
          <button
            type="button"
            key={host}
            onClick={() => setActiveHost((prev) => (prev === host ? null : host))}
            onMouseEnter={() => setActiveHost(host)}
            onMouseLeave={() => setActiveHost(null)}
            style={{
              display: "flex",
              alignItems: "center",
              gap: "6px",
              cursor: "pointer",
              background: "none",
              border: "none",
              padding: 0,
              font: "inherit",
              color: "inherit",
              opacity: isActive ? 1 : 0.3,
              transition: "opacity 0.15s",
              userSelect: "none",
            }}
          >
            <span
              style={{
                display: "inline-block",
                width: 12,
                height: 12,
                borderRadius: 2,
                backgroundColor: pickColor(host),
              }}
            />
            {host}
          </button>
        );
      })}
    </div>
  );

  return (
    <ResponsiveContainer width="100%" height="100%" maxHeight={600}>
      <BarChart data={data}>
        <XAxis
          dataKey="month"
          label={showAxisLabels ? { value: "Month", position: "insideBottom", offset: -4 } : undefined}
          height={xAxisHeight}
        />
        <YAxis
          label={showAxisLabels ? { value: "Hours", angle: -90, position: "insideLeft" } : undefined}
          width={yAxisWidth}
        />
        <Tooltip content={<HourSumTooltip />} />
        <Legend content={renderLegend} />
        {uniqueHosts.map((host) => {
          const isActive = activeHost === null || activeHost === host;
          return <Bar key={host} dataKey={host} stackId="a" fill={pickColor(host)} opacity={isActive ? 1 : 0.2} />;
        })}
      </BarChart>
    </ResponsiveContainer>
  );
}
