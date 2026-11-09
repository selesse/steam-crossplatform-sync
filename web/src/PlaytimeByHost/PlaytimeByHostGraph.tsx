import { useState } from "react";
import { Cell, Legend, Pie, PieChart, ResponsiveContainer, Tooltip } from "recharts";
import { pickColor } from "../color-wheel";
import { StreamedLegendSwatch, streamedFill, streamedName, streamedPatterns } from "../streamed";
import type { PlaytimeByHostTotal } from "../types/playtime";

interface Props {
  data: PlaytimeByHostTotal[];
  streamSources: Map<string, string[]>;
}

export default function PlaytimeByHostGraph({ data, streamSources }: Props) {
  const [activeHost, setActiveHost] = useState<string | null>(null);

  const hosts = data.map((value) => value.host);
  // A machine's streamed time sits right after its own slice, so the two read as one share.
  const chartData = data.flatMap((value) => {
    const own = { name: value.host, host: value.host, value: value.sumHours - value.streamedHours, streamed: false };
    if (value.streamedHours <= 0) return [own];
    const streamed = {
      name: streamedName(value.host, streamSources),
      host: value.host,
      value: value.streamedHours,
      streamed: true,
    };
    return [own, streamed];
  });

  function renderLabel(entry: { name: string; value: number }) {
    return `${entry.value.toFixed(0)} hours`;
  }

  const tooltipContent = (props: any) => {
    const { active, payload } = props;
    if (active && payload && payload.length) {
      return (
        <div
          style={{
            border: "1px solid #ccc",
            padding: "8px",
            width: "200px",
            borderRadius: "4px",
            backgroundColor: "black",
          }}
        >
          <div>{payload[0].name}</div>
          <div>{payload[0].value.toFixed(0)} hours</div>
        </div>
      );
    }
    return null;
  };

  const renderLegend = () => (
    <div style={{ display: "flex", justifyContent: "center", gap: "16px", flexWrap: "wrap", marginTop: "8px" }}>
      {hosts.map((host) => {
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
      {chartData.some((entry) => entry.streamed) && <StreamedLegendSwatch />}
    </div>
  );

  return (
    <ResponsiveContainer width="100%" height="100%" minWidth={200} maxHeight={600}>
      <PieChart>
        {streamedPatterns(hosts)}
        <Pie data={chartData} outerRadius={"80%"} label={renderLabel} dataKey="value" height={500}>
          {chartData.map((entry) => {
            const isActive = activeHost === null || activeHost === entry.host;
            const fill = entry.streamed ? streamedFill(entry.host, hosts) : pickColor(entry.host);
            return <Cell key={`cell-${entry.name}`} fill={fill} opacity={isActive ? 1 : 0.2} />;
          })}
        </Pie>
        <Legend content={renderLegend} />
        <Tooltip content={tooltipContent} />
      </PieChart>
    </ResponsiveContainer>
  );
}
