import type { Database } from "sql.js";

// A session's time, split by where it was played. A session recorded on one
// machine while that machine streamed to another was played on the other one
// for as long as the stream lasted; the rest of it was played where it ran.
// Remote Play Together is left out: a friend joining doesn't move you.
//
// Each piece takes a share of the session's active playtime in proportion to
// its length. The session's own start and end come along so filters on how
// long a session lasted still judge the whole session, not a piece of it.
export const PLAY_SEGMENTS_SCHEMA = `
  CREATE TABLE play_segments (
    played_on VARCHAR,
    ran_on VARCHAR,
    started_at VARCHAR,
    finished_at VARCHAR,
    steam_app_id INTEGER,
    active_playtime_seconds REAL,
    session_started_at VARCHAR,
    session_finished_at VARCHAR
  );
`;

interface Stream {
  remoteHost: string;
  start: number;
  end: number;
}

interface Piece {
  playedOn: string;
  start: number;
  end: number;
}

export function buildPlaySegments(db: Database) {
  db.exec(PLAY_SEGMENTS_SCHEMA);
  const streamsByHost = readStreams(db);

  const sessions =
    db.exec("SELECT host, started_at, finished_at, steam_app_id, active_playtime_seconds FROM gaming_sessions")[0]
      ?.values ?? [];

  const insert = db.prepare("INSERT INTO play_segments VALUES (?, ?, ?, ?, ?, ?, ?, ?)");
  db.exec("BEGIN");
  for (const [host, startedAt, finishedAt, steamAppId, active] of sessions) {
    const ranOn = host as string;
    const start = Temporal.Instant.from(startedAt as string).epochMilliseconds;
    const end = Temporal.Instant.from(finishedAt as string).epochMilliseconds;
    const wall = end - start;

    for (const piece of split(ranOn, start, end, streamsByHost.get(ranOn) ?? [])) {
      const share = wall > 0 ? (piece.end - piece.start) / wall : 1;
      insert.run([
        piece.playedOn,
        ranOn,
        Temporal.Instant.fromEpochMilliseconds(piece.start).toString(),
        Temporal.Instant.fromEpochMilliseconds(piece.end).toString(),
        steamAppId,
        active === null ? null : (active as number) * share,
        startedAt,
        finishedAt,
      ]);
    }
  }
  db.exec("COMMIT");
  insert.free();
}

function readStreams(db: Database): Map<string, Stream[]> {
  const rows =
    db.exec(`
      SELECT host, remote_host, started_at, finished_at
      FROM remote_play_sessions
      WHERE role = 'host' AND remote_host IS NOT NULL AND COALESCE(remote_play_together, 0) = 0
    `)[0]?.values ?? [];

  const byHost = new Map<string, Stream[]>();
  for (const [host, remoteHost, startedAt, finishedAt] of rows) {
    const streams = byHost.get(host as string) ?? [];
    streams.push({
      remoteHost: remoteHost as string,
      start: Temporal.Instant.from(startedAt as string).epochMilliseconds,
      end: Temporal.Instant.from(finishedAt as string).epochMilliseconds,
    });
    byHost.set(host as string, streams);
  }
  for (const streams of byHost.values()) {
    streams.sort((a, b) => a.start - b.start);
  }
  return byHost;
}

function split(ranOn: string, start: number, end: number, streams: Stream[]): Piece[] {
  const pieces: Piece[] = [];
  let cursor = start;
  for (const stream of streams) {
    const from = Math.max(stream.start, cursor);
    const to = Math.min(stream.end, end);
    if (to <= from) continue;
    if (from > cursor) pieces.push({ playedOn: ranOn, start: cursor, end: from });
    pieces.push({ playedOn: stream.remoteHost, start: from, end: to });
    cursor = to;
  }
  if (cursor < end || pieces.length === 0) pieces.push({ playedOn: ranOn, start: cursor, end });
  return absorbSlivers(pieces, ranOn);
}

// The daemon notices a game up to a poll interval (30s) late, so a session
// streamed from start to finish still overhangs its stream by a few seconds at
// each end. A local piece that short beside a stream is that lag, not time
// played at the host; it belongs to the stream it touches.
const SLIVER_MS = 2 * 60 * 1000;

function absorbSlivers(pieces: Piece[], ranOn: string): Piece[] {
  const kept: Piece[] = [];
  for (const [index, piece] of pieces.entries()) {
    const isSliver = piece.playedOn === ranOn && piece.end - piece.start < SLIVER_MS;
    const previous = kept[kept.length - 1];
    const next = pieces[index + 1];
    if (isSliver && previous && previous.playedOn !== ranOn) {
      previous.end = piece.end;
    } else if (isSliver && next && next.playedOn !== ranOn) {
      next.start = piece.start;
    } else {
      kept.push(piece);
    }
  }
  return kept;
}
