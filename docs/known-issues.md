# Known issues (from code review, Day 6)

## Next up
- H1: same idempotency key with a different request is accepted silently

## Planned after MVP
- No authentication: tenant comes from the URL (needs login tokens)
- Database doesn't enforce append-only or balancing (needs triggers)
- Repositories still have delete methods inherited from JpaRepository
- Tenant filtering happens in Java, not in the SQL queries
- Currency codes aren't checked against real ISO codes
- No size limits on idempotency key, description, or number of lines