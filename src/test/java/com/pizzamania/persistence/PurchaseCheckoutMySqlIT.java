package com.pizzamania.persistence;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class PurchaseCheckoutMySqlIT extends AbstractCheckoutContract {
    @DynamicPropertySource
    static void initializeOwnedMySqlSchema(DynamicPropertyRegistry properties) throws Exception {
        String url = requiredEnvironment("PIZZA_MYSQL_TEST_URL");
        assertThat(url).matches("jdbc:mysql://(127\\.0\\.0\\.1|localhost):[0-9]+/pizza_mania_it(\\?.*)?");
        String user = requiredEnvironment("PIZZA_MYSQL_TEST_USER");
        String password = requiredEnvironment("PIZZA_MYSQL_TEST_PASSWORD");
        try (Connection connection = DriverManager.getConnection(url, user, password)) {
            assertThat(value(connection, "SELECT DATABASE()")).isEqualTo("pizza_mania_it");
            assertThat(value(connection, "SELECT COUNT(*) FROM information_schema.tables "
                    + "WHERE table_schema = DATABASE()")).isEqualTo("0");
            String mode = value(connection, "SELECT @@SESSION.sql_mode");
            assertThat(mode.contains("STRICT_TRANS_TABLES") || mode.contains("STRICT_ALL_TABLES")).isTrue();
            System.out.printf("MYSQL_CHECKOUT_CONTRACT server=%s sql_mode=%s%n",
                    value(connection, "SELECT VERSION()"), mode);
            ScriptUtils.executeSqlScript(connection, new ClassPathResource("documents/Table DDLs.txt"));
            assertMoneyColumn(connection, "product", "product_cost", 5);
            assertMoneyColumn(connection, "purchase_detail", "product_cost", 5);
            assertMoneyColumn(connection, "purchase_detail", "total_product_cost", 10);
            assertMoneyColumn(connection, "purchase", "purchase_amount", 10);
        }
        properties.add("spring.datasource.jdbc-url", () -> url);
        properties.add("spring.datasource.username", () -> user);
        properties.add("spring.datasource.password", () -> password);
        properties.add("spring.datasource.driver-class-name", () -> "com.mysql.cj.jdbc.Driver");
        // Legacy CHAR status mappings differ from Hibernate's VARCHAR defaults.
        // Check money metadata explicitly above; exercise the maintained schema unchanged.
        properties.add("spring.jpa.hibernate.ddl-auto", () -> "none");
    }

    private static void assertMoneyColumn(Connection connection, String table, String column,
            int precision) throws Exception {
        try (var query = connection.prepareStatement("SELECT data_type, numeric_precision, numeric_scale, "
                + "is_nullable FROM information_schema.columns WHERE table_schema = DATABASE() "
                + "AND table_name = ? AND column_name = ?")) {
            query.setString(1, table);
            query.setString(2, column);
            try (ResultSet metadata = query.executeQuery()) {
                assertThat(metadata.next()).as(table + "." + column).isTrue();
                assertThat(metadata.getString(1)).isEqualTo("decimal");
                assertThat(metadata.getInt(2)).isEqualTo(precision);
                assertThat(metadata.getInt(3)).isEqualTo(2);
                assertThat(metadata.getString(4)).isEqualTo("NO");
            }
        }
    }

    private static String value(Connection connection, String query) throws Exception {
        try (Statement statement = connection.createStatement(); ResultSet rows = statement.executeQuery(query)) {
            assertThat(rows.next()).isTrue();
            return rows.getString(1);
        }
    }

    private static String requiredEnvironment(String name) {
        String value = System.getenv(name);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException(name + " is required; use the hosted disposable MySQL job");
        }
        return value;
    }
}
