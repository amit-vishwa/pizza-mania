package com.pizzamania.persistence;

import java.math.BigDecimal;
import java.util.List;

import jakarta.persistence.EntityManager;

import com.pizzamania.dto.PurchaseDetailDto;
import com.pizzamania.mapper.PurchaseDetailMapper;
import com.pizzamania.service.PurchaseDetailManagementService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;

/** Real Spring services/repositories; callers and verification have no outer transaction. */
@WithMockUser(username = "checkout-fixture")
abstract class AbstractCheckoutContract {
    @Autowired PurchaseDetailManagementService checkout;
    @Autowired JdbcTemplate jdbc;
    @Autowired EntityManager entityManager;
    @MockitoBean JwtDecoder jwtDecoder;
    @MockitoSpyBean PurchaseDetailMapper lineMapper;

    private int userId;

    @BeforeEach
    void resetOwnedFixture() {
        assertThat(TransactionSynchronizationManager.isActualTransactionActive()).isFalse();
        for (String table : List.of("cart", "purchase_detail", "purchase", "product", "user", "role")) {
            jdbc.update("DELETE FROM `" + table + "`");
        }
        jdbc.update("INSERT INTO `role` (role_name, record_status, created_on_timestamp, "
                + "created_by_user, created_by_process) VALUES ('ROLE_USER', 'A', CURRENT_TIMESTAMP, 'test', 'test')");
        int roleId = jdbc.queryForObject("SELECT role_id FROM `role`", Integer.class);
        jdbc.update("INSERT INTO `user` (user_name, user_pass, user_type, role_id, record_status, "
                + "created_on_timestamp, created_by_user, created_by_process) "
                + "VALUES ('checkout-fixture', 'unused-fixture-password', 'internal', ?, 'A', "
                + "CURRENT_TIMESTAMP, 'test', 'test')", roleId);
        userId = jdbc.queryForObject("SELECT user_id FROM `user`", Integer.class);
    }

    @Test
    void checkout1500CommitsAuthoritativePriceInventoryHeaderAndCart() throws Exception {
        int product = product("500.00", 10);
        PurchaseDetailDto request = item(product, 3);
        request.setProductCost(new BigDecimal("0.01"));
        request.setTotalProductCost(new BigDecimal("0.03"));

        List<PurchaseDetailDto> response = checkout.addPurchaseDetails(List.of(request));

        assertThat(response).hasSize(1);
        assertThat(response.get(0).getPurchaseId()).isNotNull();
        assertThat(response.get(0).getTotalProductCost()).isEqualByComparingTo("1500.00");
        assertPurchaseRows("1500.00", 1);
        assertThat(amount("SELECT product_cost FROM purchase_detail")).isEqualByComparingTo("500.00");
        assertThat(stock(product)).isEqualTo(7);
        assertThat(jdbc.queryForObject("SELECT purchase_id FROM cart", Integer.class))
                .isEqualTo(response.get(0).getPurchaseId());
    }

    @Test
    void checkoutRetainsExactFractionalAmountsAcrossLines() throws Exception {
        int first = product("0.10", 10);
        int second = product("0.20", 10);

        checkout.addPurchaseDetails(List.of(item(first, 3), item(second, 1)));

        assertPurchaseRows("0.50", 2);
        assertThat(stock(first)).isEqualTo(7);
        assertThat(stock(second)).isEqualTo(9);
    }

    @Test
    void exactMaximumCheckoutCommitsWithoutClipping() throws Exception {
        int product = product("999.99", 100002);

        checkout.addPurchaseDetails(List.of(item(product, 100001)));

        assertPurchaseRows("99999999.99", 1);
        assertThat(stock(product)).isEqualTo(1);
    }

    @Test
    void excessiveLineLeavesNoPurchaseAndPreservesStock() {
        int product = product("500.00", 200001);

        assertThatThrownBy(() -> checkout.addPurchaseDetails(List.of(item(product, 200000))))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("99999999.99");

        assertNoPurchase();
        assertThat(stock(product)).isEqualTo(200001);
    }

    @Test
    void aggregateOverflowRollsBackEarlierInventoryUpdates() {
        int first = product("500.00", 100001);
        int second = product("500.00", 100001);

        assertThatThrownBy(() -> checkout.addPurchaseDetails(
                List.of(item(first, 100000), item(second, 100000))))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("99999999.99");

        assertNoPurchase();
        assertThat(stock(first)).isEqualTo(100001);
        assertThat(stock(second)).isEqualTo(100001);
    }

    @Test
    void failureAfterDatabaseWritesRollsBackWholeCheckout() {
        int product = product("500.00", 10);
        // The real mapper is used normally; inject a late failure only after real SQL writes.
        doAnswer(invocation -> {
            assertThat(TransactionSynchronizationManager.isActualTransactionActive()).isTrue();
            entityManager.flush();
            assertPurchaseRows("1500.00", 1);
            assertThat(stock(product)).isEqualTo(7);
            throw new IllegalStateException("fixture failure after persisted checkout");
        }).when(lineMapper).purchaseDetailEntityToDto(any());

        assertThatThrownBy(() -> checkout.addPurchaseDetails(List.of(item(product, 3))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("fixture failure after persisted checkout");

        assertNoPurchase();
        assertThat(stock(product)).isEqualTo(10);
    }

    private int product(String price, int quantity) {
        jdbc.update("INSERT INTO product (product_name, product_description, product_cost, "
                + "product_quantity, product_available, record_status, created_on_timestamp, "
                + "created_by_user, created_by_process) "
                + "VALUES ('Fixture pizza', 'Synthetic fixture', ?, ?, TRUE, 'A', CURRENT_TIMESTAMP, 'test', 'test')",
                new BigDecimal(price), quantity);
        return jdbc.queryForObject("SELECT MAX(product_id) FROM product", Integer.class);
    }

    private PurchaseDetailDto item(int product, int quantity) {
        PurchaseDetailDto request = new PurchaseDetailDto();
        request.setProductId(product);
        request.setProductQuantity(quantity);
        return request;
    }

    private void assertPurchaseRows(String total, int lines) {
        assertThat(count("purchase")).isEqualTo(1);
        assertThat(count("purchase_detail")).isEqualTo(lines);
        assertThat(count("cart")).isEqualTo(1);
        assertThat(amount("SELECT purchase_amount FROM purchase")).isEqualByComparingTo(total);
        assertThat(amount("SELECT SUM(total_product_cost) FROM purchase_detail")).isEqualByComparingTo(total);
        assertThat(jdbc.queryForObject("SELECT purchase_items FROM purchase", Integer.class)).isEqualTo(lines);
        assertThat(jdbc.queryForObject("SELECT user_id FROM cart", Integer.class)).isEqualTo(userId);
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM purchase_detail d JOIN purchase p "
                + "ON d.purchase_id = p.purchase_id", Integer.class)).isEqualTo(lines);
    }

    private void assertNoPurchase() {
        assertThat(TransactionSynchronizationManager.isActualTransactionActive()).isFalse();
        assertThat(count("purchase")).isZero();
        assertThat(count("purchase_detail")).isZero();
        assertThat(count("cart")).isZero();
    }

    private int stock(int product) {
        return jdbc.queryForObject("SELECT product_quantity FROM product WHERE product_id = ?", Integer.class, product);
    }

    private int count(String table) {
        return jdbc.queryForObject("SELECT COUNT(*) FROM " + table, Integer.class);
    }

    private BigDecimal amount(String query) {
        return jdbc.queryForObject(query, BigDecimal.class);
    }
}
