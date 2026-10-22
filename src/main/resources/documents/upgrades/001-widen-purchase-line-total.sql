-- Apply once to an existing pizza-mania schema before deploying the application.
-- Widening preserves existing amounts; no arithmetic conversion or data deletion.
ALTER TABLE `purchase_detail`
    MODIFY COLUMN `total_product_cost` DECIMAL(10,2) NOT NULL;
