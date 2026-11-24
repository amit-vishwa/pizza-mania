# Search pagination regression

## Reproduction

Reviewed source: `03b1c44e94cde5fd4784b08d186cb1d719d10269` (the changes merged by PR #24). On November 22, 2026, an isolated Spring/H2 test exercised the real ProductManagementSearchRepository and shared GenericDao. Synthetic product rows were committed through JDBC; there was no outer test transaction, Docker, Cognito call or production database connection.

The original seven cases produced four passes, two assertion failures and one error:

- 15 rows, page 2, size 10: `InvalidDataAccessApiUsageException: first-result value cannot be negative : -1`.
- 15 rows, page 2147483647, size 10: overflow returned the first ten rows instead of an empty page.
- 15 rows, page 1073741824, size 4: overflow returned the first four rows instead of an empty page.
- First page, partial last page, exact-end page and an empty dataset passed their row/count checks.

These are repository/JPA observations, not an independently exercised HTTP response or MySQL result.

## Fix boundary

SearchCriteria computes the product in long arithmetic, retaining its existing -1 sentinel for an out-of-range or JPA-unrepresentable offset. GenericDao handles that sentinel and exact-end offsets by returning an empty list before creating the row query. It never passes the sentinel to JPA. The count query still provides totalRecords.

The page envelope now receives the requested zero-based page number rather than a row offset. `firstRecordOffset` uses long arithmetic/storage so large requested pages do not wrap to negative values or zero. The JSON fields and numeric shape are retained; the Java getter/setter type for firstRecordOffset widens from int to long. Java callers assigning the getter directly to int must adapt. This project previously mixed page numbers and row offsets in response metadata; correcting the values is intentional.

Existing controller normalization and the 100-record external page limit remain unchanged. Internal explicitly unpaged searches remain supported. The existing int totalRecords field is unchanged; this work does not add support for result counts beyond its range or JPA offsets beyond Integer.MAX_VALUE.

## Verification

With Java 17, from the repository root:

```bash
./mvnw --batch-mode --no-transfer-progress -Dtest=SearchPaginationIntegrationTest test
./mvnw --batch-mode --no-transfer-progress verify
```

The final eight-case test covers first, partial-last, beyond-last, exact-end and empty pages, two overflowing offsets, and a nonzero requested page on an empty dataset. It checks exact ordered product names, row totals, page size, currentPage, firstRecordOffset and totalPages. Existing UtilityTest checks remain responsible for API paging bounds.

H2 is the local regression database, not proof of MySQL execution or query-plan equivalence. Hosted MySQL schema/checkout checks remain separate existing CI jobs. Review-copy commands used installed Maven 3.10.0 with the existing dependency cache; no build or dependency versions were changed.
