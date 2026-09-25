# Setup & session rhythm

## One-time setup
1. `git init`, copy this scaffold in, commit it.
2. Install `jq` (the hooks need it).
3. Copy `.env.example` → `.env` and export `RECON_DB_READONLY_URL` in your shell. The read-only
   DB role gets created in Phase 1; until then the postgres MCP just won't connect — that's fine.
4. MCP packages move fast. Before first use, confirm `@modelcontextprotocol/server-postgres`
   and `@upstash/context7-mcp` are still maintained; swap in a maintained read-only Postgres
   MCP if needed. Run `/mcp` to confirm they connect.
5. Move `src/main/java/com/recon/ledger/CLAUDE.md` into your real ledger package once it exists.
6. Run `/context` once to see what CLAUDE.md + MCPs cost at session start.
7. Ask Claude Code to sanity-check `.claude/settings.json` against the current hooks docs,
   and read the two hook scripts yourself so you know what they do.

## Every session
1. Open Claude Code → plan mode → `/plan-feature`
2. Approve the plan → build (Learning output style for new material, default for boilerplate)
3. Ask the `code-reviewer` agent to review → fix blockers
4. `/teach-back` → write your learning-log entry yourself
5. Commit → `/handoff` → `/clear`

## Every phase
`/phase-wrapup` → do the No-AI rebuild → next phase.

## Weekly from Phase 3
`/incident-drill` (always on a drill/ branch).

## Token hygiene
- One slice per session, then `/clear`. `docs/progress.md` carries the state.
- `/compact` only when a slice runs long.
- Send big logs, test output, and query plans to a subagent.
- Stronger model for design and concurrency; cheaper model for boilerplate (`/model`).
- Don't add MCP servers "just in case" — each one costs context every session.

## Kickoff prompt for session 1
Read CLAUDE.md and docs/progress.md, then run /plan-feature for the Phase 1 domain model:
ledger entry, journal entry, settlement line, processor event, and how they relate.
Analogy first, then the Flyway schema with one alternative and the tradeoff. Also help me
decide Gradle vs Maven. Don't write code yet.
