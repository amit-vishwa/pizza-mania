package com.pizzamania.persistence;

import com.pizzamania.model.Product;
import com.pizzamania.repository.ProductManagementSearchRepository;
import com.pizzamania.utility.Page;
import com.pizzamania.utility.Paging;
import com.pizzamania.utility.SearchCriteria;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = "spring.datasource.jdbc-url=jdbc:h2:mem:search_pages;MODE=MySQL;NON_KEYWORDS=USER;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE")
@ActiveProfiles("test")
class SearchPaginationIntegrationTest {
    @Autowired ProductManagementSearchRepository repository;
    @Autowired JdbcTemplate jdbc;
    @MockitoBean JwtDecoder jwtDecoder;

    @BeforeEach
    void resetOwnedDatabase() {
        assertThat(TransactionSynchronizationManager.isActualTransactionActive()).isFalse();
        jdbc.update("DELETE FROM product");
    }

    @ParameterizedTest(name = "rows={0}, page={1}, size={2}")
    @CsvSource({"15,0,10", "15,1,10", "15,2,10", "20,2,10", "0,0,10",
            "15,2147483647,10", "15,1073741824,4", "0,2,10"})
    void returnsRequestedRowsOrEmptyPage(int rows, int pageNumber, int size) {
        for (int i = 0; i < rows; i++) {
            jdbc.update("INSERT INTO product (product_name, product_description, product_cost, "
                    + "product_quantity, product_available, record_status, created_on_timestamp, "
                    + "created_by_user, created_by_process) "
                    + "VALUES (?, 'Synthetic pagination fixture', 1.00, 1, TRUE, 'A', "
                    + "CURRENT_TIMESTAMP, 'test', 'test')", "Fixture-" + i);
        }

        Page<Product> result = repository.search(new SearchCriteria<>(new Product(), new Paging(pageNumber, size)));

        int start = (int) Math.min((long) pageNumber * size, rows);
        int end = Math.min(start + size, rows);
        assertThat(result.getTotalRecords()).isEqualTo(rows);
        assertThat(result.getRecordsInPage()).isEqualTo(size);
        assertThat(result.getCurrentPage()).isEqualTo(pageNumber);
        assertThat(result.getFirstRecordOffset()).isEqualTo((long) pageNumber * size);
        assertThat(result.getTotalPages()).isEqualTo((rows + size - 1) / size);
        assertThat(result.getPageEntries()).extracting(Product::getProductName)
                .containsExactlyElementsOf(IntStream.range(start, end).mapToObj(i -> "Fixture-" + i).toList());
    }
}
