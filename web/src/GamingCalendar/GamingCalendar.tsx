import { format, getDay, parse, startOfWeek } from "date-fns";
import { enUS } from "date-fns/locale";
import { useCallback, useEffect, useMemo, useReducer, useRef } from "react";
import { Calendar, type Event, type ToolbarProps, dateFnsLocalizer } from "react-big-calendar";
import "react-big-calendar/lib/css/react-big-calendar.css";
import "./GamingCalendar.css";

import { gameColor } from "../color-wheel";
import { useDatabase } from "../database";
import { getSessions } from "../services/sessions";
import type { Session } from "../types/playtime";
import { computeSessionPlaytime } from "../utils/playtime-helpers";
import {
  type Popup,
  type View,
  calendarReducer,
  canGoBack,
  canGoForward,
  initialCalendarState,
} from "./calendar-state";

const localizer = dateFnsLocalizer({
  format,
  parse,
  startOfWeek: (d: Date) => startOfWeek(d, { weekStartsOn: 1 }),
  getDay,
  locales: { "en-US": enUS },
});

// react-big-calendar requires legacy Date objects at its boundary
const toDate = (d: Temporal.PlainDate): Date => new Date(d.year, d.month - 1, d.day);

// Local fields, not toISOString(): that's UTC, a day early east of UTC
const fromDate = (d: Date): Temporal.PlainDate =>
  Temporal.PlainDate.from({ year: d.getFullYear(), month: d.getMonth() + 1, day: d.getDate() });

// Sessions of the same game separated by a short gap (e.g. quick relaunches,
// crashes) are folded into one calendar event so the week view doesn't turn
// into a wall of slivers. The gap becomes idle time in computeSessionPlaytime,
// same as idle time within a single session.
const MERGE_GAP_MINUTES = 10;

function mergeCloseSessions(sessions: Session[]): Session[] {
  const merged: Session[] = [];
  for (const session of sessions) {
    const prev = merged[merged.length - 1];
    const gapSeconds = prev
      ? prev.endTime.until(session.startTime, { largestUnit: "seconds" }).seconds
      : Number.POSITIVE_INFINITY;
    if (prev && prev.name === session.name && gapSeconds <= MERGE_GAP_MINUTES * 60) {
      const hosts = new Set([...prev.host.split(" / "), session.host]);
      merged[merged.length - 1] = {
        ...prev,
        endTime: session.endTime,
        activePlaytimeSeconds: prev.activePlaytimeSeconds + session.activePlaytimeSeconds,
        host: [...hosts].join(" / "),
      };
    } else {
      merged.push(session);
    }
  }
  return merged;
}

interface CustomToolbarProps extends ToolbarProps<SessionEvent> {
  canGoBack: boolean;
  canGoForward: boolean;
  onStep: (to: "PREV" | "NEXT" | "TODAY") => void;
}

function CustomToolbar({ view, onView, label, canGoBack, canGoForward, onStep }: CustomToolbarProps) {
  return (
    <div className="rbc-toolbar">
      <span className="rbc-btn-group">
        <button type="button" disabled={!canGoBack} onClick={() => onStep("PREV")}>
          Back
        </button>
        <button type="button" onClick={() => onStep("TODAY")}>
          Today
        </button>
        <button type="button" disabled={!canGoForward} onClick={() => onStep("NEXT")}>
          Next
        </button>
      </span>
      <span className="rbc-toolbar-label">{label}</span>
      <span className="rbc-btn-group">
        {(["week", "month", "agenda"] as View[]).map((v) => (
          <button key={v} type="button" className={view === v ? "rbc-active" : ""} onClick={() => onView(v)}>
            {v.charAt(0).toUpperCase() + v.slice(1)}
          </button>
        ))}
      </span>
    </div>
  );
}

// Fixed 8am reference for the calendar's min display hour
const HOUR_8AM = (() => {
  const d = toDate(Temporal.PlainDate.from("1970-01-01"));
  d.setHours(8, 0, 0, 0);
  return d;
})();

