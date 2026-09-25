---
name: incident-drill
description: Run a realistic production-incident debugging exercise. Use when the user runs /incident-drill or asks for a debugging drill or incident practice. Phase 3 or later.
---

# Incident drill

1. Create a branch `drill/<short-name>`. Never do this on main.
2. Pick ONE realistic fault appropriate to what exists so far: connection pool exhaustion,
   N+1 query, missing index, deadlock, race condition causing double-match, slow Kafka consumer
   / lag, cache stampede, memory leak, auth bypass. Inject it subtly. Do NOT tell Anuraj what it is.
3. Describe only the symptoms, like a pager alert or a user complaint.
4. Let him investigate. Answer questions as the "system" would (logs, metrics, plans), but do
   not hint unless he asks. Hint ladder: signal to look at → subsystem → mechanism.
5. After he finds and fixes it, debrief: root cause, the fastest investigation path, which
   monitoring signal would have caught it earlier, and how to prevent it (test, alert, guard).
6. Remind him to write a short postmortem in `docs/learning-log.md` himself.
