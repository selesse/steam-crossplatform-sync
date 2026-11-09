// Hosts are given colors in the order they first appeared in the database,
// which guarantees distinct colors and keeps existing hosts stable because new
// hosts only ever append. Pastels that sit well on the dark theme, led by its
// sky blue accent.
const COLORS = [
  "#8ecae6", // sky
  "#f4a261", // amber
  "#a8d5ba", // sage
  "#b9a7e8", // lavender
  "#f28482", // coral
  "#f6d776", // gold
  "#e0c9a6", // sand
  "#7fd1b9", // teal
  "#d98cc2", // orchid
  "#9bb7e8", // cornflower
  "#b5d86a", // lime
  "#f0b3c8", // pink
];

const hashCode = (s: string) => {
  return Math.abs(s.split("").reduce((a, b) => ((a << 5) - a + b.charCodeAt(0)) | 0, 0));
};

let hostOrder: string[] = [];

export const setHostOrder = (hosts: string[]) => {
  hostOrder = hosts;
};

export const pickColorAt = (index: number) => COLORS[index % COLORS.length];

export const pickColor = (name: string) => {
  const index = hostOrder.indexOf(name);
  if (index >= 0) {
    return pickColorAt(index);
  }
  return COLORS[hashCode(name) % COLORS.length];
};

function hslToRgb(h: number, saturation: number, lightness: number): [number, number, number] {
  const s = saturation / 100;
  const l = lightness / 100;
  const k = (n: number) => (n + h / 30) % 12;
  const a = s * Math.min(l, 1 - l);
  const f = (n: number) => l - a * Math.max(-1, Math.min(k(n) - 3, Math.min(9 - k(n), 1)));
  return [Math.round(f(0) * 255), Math.round(f(8) * 255), Math.round(f(4) * 255)];
}

export function gameColor(name: string): { background: string; text: string } {
  const hue = hashCode(name) % 360;
  const [r, g, b] = hslToRgb(hue, 65, 52);
  const brightness = (r * 299 + g * 587 + b * 114) / 1000;
  return {
    background: `hsl(${hue}, 65%, 52%)`,
    text: brightness > 125 ? "#000000" : "#ffffff",
  };
}
