import { useEffect, useState } from "react";

export interface ChartDimensions {
  yAxisWidth: number;
  xAxisHeight: number;
  showAxisLabels: boolean;
}

function getDimensions(width: number): ChartDimensions {
  if (width < 600) {
    return { yAxisWidth: 35, xAxisHeight: 25, showAxisLabels: false };
  }
  return { yAxisWidth: 50, xAxisHeight: 40, showAxisLabels: true };
}

export function useChartDimensions(): ChartDimensions {
  const [dims, setDims] = useState(() => getDimensions(window.innerWidth));

  useEffect(() => {
    const handler = () => setDims(getDimensions(window.innerWidth));
    window.addEventListener("resize", handler);
    return () => window.removeEventListener("resize", handler);
  }, []);

  return dims;
}
