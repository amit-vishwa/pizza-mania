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
exception transaction. Unit mocks do not prove that rollback; the application contracts below verify
the real Spring transaction using independent database reads.

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
The GitHub-hosted MySQL 8.4 matrix runs each contract in a separate job/service:
`mvn --batch-mode --no-transfer-progress -Pmysql-it -Dmysql.contract=PurchaseSchemaMySqlIT clean verify`
and the same command with `PurchaseCheckoutMySqlIT`. Each job owns a fresh database;
the two fixtures must not run against the same populated schema.
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

## Application checkout and rollback contracts

`PurchaseCheckoutTransactionTest` runs six shared service-level cases on a dedicated
H2 database in the default suite. `PurchaseCheckoutMySqlIT` runs the same cases
against the maintained DDL on hosted MySQL, with Hibernate set to validate only.
Select it locally only with an approved empty disposable database:
`./mvnw -Pmysql-it -Dmysql.contract=PurchaseCheckoutMySqlIT verify`.
The default `mysql-it` selection remains the six JDBC schema/migration cases.

The application cases cover authoritative database pricing for 3 x 500.00, exact
0.10/0.20 arithmetic, the exact 99,999,999.99 checkout maximum, line overflow,
aggregate overflow after inventory updates, and a forced failure after stock,
header, cart and line writes. There is no outer test transaction. Success is
verified after the service returns; rollback is verified after it throws, through
new JDBC reads. All checkout services, repositories, mappers and user lookup are
real. A mapper spy injects the late exception only after assertions see actual
SQL writes inside the service transaction. JWT decoding is mocked to avoid
external authentication; these are service contracts, not HTTP/Cognito tests.

The fixture first refuses non-loopback or nonempty MySQL schemas before creating
tables. Row cleanup is limited to that owned fixture; CI destroys the service.
Local H2 success does not establish MySQL success: review the hosted matrix
results at the submitted commit before merging or closing issue #18.
