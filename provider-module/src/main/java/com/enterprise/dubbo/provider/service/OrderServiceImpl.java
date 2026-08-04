package com.enterprise.dubbo.provider.service;

import com.enterprise.dubbo.api.model.OrderRequest;
import com.enterprise.dubbo.api.model.OrderResponse;
import com.enterprise.dubbo.api.service.OrderService;
import org.apache.dubbo.config.annotation.DubboService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Implementação do serviço de pedidos.
 * Marcado com @DubboService definindo versão, tempo limite e número de retentativas.
 */
@DubboService(version = "1.0.0", timeout = 3000, retries = 2)
public class OrderServiceImpl implements OrderService {

    private static final Logger logger = LoggerFactory.getLogger(OrderServiceImpl.class);
    private static final String NODE_NAME = "provider-node-1";

    @Override
    public OrderResponse createOrder(OrderRequest request) {
        logger.info("[RPC Server] Recebida requisição síncrona para criar pedido: {}", request);
        return processRequest(request);
    }

    @Override
    public CompletableFuture<OrderResponse> createOrderAsync(OrderRequest request) {
        logger.info("[RPC Server] Recebida requisição assíncrona para criar pedido: {}", request);
        return CompletableFuture.supplyAsync(() -> {
            try {
                // Simula processamento assíncrono um pouco mais lento
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                logger.error("Erro no processamento assíncrono", e);
            }
            return processRequest(request);
        });
    }

    @Override
    public OrderResponse getOrderById(String orderId) {
        logger.info("[RPC Server] Recebida consulta para o pedido ID: {}", orderId);

        // Simulação de retorno de pedido fixo ou erro planejado
        if ("invalid-id".equals(orderId)) {
            logger.warn("[RPC Server] ID de pedido inválido fornecido!");
            throw new IllegalArgumentException("Pedido com ID inválido não encontrado.");
        }

        BigDecimal total = BigDecimal.valueOf(150.00);
        return new OrderResponse(
                orderId,
                "cust-999",
                "PROD-DEFAULT",
                1,
                total,
                "CONCLUIDO",
                NODE_NAME,
                LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME)
        );
    }

    private OrderResponse processRequest(OrderRequest request) {
        if (request.getQuantity() == null || request.getQuantity() <= 0) {
            throw new IllegalArgumentException("A quantidade deve ser maior do que zero.");
        }

        String orderId = "ORD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        BigDecimal price = request.getPrice() != null ? request.getPrice() : BigDecimal.ZERO;
        BigDecimal totalAmount = price.multiply(BigDecimal.valueOf(request.getQuantity()));

        String createdAt = LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME);

        return new OrderResponse(
                orderId,
                request.getCustomerId(),
                request.getProductCode(),
                request.getQuantity(),
                totalAmount,
                "PROCESSADO",
                NODE_NAME,
                createdAt
        );
    }
}
