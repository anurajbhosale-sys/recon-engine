---
name: handoff
description: End-of-session wrap-up that saves state so the next session can start fresh after /clear. Use whenever the user runs /handoff, says "wrap up", "end session", "I'm done for today", or before clearing context.
---

# Session handoff

1. Run the test suite (or the tests for touched modules) and report pass/fail honestly.
2. Rewrite `docs/progress.md` completely (do not append): current phase, last completed slice,
   exact next step, open questions, known issues. Keep it under 25 lines.
3. List any decisions made this session that deserve an ADR and aren't written yet.
4. Suggest a commit message (conventional commits) for uncommitted work. Do not push.
5. Remind Anuraj to add a `docs/learning-log.md` entry in his own words.
6. Tell him it's safe to run `/clear`.
