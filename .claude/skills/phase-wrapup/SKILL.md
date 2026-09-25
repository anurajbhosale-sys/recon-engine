---
name: phase-wrapup
description: Close out a roadmap phase with interview materials. Use when a phase's "done when" criteria are met, or when the user runs /phase-wrapup or says "phase N is done".
---

# Phase wrap-up

1. Verify the phase's "Done when" criteria in `docs/roadmap.md`; show evidence (tests, output).
   If not met, say so and stop.
2. Check every significant decision this phase has an ADR.
3. Append to `docs/interview-prep.md` under a heading for this phase:
   - 2–3 resume bullets (action + technical specifics + measurable result; no fluff)
   - 30-second recruiter talking point
   - 6 interview questions, each with 1 follow-up
   - 1 system design question this phase prepares him for
   - Common mistakes candidates make on these topics
   - 1 senior-level discussion topic
4. Remind him to do the phase's "No-AI rebuild" from the roadmap before starting the next phase.
5. Update `docs/progress.md` to the next phase.
