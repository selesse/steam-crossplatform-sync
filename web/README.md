# Playtime dashboard

A dashboard for the playtime history steam-crossplatform-sync records:
playtime by game, machine, month and year, plus a calendar of every session.
It runs entirely in the browser, on [sql.js][sql.js] (SQLite compiled to
WebAssembly), so there's no server to run beyond something to serve static
files.

[sql.js]: https://sql.js.org

## Running it

You'll need [bun][bun].

```
bun install
bun run use-databases ~/.local/share/steam-crossplatform-sync/steam-logs.sqlite3
bun run dev
```

`use-databases` takes any number of databases, one per machine, as the daemon
writes them. It copies them into `public/` under content-hashed names and
lists them in `public/db-manifest.json`. Run it again whenever you want newer
data.

To host it, build the bundle, then point it at your databases:

```
bun run build
bun run use-databases --out dist path/to/*/steam-logs.sqlite3
```

and serve `dist/` from the root of a domain.

[bun]: https://bun.sh

## How it reads the data

On load, [`src/database.ts`](src/database.ts) fetches every database in
`db-manifest.json` and copies their `games` and `gaming_sessions` rows into one
in-memory database. A session that appears in more than one database counts
once, and a game takes its name from the last database that has one. It then
creates the views in [`src/views.sql`](src/views.sql), which most charts query.

## Conventions

- Use bun, not npm, to install packages and run scripts.
- `bun run lint` checks linting, formatting and import order with
  [Biome][biome]; `bun run fix` applies what it can. CI runs it along with
  `bun run build`.
- Use the [Temporal API][temporal] instead of `Date`. The polyfill
  (`temporal-polyfill/global`) is imported in `main.tsx`, so `Temporal` is
  available everywhere without an import.
  - Current date/time: `Temporal.Now.plainDateISO()`, `Temporal.Now.instant()`
  - Timestamps from the database (ISO 8601 strings): `Temporal.Instant.from(str)`
  - `new Date(...)` only where a library requires it, such as
    react-big-calendar's event `start`/`end` and date-fns callbacks.

[biome]: https://biomejs.dev
[temporal]: https://tc39.es/proposal-temporal/
