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
        }
        properties.add("spring.datasource.jdbc-url", () -> url);
        properties.add("spring.datasource.username", () -> user);
        properties.add("spring.datasource.password", () -> password);
        properties.add("spring.datasource.driver-class-name", () -> "com.mysql.cj.jdbc.Driver");
        // Validate the maintained schema; never let Hibernate create/drop or upgrade it.
        properties.add("spring.jpa.hibernate.ddl-auto", () -> "validate");
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
