# MySQL schema baseline

The MySQL Schema Baseline workflow creates a disposable MySQL 8.4 service on a
GitHub-hosted Ubuntu runner. It loads the checked-in `Table DDLs.txt` into an empty
`pizza_mania_it` database, records server version and SQL mode, then runs three
JDBC schema contracts. The existing default suites also run.

The contracts document the current DECIMAL(5,2) capacity, an exact 500.00
round-trip, and rejection of 3 x 500.00 in strict mode with no inserted line.
The deliberate SQL exception is asserted, so the baseline job should pass.
Its first hosted result is required before claiming MySQL reproduction.

This is infrastructure and baseline evidence for issue #18, not the capacity
fix. It does not exercise Spring services, HTTP, authentication, inventory
updates, or checkout rollback. The synthetic parent purchase is committed during
fixture setup; a failed line insert does not prove application transaction
atomicity. Later work must test those guarantees through the application and
replace the baseline overflow expectation when the capacity fix is introduced.

## Local work without Docker

Run `./mvnw --batch-mode --no-transfer-progress test` for the default H2/unit suites.
The MySQL class ends in `IT` and is executed only by the `mysql-it` Failsafe profile
during `verify`; missing database configuration fails explicitly, rather than
silently skipping the tests. No local Docker/WSL or Cognito account is needed.

For a developer who already has an approved disposable local MySQL instance:
create an empty `pizza_mania_it` database, set `PIZZA_MYSQL_TEST_URL`,
`PIZZA_MYSQL_TEST_USER`, and `PIZZA_MYSQL_TEST_PASSWORD`, then run
`./mvnw --batch-mode --no-transfer-progress -Pmysql-it verify`.
The fixture accepts only loopback URLs and refuses a nonempty database before
loading schema or deleting fixture rows. Never use application data. For a
repeat run, recreate the disposable database first. CI removes its service when
the job ends; developers own teardown of their approved local fixture.

Workflow credentials are public fixture values for the temporary isolated
service. They are not application credentials and must never be reused elsewhere.
The DDL statement terminator after the log table is corrected so the complete
repository schema can load. No monetary column definition changes in this PR.
