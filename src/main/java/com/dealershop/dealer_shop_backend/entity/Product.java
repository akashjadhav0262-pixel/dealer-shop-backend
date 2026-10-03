package com.dealershop.dealer_shop_backend.entity;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(
        name = "products",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_product_shop_sku", columnNames = {"shop_id", "sku"}),
                @UniqueConstraint(name = "uk_product_shop_barcode", columnNames = {"shop_id", "barcode"})
        }
)
public class Product extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "shop_id", nullable = false)
    private Shop shop;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(length = 500)
    private String description;

    @Column(nullable = false, length = 50)
    private String sku;

    @Column(length = 50)
    private String barcode;

    @Column(name = "purchase_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal purchasePrice = BigDecimal.ZERO;

    @Column(name = "selling_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal sellingPrice = BigDecimal.ZERO;

    @Column(name = "stock_quantity", nullable = false, precision = 12, scale = 3)
    private BigDecimal stockQuantity = BigDecimal.ZERO;

    @Column(name = "min_stock_threshold", nullable = false, precision = 12, scale = 3)
    private BigDecimal minStockThreshold = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Unit unit = Unit.PIECE;

    @Column(nullable = false)
    private boolean active = true;

    public Product() {
    }

    public Shop getShop() { return shop; }
    public void setShop(Shop shop) { this.shop = shop; }

    public Category getCategory() { return category; }
    public void setCategory(Category category) { this.category = category; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getSku() { return sku; }
    public void setSku(String sku) { this.sku = sku; }

    public String getBarcode() { return barcode; }
    public void setBarcode(String barcode) { this.barcode = barcode; }

    public BigDecimal getPurchasePrice() { return purchasePrice; }
    public void setPurchasePrice(BigDecimal purchasePrice) { this.purchasePrice = purchasePrice; }

    public BigDecimal getSellingPrice() { return sellingPrice; }
    public void setSellingPrice(BigDecimal sellingPrice) { this.sellingPrice = sellingPrice; }

    public BigDecimal getStockQuantity() { return stockQuantity; }
    public void setStockQuantity(BigDecimal stockQuantity) { this.stockQuantity = stockQuantity; }

    public BigDecimal getMinStockThreshold() { return minStockThreshold; }
    public void setMinStockThreshold(BigDecimal minStockThreshold) { this.minStockThreshold = minStockThreshold; }

    public Unit getUnit() { return unit; }
    public void setUnit(Unit unit) { this.unit = unit; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}