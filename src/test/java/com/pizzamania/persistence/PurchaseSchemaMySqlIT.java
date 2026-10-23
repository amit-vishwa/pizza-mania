package com.pizzamania.persistence;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ScriptUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

/** JDBC schema contract only: no Spring application, Cognito or application checkout. */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class PurchaseSchemaMySqlIT {

    private Connection database;

    @BeforeAll
    void initializeEmptyDisposableSchema() throws Exception {
        String url = requiredEnvironment("PIZZA_MYSQL_TEST_URL");
        assertThat(url).matches("jdbc:mysql://(127\\.0\\.0\\.1|localhost):[0-9]+/pizza_mania_it(\\?.*)?");
        database = DriverManager.getConnection(url,
                requiredEnvironment("PIZZA_MYSQL_TEST_USER"),
                requiredEnvironment("PIZZA_MYSQL_TEST_PASSWORD"));
        assertThat(value("SELECT DATABASE()")).isEqualTo("pizza_mania_it");
        assertThat(value("SELECT COUNT(*) FROM information_schema.tables "
                + "WHERE table_schema = DATABASE()")).isEqualTo("0");
        String mode = value("SELECT @@SESSION.sql_mode");
        assertThat(mode.contains("STRICT_TRANS_TABLES") || mode.contains("STRICT_ALL_TABLES"))
                .as("Baseline overflow reproduction requires strict SQL mode").isTrue();
        System.out.printf("MYSQL_SCHEMA_CONTRACT server=%s sql_mode=%s%n",
                value("SELECT VERSION()"), mode);
        ScriptUtils.executeSqlScript(database,
                new ClassPathResource("documents/Table DDLs.txt"));
    }

    @BeforeEach
    void clearOwnedFixtureRows() throws Exception {
        // BeforeAll requires an empty dedicated schema; never point this at application data.
        try (Statement statement = database.createStatement()) {
            statement.executeUpdate("DELETE FROM cart");
            statement.executeUpdate("DELETE FROM purchase_detail");
            statement.executeUpdate("DELETE FROM purchase");
            statement.executeUpdate("DELETE FROM product");
        }
    }

    @AfterAll
    void closeConnection() throws Exception {
        if (database != null) {
            database.close();
        }
    }

    @Test
    void repositorySchema_shouldExposeCurrentLineTotalCapacity() throws Exception {
        try (Statement statement = database.createStatement();
                ResultSet column = statement.executeQuery(
                        "SELECT numeric_precision, numeric_scale FROM information_schema.columns "
                        + "WHERE table_schema = DATABASE() AND table_name = 'purchase_detail' "
                        + "AND column_name = 'total_product_cost'")) {
            assertThat(column.next()).isTrue();
            assertThat(column.getInt(1)).isEqualTo(10);
            assertThat(column.getInt(2)).isEqualTo(2);
        }
    }

    @Test
    void ordinaryLineTotal_shouldRoundTripExactly() throws Exception {
        seedProductAndPurchase();
        insertLine(1, new BigDecimal("500.00"));
        assertThat(new BigDecimal(value("SELECT total_product_cost FROM purchase_detail")))
                .isEqualByComparingTo("500.00");
        assertThat(value("SELECT COUNT(*) FROM purchase_detail")).isEqualTo("1");
    }

    @Test
    void total1500_shouldRoundTripExactly() throws Exception {
        seedProductAndPurchase();
        insertLine(3, new BigDecimal("500.00").multiply(BigDecimal.valueOf(3)));
        assertThat(new BigDecimal(value("SELECT total_product_cost FROM purchase_detail")))
                .isEqualByComparingTo("1500.00");
        assertThat(value("SELECT COUNT(*) FROM purchase_detail")).isEqualTo("1");
    }

    @Test
    void maximumStoredTotal_shouldRoundTripExactly() throws Exception {
        seedProductAndPurchase();
        insertLine(1, new BigDecimal("99999999.99"));
        assertThat(new BigDecimal(value("SELECT total_product_cost FROM purchase_detail")))
                .isEqualByComparingTo("99999999.99");
    }

    @Test
    void aboveStorageMaximum_shouldRejectWithoutInsertion() throws Exception {
        seedProductAndPurchase();
        SQLException failure = assertThrows(SQLException.class,
                () -> insertLine(200000, new BigDecimal("100000000.00")));
        assertThat(failure.getErrorCode()).isEqualTo(1264);
        assertThat(failure.getSQLState()).isEqualTo("22001");
        assertThat(value("SELECT COUNT(*) FROM purchase_detail")).isEqualTo("0");
    }

    @Test
    void upgrade_shouldPreserveOldValuesAndAllowLargerTotals() throws Exception {
        seedProductAndPurchase();
        try (Statement statement = database.createStatement()) {
            statement.execute("ALTER TABLE purchase_detail MODIFY COLUMN "
                    + "total_product_cost DECIMAL(5,2) NOT NULL");
        }
        try {
            insertLine(1, new BigDecimal("500.00"));
            ScriptUtils.executeSqlScript(database, new ClassPathResource(
                    "documents/upgrades/001-widen-purchase-line-total.sql"));
            assertThat(new BigDecimal(value("SELECT total_product_cost FROM purchase_detail")))
                    .isEqualByComparingTo("500.00");
            insertLine(3, new BigDecimal("1500.00"));
            assertThat(value("SELECT COUNT(*) FROM purchase_detail")).isEqualTo("2");
            assertThat(new BigDecimal(value("SELECT SUM(total_product_cost) FROM purchase_detail")))
                    .isEqualByComparingTo("2000.00");
        } finally {
            // Keep unrelated cases isolated if a migration assertion fails.
            try (Statement statement = database.createStatement()) {
                statement.execute("ALTER TABLE purchase_detail MODIFY COLUMN "
                        + "total_product_cost DECIMAL(10,2) NOT NULL");
            }
        }
    }

    private void seedProductAndPurchase() throws Exception {
        try (Statement statement = database.createStatement()) {
            statement.executeUpdate("INSERT INTO product "
                    + "(product_id, product_name, product_description, product_cost, "
                    + "product_quantity, product_available, created_by_user, created_by_process) "
                    + "VALUES (1, 'Fixture pizza', 'Synthetic fixture', 500.00, 10, 1, 'test', 'test')");
            statement.executeUpdate("INSERT INTO purchase "
                    + "(purchase_id, purchase_items, purchase_amount, status, status_timestamp, "
                    + "created_by_user, created_by_process) "
                    + "VALUES (1, 1, 1500.00, 'PENDING', CURRENT_TIMESTAMP, 'test', 'test')");
        }
    }

    private void insertLine(int quantity, BigDecimal total) throws SQLException {
        try (PreparedStatement statement = database.prepareStatement(
                "INSERT INTO purchase_detail (purchase_id, product_id, product_quantity, "
                + "product_cost, total_product_cost, status, status_timestamp, created_by_user, "
                + "created_by_process) VALUES (1, 1, ?, 500.00, ?, 'PENDING', "
                + "CURRENT_TIMESTAMP, 'test', 'test')")) {
            statement.setInt(1, quantity);
            statement.setBigDecimal(2, total);
            statement.executeUpdate();
        }
    }

    private String value(String query) throws SQLException {
        try (Statement statement = database.createStatement();
                ResultSet rows = statement.executeQuery(query)) {
            assertThat(rows.next()).isTrue();
            return rows.getString(1);
        }
    }

    private String requiredEnvironment(String name) {
        String value = System.getenv(name);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException(name + " is required; run the hosted MySQL schema job");
        }
        return value;
    }
}
