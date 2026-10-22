package com.pizzamania.service;

import java.math.BigDecimal;
import java.util.List;

import com.pizzamania.dto.ProductDto;
import com.pizzamania.dto.PurchaseDetailDto;
import com.pizzamania.dto.PurchaseDto;
import com.pizzamania.mapper.ProductMapper;
import com.pizzamania.mapper.PurchaseDetailMapper;
import com.pizzamania.model.Product;
import com.pizzamania.model.PurchaseDetail;
import com.pizzamania.repository.PurchaseDetailManagementRepository;
import com.pizzamania.repository.PurchaseManagementRepository;
import com.pizzamania.utility.Page;
import com.pizzamania.utility.SecurityContextUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PurchaseAmountValidationTest {
    @Mock private ProductManagementService products;
    @Mock private PurchaseManagementService purchases;
    @Mock private CartManagementService carts;
    @Mock private PurchaseDetailManagementRepository lines;
    @Mock private ProductMapper productMapper;
    @Mock private PurchaseDetailMapper lineMapper;
    @Mock private SecurityContextUtil security;
    @Mock private PurchaseManagementRepository purchaseRepository;
    @InjectMocks private PurchaseDetailManagementServiceImpl checkout;
    @InjectMocks private PurchaseManagementServiceImpl purchaseService;

    @Test
    void excessiveLineTotalIsRejectedBeforeInventoryWrite() {
        productAt500();
        assertThrows(IllegalArgumentException.class,
                () -> checkout.addPurchaseDetails(List.of(item(200000))));
        verify(products, never()).saveAllProducts(any());
        verifyNoInteractions(purchases, carts, lines, productMapper, lineMapper);
    }

    @Test
    void individuallyValidLinesCannotOverflowPurchaseHeader() {
        productAt500();
        when(security.getUserName()).thenReturn("fixture");
        when(productMapper.productDtoToEntity(any())).thenAnswer(invocation -> {
            Product product = new Product();
            product.setProductQuantity(200000);
            return product;
        });
        when(lineMapper.purchaseDetailDtoToEntity(any())).thenAnswer(invocation -> {
            PurchaseDetailDto dto = invocation.getArgument(0);
            PurchaseDetail line = new PurchaseDetail();
            line.setTotalProductCost(dto.getTotalProductCost());
            return line;
        });
        assertThrows(IllegalArgumentException.class,
                () -> checkout.addPurchaseDetails(List.of(item(100000), item(100000))));
        // Real inventory rollback is tested separately with actual transactions.
        verify(products, times(2)).saveAllProducts(any());
        verifyNoInteractions(purchases, carts, lines);
    }

    @Test
    void directPurchaseCreateRejectsExcessiveHeaderBeforeSaving() {
        PurchaseDto request = new PurchaseDto();
        request.setPurchaseItems(1);
        request.setPurchaseAmount(new BigDecimal("100000000.00"));
        assertThrows(IllegalArgumentException.class,
                () -> purchaseService.addPurchases(List.of(request)));
        verifyNoInteractions(purchaseRepository);
    }

    private void productAt500() {
        ProductDto product = new ProductDto();
        product.setProductCost(new BigDecimal("500.00"));
        product.setProductQuantity(200000);
        when(products.searchProducts(any())).thenReturn(new Page<>(1, 0, 1, List.of(product)));
    }

    private PurchaseDetailDto item(int quantity) {
        PurchaseDetailDto request = new PurchaseDetailDto();
        request.setProductId(1);
        request.setProductQuantity(quantity);
        return request;
    }
}
