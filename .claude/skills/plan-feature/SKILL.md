---
name: plan-feature
description: Plan a feature before any code is written. Use at the start of every new slice or feature, whenever the user runs /plan-feature, or says "let's build X", "next feature", or "start phase N".
---

# Plan a feature

Do not write or edit code during this skill. Produce the plan, then stop and wait.

1. Read `docs/progress.md` and the current phase in `docs/roadmap.md`.
2. **Analogy first:** explain the feature in plain English with a real-world analogy.
3. **Problem & requirements:** functional requirements, and which CLAUDE.md invariants apply.
4. **Design:** data model changes (tables, constraints, indexes), API shape, transaction
   boundaries, module(s) touched.
5. **One real alternative** and the tradeoff between them.
6. **Scaling check:** at 100 / 10k / 1M transactions per day — what breaks first?
7. **No-AI note:** how a professional engineer would research this without AI (which docs,
   which source classes, which experiments).
8. **Learning split:** list which parts are familiar patterns (Claude writes) vs new material
   (Anuraj predicts/proposes first or writes the `TODO(human)` parts).
9. **Slice size:** if this is more than one session of work, split it and propose slice 1 only.
10. If the decision is significant, propose an ADR title.

End with: "Approve, change, or ask questions?"
