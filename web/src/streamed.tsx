import { pickColor } from "./color-wheel";

// Time a machine played by streaming from another is drawn in that machine's
// own color with a darker 45° hatch: it counts toward the machine you held,
// and the texture says something else ran it.

export const streamedKey = (host: string) => `${host} (streamed)`;

export const streamedName = (host: string, sources: Map<string, string[]>) => {
  const from = sources.get(host);
  return from ? `${host}, streamed from ${from.join(", ")}` : streamedKey(host);
};

const patternId = (host: string, hosts: string[]) => `streamed-${hosts.indexOf(host)}`;

export const streamedFill = (host: string, hosts: string[]) => `url(#${patternId(host, hosts)})`;

function shade(hex: string, amount: number): string {
  const channels = [1, 3, 5].map((i) => Math.round(Number.parseInt(hex.slice(i, i + 2), 16) * (1 - amount)));
  return `rgb(${channels.join(", ")})`;
}

/**
 * SVG pattern definitions for a chart's streamed fills. Call it inside the chart rather than
 * rendering it as a component: Recharts drops child components it doesn't know, but passes a plain
 * <defs> through.
 */
export function streamedPatterns(hosts: string[]) {
  return (
    <defs>
      {hosts.map((host) => (
        <pattern
          key={host}
          id={patternId(host, hosts)}
          width={6}
          height={6}
          patternUnits="userSpaceOnUse"
          patternTransform="rotate(45)"
        >
          <rect width={6} height={6} fill={pickColor(host)} />
          <line x1={0} y1={0} x2={0} y2={6} stroke={shade(pickColor(host), 0.35)} strokeWidth={3} />
        </pattern>
      ))}
    </defs>
  );
}

/** A legend entry explaining the hatch, drawn in neutral gray so it names no machine. */
export function StreamedLegendSwatch() {
  return (
    <span style={{ display: "flex", alignItems: "center", gap: "6px", opacity: 0.85 }}>
      <svg width={12} height={12} aria-hidden="true">
        <defs>
          <pattern
            id="streamed-legend"
            width={4}
            height={4}
            patternUnits="userSpaceOnUse"
            patternTransform="rotate(45)"
          >
            <rect width={4} height={4} fill="#bbbbbb" />
            <line x1={0} y1={0} x2={0} y2={4} stroke="#777777" strokeWidth={2} />
          </pattern>
        </defs>
        <rect width={12} height={12} rx={2} fill="url(#streamed-legend)" />
      </svg>
      Streamed from another machine
    </span>
  );
}
