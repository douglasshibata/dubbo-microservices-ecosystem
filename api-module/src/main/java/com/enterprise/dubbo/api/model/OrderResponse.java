package com.enterprise.dubbo.api.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Representa a resposta de um pedido processado.
 */
public class OrderResponse implements Serializable {
    private static final long serialVersionUID = 1L;

    private String orderId;
    private String customerId;
    private String productCode;
    private Integer quantity;
    private BigDecimal totalAmount;
    private String status;
    private String processedByNode;
    private String createdAt;

    public OrderResponse() {}

    public OrderResponse(String orderId, String customerId, String productCode, Integer quantity,
                         BigDecimal totalAmount, String status, String processedByNode, String createdAt) {
        this.orderId = orderId;
        this.customerId = customerId;
        this.productCode = productCode;
        this.quantity = quantity;
        this.totalAmount = totalAmount;
        this.status = status;
        this.processedByNode = processedByNode;
        this.createdAt = createdAt;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
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

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getProcessedByNode() {
        return processedByNode;
    }

    public void setProcessedByNode(String processedByNode) {
        this.processedByNode = processedByNode;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "OrderResponse{" +
                "orderId='" + orderId + '\'' +
                ", customerId='" + customerId + '\'' +
                ", productCode='" + productCode + '\'' +
                ", quantity=" + quantity +
                ", totalAmount=" + totalAmount +
                ", status='" + status + '\'' +
                ", processedByNode='" + processedByNode + '\'' +
                ", createdAt='" + createdAt + '\'' +
                '}';
    }
}
