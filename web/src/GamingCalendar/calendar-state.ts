/**
 * The calendar page's state machine: which date and view are shown, and
 * which session's popup (if any) is open. The toolbar and the keyboard both
 * dispatch the same actions, so they can't disagree about where Back stops.
 *
 * - navigate PREV/NEXT steps a week (week view) or a month (month, agenda),
 *   but never past the earliest session or the current week/month
 * - navigate TODAY, goTo and setView move freely
 * - anything that changes what's shown closes the popup, whose event would
 *   otherwise be off screen
 */

export type View = "week" | "month" | "agenda";

export interface Popup<E> {
  event: E;
  x: number;
  y: number;
}

export interface CalendarState<E> {
  date: Temporal.PlainDate;
  view: View;
  popup: Popup<E> | null;
}

export type CalendarAction<E> =
  | { type: "navigate"; to: "PREV" | "NEXT" | "TODAY"; earliest: Temporal.PlainDate | null; today: Temporal.PlainDate }
  | { type: "goTo"; date: Temporal.PlainDate }
  | { type: "setView"; view: View }
  | { type: "openPopup"; popup: Popup<E> }
  | { type: "closePopup" };

export function initialCalendarState<E>(today: Temporal.PlainDate): CalendarState<E> {
  return { date: today, view: "week", popup: null };
}

export function calendarReducer<E>(state: CalendarState<E>, action: CalendarAction<E>): CalendarState<E> {
  switch (action.type) {
    case "navigate": {
      const date = step(state.date, state.view, action.to, action.earliest, action.today);
      return { ...state, date, popup: null };
    }
    case "goTo":
      return { ...state, date: action.date, popup: null };
    case "setView":
      return { ...state, view: action.view, popup: null };
    case "openPopup":
      return { ...state, popup: action.popup };
    case "closePopup":
      return state.popup === null ? state : { ...state, popup: null };
  }
}

function step(
  date: Temporal.PlainDate,
  view: View,
  to: "PREV" | "NEXT" | "TODAY",
  earliest: Temporal.PlainDate | null,
  today: Temporal.PlainDate,
): Temporal.PlainDate {
  switch (to) {
    case "TODAY":
      return today;
    case "PREV":
      if (!canGoBack(date, view, earliest)) return date;
      return view === "week" ? date.subtract({ weeks: 1 }) : date.subtract({ months: 1 });
    case "NEXT":
      if (!canGoForward(date, view, today)) return date;
      return view === "week" ? date.add({ weeks: 1 }) : date.add({ months: 1 });
  }
}

export function canGoBack(date: Temporal.PlainDate, view: View, earliest: Temporal.PlainDate | null): boolean {
  if (!earliest) return false;
  if (view === "week") {
    return Temporal.PlainDate.compare(startOfWeekMonday(date), startOfWeekMonday(earliest)) > 0;
  }
  return compareYearMonth(date, earliest) > 0;
}

export function canGoForward(date: Temporal.PlainDate, view: View, today: Temporal.PlainDate): boolean {
  if (view === "week") {
    return Temporal.PlainDate.compare(startOfWeekMonday(date), startOfWeekMonday(today)) < 0;
  }
  return compareYearMonth(date, today) < 0;
}

export function startOfWeekMonday(d: Temporal.PlainDate): Temporal.PlainDate {
  return d.subtract({ days: d.dayOfWeek - 1 });
}

function compareYearMonth(a: Temporal.PlainDate, b: Temporal.PlainDate): number {
  return Temporal.PlainYearMonth.compare(a.toPlainYearMonth(), b.toPlainYearMonth());
}
