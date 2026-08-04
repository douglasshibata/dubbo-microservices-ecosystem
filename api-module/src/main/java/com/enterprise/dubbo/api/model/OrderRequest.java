package com.enterprise.dubbo.api.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Representa a requisição de criação de um novo pedido.
 */
public class OrderRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    private String customerId;
    private String productCode;
    private Integer quantity;
    private BigDecimal price;

    public OrderRequest() {}

    public OrderRequest(String customerId, String productCode, Integer quantity, BigDecimal price) {
        this.customerId = customerId;
        this.productCode = productCode;
        this.quantity = quantity;
        this.price = price;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public String getProductCode() {
        return productCode;
    }

    public void setProductCode(String productCode) {
        this.productCode = productCode;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    @Override
    public String toString() {
        return "OrderRequest{" +
                "customerId='" + customerId + '\'' +
                ", productCode='" + productCode + '\'' +
                ", quantity=" + quantity +
                ", price=" + price +
                '}';
    }
}
