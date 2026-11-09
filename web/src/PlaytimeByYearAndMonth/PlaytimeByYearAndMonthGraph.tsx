import { useState } from "react";
import { Area, AreaChart, Legend, ResponsiveContainer, Tooltip, XAxis, YAxis } from "recharts";
import HourSumTooltip from "../Tooltip/HourSumTooltip";
import { pickColorAt } from "../color-wheel";
import { useChartDimensions } from "../hooks";
import type { PlaytimeByYearAndMonth } from "../types/playtime";

const MONTHS: [string, string][] = [
  ["01", "Jan"],
  ["02", "Feb"],
  ["03", "Mar"],
  ["04", "Apr"],
  ["05", "May"],
  ["06", "Jun"],
  ["07", "Jul"],
  ["08", "Aug"],
  ["09", "Sep"],
  ["10", "Oct"],
  ["11", "Nov"],
  ["12", "Dec"],
];

interface Props {
  data: PlaytimeByYearAndMonth[];
  uniqueYears: string[];
}

export default function PlaytimeByYearAndMonthGraph({ data, uniqueYears }: Props) {
  const [activeYear, setActiveYear] = useState<string | null>(null);
  const { yAxisWidth, showAxisLabels } = useChartDimensions();

  const byMonth: Record<string, Record<string, number>> = {};
  for (const entry of data) {
    byMonth[entry.month] ||= {};
    byMonth[entry.month][entry.year] = entry.numberOfHours;
  }

  const chartData = MONTHS.map(([month, label]) => ({
    month: label,
    ...Object.fromEntries(uniqueYears.map((year) => [year, byMonth[month]?.[year] ?? null])),
  }));

  const handleLegendClick = (year: string) => {
    setActiveYear((prev) => (prev === year ? null : year));
  };

  const renderLegend = () => (
    <div style={{ display: "flex", justifyContent: "center", gap: "16px", flexWrap: "wrap", marginTop: "8px" }}>
      {uniqueYears.map((year) => {
        const isActive = activeYear === null || activeYear === year;
        return (
          <button
            type="button"
            key={year}
            onClick={() => handleLegendClick(year)}
            onMouseEnter={() => setActiveYear(year)}
            onMouseLeave={() => setActiveYear(null)}
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
                backgroundColor: pickColorAt(uniqueYears.indexOf(year)),
              }}
            />
            {year}
          </button>
        );
      })}
    </div>
  );

  return (
    <ResponsiveContainer width="100%" height="100%" maxHeight={600}>
      <AreaChart data={chartData}>
        <XAxis dataKey="month" />
        <YAxis
          label={showAxisLabels ? { value: "Hours", angle: -90, position: "insideLeft" } : undefined}
          width={yAxisWidth}
        />
        <Tooltip content={<HourSumTooltip />} />
        <Legend content={renderLegend} />
        {uniqueYears.map((year) => {
          const isActive = activeYear === null || activeYear === year;
          return (
            <Area
              key={year}
              type="monotone"
              dataKey={year}
              stackId="a"
              stroke={pickColorAt(uniqueYears.indexOf(year))}
              fill={pickColorAt(uniqueYears.indexOf(year))}
              fillOpacity={isActive ? 0.6 : 0.1}
              strokeOpacity={isActive ? 1 : 0.2}
              strokeWidth={isActive ? 2 : 1}
              connectNulls={false}
            />
          );
        })}
      </AreaChart>
    </ResponsiveContainer>
  );
}
