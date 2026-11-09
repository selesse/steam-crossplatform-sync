export const getPaddedPeriod = (year: number, month: number) => {
  return `${year}-${String(month).padStart(2, "0")}`;
};

export const getPaddedCurrentPeriod = () => {
  const today = Temporal.Now.plainDateISO();
  return `${today.year}-${String(today.month).padStart(2, "0")}`;
};
