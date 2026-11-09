import { useState } from "react";
import { Cell, Legend, Pie, PieChart, ResponsiveContainer, Tooltip } from "recharts";
import { pickColor } from "../color-wheel";
import type { PlaytimeByHostTotal } from "../types/playtime";

interface Props {
  data: PlaytimeByHostTotal[];
}

export default function PlaytimeByHostGraph({ data }: Props) {
  const [activeHost, setActiveHost] = useState<string | null>(null);

  const chartData = data.map((value) => ({
    name: value.host,
    value: value.sumHours,
  }));

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
      {chartData.map((entry) => {
        const isActive = activeHost === null || activeHost === entry.name;
        return (
          <button
            type="button"
            key={entry.name}
            onClick={() => setActiveHost((prev) => (prev === entry.name ? null : entry.name))}
            onMouseEnter={() => setActiveHost(entry.name)}
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
                backgroundColor: pickColor(entry.name),
              }}
            />
            {entry.name}
          </button>
        );
      })}
    </div>
  );

  return (
    <ResponsiveContainer width="100%" height="100%" minWidth={200} maxHeight={600}>
      <PieChart>
        <Pie data={chartData} outerRadius={"80%"} label={renderLabel} dataKey="value" height={500}>
          {chartData.map((entry) => {
            const isActive = activeHost === null || activeHost === entry.name;
            return <Cell key={`cell-${entry.name}`} fill={pickColor(entry.name)} opacity={isActive ? 1 : 0.2} />;
          })}
        </Pie>
        <Legend content={renderLegend} />
        <Tooltip content={tooltipContent} />
      </PieChart>
    </ResponsiveContainer>
  );
}
