# MySQL schema contracts and purchase limits

Computed line totals and purchase amounts support nonnegative values through
99,999,999.99, with at most two significant decimal places. No rounding or clipping
is performed to make an unsupported amount fit. The existing purchase-create
rule still requires a positive header amount. Unit-price capacity remains 999.99.

purchase_detail.total_product_cost now uses DECIMAL(10,2), matching the purchase
header amount and its JPA mapping. Each computed line is checked before its stock
update. The aggregate is checked before header creation; direct purchase-header
creation enforces the same limit. Over-limit or fractional-cent amounts raise
IllegalArgumentException, which the existing API handler maps to bad request.
Earlier stock writes in a multi-line checkout rely on its existing rollback-on-
exception transaction. Unit mocks do not prove that rollback; application-level
database verification is follow-up work before issue #18 is closed.

## Fresh schema and upgrades

For a fresh empty database, initialize with `src/main/resources/documents/Table DDLs.txt`.
For an existing database, apply `src/main/resources/documents/upgrades/001-widen-purchase-line-total.sql`
to the intended schema before deploying the updated application. The script only
widens the line-total column; it does not delete or recalculate amounts. Plan the
normal backup and DDL deployment window for your environment. No automatic
migration framework or application-startup migration is introduced.

The hosted MySQL contracts exercise both fresh initialization and an old-width
table containing 500.00, then apply the exact upgrade script and verify that the
old value survives and 1500.00 becomes storable. They also test exact maximum
storage and rejection above it. The JDBC fixtures intentionally isolate column
storage; synthetic line/header values are not a checkout calculation test.

## Execution

Run `./mvnw --batch-mode --no-transfer-progress test` locally for the H2/unit suites.
The GitHub-hosted MySQL 8.4 service runs
`mvn --batch-mode --no-transfer-progress -Pmysql-it clean verify`.
Missing MySQL settings fail explicitly rather than silently skipping tests.
No local Docker/WSL, external delivery or Cognito account is required.

For an already approved disposable local MySQL instance, create an empty
`pizza_mania_it` database and set `PIZZA_MYSQL_TEST_URL`, `PIZZA_MYSQL_TEST_USER`
and `PIZZA_MYSQL_TEST_PASSWORD`, then run `./mvnw -Pmysql-it verify`.
The fixture accepts only loopback URLs and refuses a nonempty database before
loading schema or deleting fixture rows. Recreate the disposable schema for a
repeat run. CI removes its service when the job ends. Public workflow passwords
are temporary fixture values and must never be reused as application credentials.

PR #19 records the historical DECIMAL(5,2) overflow reproduction. The current
contracts intentionally replace its 1500.00 rejection assertion with acceptance.
First hosted execution of this change is required before claiming MySQL results.
