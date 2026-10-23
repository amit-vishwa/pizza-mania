package com.pizzamania.persistence;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(properties = "spring.datasource.jdbc-url=jdbc:h2:mem:checkout_contract;MODE=MySQL;NON_KEYWORDS=USER;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE")
@ActiveProfiles("test")
class PurchaseCheckoutTransactionTest extends AbstractCheckoutContract {
}