interface SessionEvent extends Event {
  gameName: string;
  activePlaytimeSeconds: number;
  startInstant: Temporal.Instant;
  endInstant: Temporal.Instant;
}

function EventLabel({ event }: { event: SessionEvent }) {
  return <span title={event.gameName}>{event.gameName}</span>;
}

function DayHeader({ date }: { date: Date }) {
  const d = fromDate(date);
  const longName = d.toLocaleString("en-US", { weekday: "long" });
  const shortName = d.toLocaleString("en-US", { weekday: "short" });
  return (
    <div>
      <div>
        <span className="day-name-long">{longName}</span>
        <span className="day-name-short">{shortName}</span>
      </div>
      <div style={{ opacity: 0.6, fontSize: "0.85em" }}>({d.day})</div>
    </div>
  );
}

type PopupState = Popup<SessionEvent>;

function formatDuration(totalMinutes: number): string {
  const h = Math.floor(totalMinutes / 60);
  const m = totalMinutes % 60;
  return h > 0 ? `${h}h${m > 0 ? ` ${m}m` : ""}` : `${m}m`;
}

function EventPopup({ popup, onClose }: { popup: PopupState; onClose: () => void }) {
  const ref = useRef<HTMLDivElement>(null);
  const { event, x, y } = popup;
  const start = event.startInstant.toZonedDateTimeISO(Temporal.Now.timeZoneId());
  const end = event.endInstant.toZonedDateTimeISO(Temporal.Now.timeZoneId());
  const { playtimeSeconds, idleSeconds } = computeSessionPlaytime(
    event.activePlaytimeSeconds,
    event.startInstant,
    event.endInstant,
  );
  const activeMinutes = Math.round(playtimeSeconds / 60);
  const idleMinutes = Math.round(idleSeconds / 60);

  const fmt = (zdt: Temporal.ZonedDateTime) => zdt.toPlainTime().toString({ smallestUnit: "minutes" });

  useEffect(() => {
    const closeOnOutsideClick = (e: MouseEvent) => {
      if (ref.current && !ref.current.contains(e.target as Node)) onClose();
    };
    const closeOnEscape = (e: KeyboardEvent) => {
      if (e.key === "Escape") onClose();
    };
    document.addEventListener("mousedown", closeOnOutsideClick);
    document.addEventListener("keydown", closeOnEscape);
    return () => {
      document.removeEventListener("mousedown", closeOnOutsideClick);
      document.removeEventListener("keydown", closeOnEscape);
    };
  }, [onClose]);

  const { background, text } = gameColor(event.gameName);

  return (
    <div ref={ref} className="event-popup" style={{ left: x, top: y }}>
      <div className="event-popup-header" style={{ backgroundColor: background, color: text }}>
        {event.gameName}
      </div>
      <div className="event-popup-body">
        <div>
          {fmt(start)} – {fmt(end)}
        </div>
        {idleMinutes > 2 ? (
          <>
            <div>Active: {formatDuration(activeMinutes)}</div>
            <div className="event-popup-idle">Idle/sleep: {formatDuration(idleMinutes)}</div>
          </>
        ) : (
          <div>{formatDuration(activeMinutes)}</div>
        )}
        <div className="event-popup-host">{event.resource as string}</div>
      </div>
    </div>
  );
}

