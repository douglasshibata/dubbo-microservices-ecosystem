package com.enterprise.dubbo.provider.service;

import com.enterprise.dubbo.api.model.OrderRequest;
import com.enterprise.dubbo.api.model.OrderResponse;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;

public class OrderServiceImplTest {

    private final OrderServiceImpl orderService = new OrderServiceImpl();

    @Test
    public void testCreateOrder_Success() {
        OrderRequest request = new OrderRequest("cust-123", "PROD-ABC", 2, BigDecimal.valueOf(49.90));

        OrderResponse response = orderService.createOrder(request);

        assertNotNull(response);
        assertNotNull(response.getOrderId());
        assertEquals("cust-123", response.getCustomerId());
        assertEquals("PROD-ABC", response.getProductCode());
        assertEquals(2, response.getQuantity());
        assertEquals(BigDecimal.valueOf(99.80), response.getTotalAmount());
        assertEquals("PROCESSADO", response.getStatus());
        assertEquals("provider-node-1", response.getProcessedByNode());
    }

    @Test
    public void testCreateOrder_InvalidQuantity_ThrowsException() {
        OrderRequest request = new OrderRequest("cust-123", "PROD-ABC", 0, BigDecimal.valueOf(49.90));

        assertThrows(IllegalArgumentException.class, () -> {
            orderService.createOrder(request);
        });
    }

    @Test
    public void testCreateOrderAsync_Success() throws Exception {
        OrderRequest request = new OrderRequest("cust-456", "PROD-XYZ", 3, BigDecimal.valueOf(10.00));

        CompletableFuture<OrderResponse> future = orderService.createOrderAsync(request);
        OrderResponse response = future.get();

        assertNotNull(response);
        assertEquals(BigDecimal.valueOf(30.00), response.getTotalAmount());
        assertEquals("PROCESSADO", response.getStatus());
    }

    @Test
    public void testGetOrderById_Success() {
        OrderResponse response = orderService.getOrderById("ORD-TEST123");

        assertNotNull(response);
        assertEquals("ORD-TEST123", response.getOrderId());
        assertEquals("cust-999", response.getCustomerId());
        assertEquals("CONCLUIDO", response.getStatus());
    }

    @Test
    public void testGetOrderById_InvalidId_ThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> {
            orderService.getOrderById("invalid-id");
        });
    }
}
