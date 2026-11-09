const wrapText = (text: string, width: number, fontSize: number) => {
  const words = text.split(" ");
  let currentLine = words[0];
  const lines = [];

  for (let i = 1; i < words.length; i++) {
    const testLine = `${currentLine} ${words[i]}`;
    const testWidth = measureTextWidth(testLine, fontSize);

    if (testWidth <= width) {
      currentLine = testLine;
    } else {
      lines.push(currentLine);
      currentLine = words[i];
    }
  }

  lines.push(currentLine);
  return lines;
};

const measureTextWidth = (text: string, fontSize: number) => {
  const canvas = document.createElement("canvas");
  const context = canvas.getContext("2d");
  if (!context) {
    throw "Could not create canvas";
  }
  context.font = `${fontSize}px sans-serif`;

  return context.measureText(text).width;
};

export default function ResizingXAxisLabels(props: any) {
  const { x, y, payload, games } = props;
  const fontSize = 15;
  const textLines = wrapText(payload.value, window.innerWidth / (2 * games.length), fontSize);
  const lineHeight = fontSize;

  return (
    <g transform={`translate(${x},${y})`}>
      {textLines.map((line, index) => (
        <text
          // biome-ignore lint/suspicious/noArrayIndexKey: wrapped lines can repeat, and their order is their identity
          key={`${line} ${index}`}
          x={0}
          y={index * lineHeight}
          dy={16}
          textAnchor="middle"
          fill={"#999"}
          fontSize={fontSize}
        >
          <title>{payload.value}</title>
          {line}
        </text>
      ))}
    </g>
  );
}