export default function GamingCalendar() {
  const db = useDatabase();
  const [{ date, view, popup }, dispatch] = useReducer(
    calendarReducer<SessionEvent>,
    Temporal.Now.plainDateISO(),
    initialCalendarState<SessionEvent>,
  );

  const events = useMemo<SessionEvent[]>(() => {
    return mergeCloseSessions(getSessions(db)).map((session) => ({
      title: session.name,
      gameName: session.name,
      start: new Date(session.startTime.epochMilliseconds),
      end: new Date(session.endTime.epochMilliseconds),
      startInstant: session.startTime,
      endInstant: session.endTime,
      resource: session.host,
      activePlaytimeSeconds: session.activePlaytimeSeconds,
    }));
  }, [db]);

  const earliestDate = useMemo<Temporal.PlainDate | null>(() => {
    if (events.length === 0) return null;
    return events.reduce(
      (min, ev) => {
        const d = fromDate(ev.start as Date);
        return Temporal.PlainDate.compare(d, min) < 0 ? d : min;
      },
      fromDate(events[0].start as Date),
    );
  }, [events]);

  const onStep = useCallback(
    (to: "PREV" | "NEXT" | "TODAY") =>
      dispatch({ type: "navigate", to, earliest: earliestDate, today: Temporal.Now.plainDateISO() }),
    [earliestDate],
  );

  useEffect(() => {
    const handler = (e: KeyboardEvent) => {
      if (e.target instanceof HTMLInputElement || e.target instanceof HTMLTextAreaElement) return;
      switch (e.key) {
        case "ArrowLeft":
          return onStep("PREV");
        case "ArrowRight":
          return onStep("NEXT");
        case "t":
          return onStep("TODAY");
        case "a":
          return dispatch({ type: "setView", view: "agenda" });
        case "m":
          return dispatch({ type: "setView", view: "month" });
        case "w":
          return dispatch({ type: "setView", view: "week" });
      }
    };
    window.addEventListener("keydown", handler);
    return () => window.removeEventListener("keydown", handler);
  }, [onStep]);

  const closePopup = useCallback(() => dispatch({ type: "closePopup" }), []);

  const eventStyleGetter = (event: SessionEvent) => {
    const { background, text } = gameColor(event.gameName);
    const { playtimeSeconds, idleSeconds, wallClockSeconds } = computeSessionPlaytime(
      event.activePlaytimeSeconds,
      event.startInstant,
      event.endInstant,
    );
    const hasIdle = idleSeconds > 120;
    const activePct = hasIdle ? `${((playtimeSeconds / wallClockSeconds) * 100).toFixed(1)}%` : null;
    return {
      className: hasIdle ? "has-idle" : undefined,
      style: {
        backgroundColor: background,
        color: text,
        border: "none",
        borderRadius: "3px",
        ...(activePct !== null ? ({ "--active-pct": activePct } as React.CSSProperties) : {}),
      },
    };
  };

  return (
    <div className="calendar-container">
      <Calendar
        localizer={localizer}
        events={events}
        date={toDate(date)}
        view={view}
        onNavigate={(d) => dispatch({ type: "goTo", date: fromDate(d) })}
        onView={(v) => dispatch({ type: "setView", view: v as View })}
        views={["week", "month", "agenda"]}
        components={{
          toolbar: (props: ToolbarProps<SessionEvent>) => (
            <CustomToolbar
              {...props}
              canGoBack={canGoBack(date, view, earliestDate)}
              canGoForward={canGoForward(date, view, Temporal.Now.plainDateISO())}
              onStep={onStep}
            />
          ),
          event: EventLabel,
          header: DayHeader,
        }}
        eventPropGetter={eventStyleGetter}
        onSelectEvent={(event, e) => {
          const rect = (e.target as HTMLElement).closest(".rbc-event")?.getBoundingClientRect();
          const x = rect ? rect.left : (e.nativeEvent as MouseEvent).clientX;
          const y = rect ? rect.top - 8 : (e.nativeEvent as MouseEvent).clientY;
          dispatch({ type: "openPopup", popup: { event: event as SessionEvent, x, y } });
        }}
        formats={{
          dayRangeHeaderFormat: ({ start, end }, culture) =>
            `${localizer.format(start, "MMMM d", culture)} – ${localizer.format(end, "MMMM d, yyyy", culture)}`,
          monthHeaderFormat: "MMMM yyyy",
        }}
        min={HOUR_8AM}
        popup
      />
      {popup && <EventPopup popup={popup} onClose={closePopup} />}
    </div>
  );
}
