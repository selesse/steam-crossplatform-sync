interface Payload {
  name: string;
  value: number;
}

export default function HourSumTooltip(props: any) {
  const { active, payload, label } = props;
  if (active && payload && payload.length) {
    const nonZero = payload.filter((payloadValue: Payload) => payloadValue.value > 0);
    const totalHours = nonZero.reduce((x: number, p: Payload) => x + p.value, 0);
    return (
      <div
        style={{
          display: "grid",
          gridTemplateColumns: "1fr",
          border: "1px solid #ccc",
          padding: "8px",
          width: "200px",
          borderRadius: "4px",
          backgroundColor: "black",
        }}
      >
        <div className="label">{`${label} (${totalHours.toFixed(0)} hours)`}</div>
        {nonZero.map((payloadValue: Payload, index: number) => {
          return (
            <div key={`${payloadValue.name} + ${index}`} style={{ display: "grid", gridTemplateColumns: "1fr 1fr" }}>
              <div className="intro">{`${payloadValue.name}`}</div>
              <div className="intro">{`${payloadValue.value.toFixed(2)} hours`}</div>
            </div>
          );
        })}
      </div>
    );
  }

  return null;
}
