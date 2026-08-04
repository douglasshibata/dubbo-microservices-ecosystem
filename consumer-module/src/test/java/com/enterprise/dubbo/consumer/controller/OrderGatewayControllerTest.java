package com.enterprise.dubbo.consumer.controller;

import com.enterprise.dubbo.api.model.OrderRequest;
import com.enterprise.dubbo.api.model.OrderResponse;
import com.enterprise.dubbo.api.service.OrderService;
import com.enterprise.dubbo.consumer.exception.GlobalExceptionHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.util.concurrent.CompletableFuture;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
public class OrderGatewayControllerTest {

    private MockMvc mockMvc;

    @Mock
    private OrderService orderService;

    @InjectMocks
    private OrderGatewayController orderGatewayController;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    public void setup() {
        this.mockMvc = MockMvcBuilders.standaloneSetup(orderGatewayController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    public void testCreateOrderSync_Success() throws Exception {
        OrderRequest request = new OrderRequest("cust-111", "PROD-1", 5, BigDecimal.valueOf(10.00));
        OrderResponse mockResponse = new OrderResponse("ORD-1", "cust-111", "PROD-1", 5,
                BigDecimal.valueOf(50.00), "PROCESSADO", "provider-node-1", "2026-08-04T00:00:00");

        when(orderService.createOrder(any(OrderRequest.class))).thenReturn(mockResponse);

        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderId").value("ORD-1"))
                .andExpect(jsonPath("$.status").value("PROCESSADO"))
                .andExpect(jsonPath("$.totalAmount").value(50.00));
    }

    @Test
    public void testGetOrderById_Success() throws Exception {
        OrderResponse mockResponse = new OrderResponse("ORD-2", "cust-999", "PROD-DEFAULT", 1,
                BigDecimal.valueOf(150.00), "CONCLUIDO", "provider-node-1", "2026-08-04T00:00:00");

        when(orderService.getOrderById("ORD-2")).thenReturn(mockResponse);

        mockMvc.perform(get("/api/orders/ORD-2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderId").value("ORD-2"))
                .andExpect(jsonPath("$.status").value("CONCLUIDO"));
    }

    @Test
    public void testGetOrderById_InvalidId_ReturnsBadRequest() throws Exception {
        when(orderService.getOrderById("invalid-id")).thenThrow(new IllegalArgumentException("ID inválido"));

        mockMvc.perform(get("/api/orders/invalid-id"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Parâmetro Inválido"))
                .andExpect(jsonPath("$.message").value("ID inválido"));
    }
}
