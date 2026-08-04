package com.enterprise.dubbo.consumer.controller;

import com.enterprise.dubbo.api.model.OrderRequest;
import com.enterprise.dubbo.api.model.OrderResponse;
import com.enterprise.dubbo.api.service.OrderService;
import org.apache.dubbo.config.annotation.DubboReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Expõe endpoints REST que chamam os serviços RPC de forma transparente.
 * Demonstra o uso de diferentes estratégias de Load Balancing (Random, RoundRobin, LeastActive, ConsistentHash).
 */
@RestController
@RequestMapping("/api/orders")
public class OrderGatewayController {

    private static final Logger logger = LoggerFactory.getLogger(OrderGatewayController.class);
    private static final String TRACE_ID_KEY = "traceId";

    /**
     * Injeta a referência remota do serviço de pedidos com estratégia de Load Balancing.
     * Estratégias do Dubbo: "random", "roundrobin", "leastactive", "consistenthash".
     */
    @DubboReference(
            version = "1.0.0",
            loadbalance = "roundrobin",
            check = false,
            timeout = 3000
    )
    private OrderService orderService;

    /**
     * Cria um novo pedido síncronamente.
     */
    @PostMapping
    public ResponseEntity<OrderResponse> createOrderSync(@RequestBody OrderRequest request) {
        String traceId = "API-S-" + UUID.randomUUID().toString().substring(0, 8);
        MDC.put(TRACE_ID_KEY, traceId);
        try {
            logger.info("[REST Gateway] Recebida requisição REST de criação de pedido: {}", request);
            OrderResponse response = orderService.createOrder(request);
            logger.info("[REST Gateway] Pedido criado com sucesso: {}", response);
            return ResponseEntity.ok(response);
        } finally {
            MDC.remove(TRACE_ID_KEY);
        }
    }

    /**
     * Cria um novo pedido assíncronamente.
     * Retorna CompletableFuture delegando o processamento assíncrono nativo do Dubbo.
     */
    @PostMapping("/async")
    public CompletableFuture<ResponseEntity<OrderResponse>> createOrderAsync(@RequestBody OrderRequest request) {
        String traceId = "API-A-" + UUID.randomUUID().toString().substring(0, 8);
        MDC.put(TRACE_ID_KEY, traceId);
        logger.info("[REST Gateway] Recebida requisição assíncrona de criação de pedido: {}", request);

        return orderService.createOrderAsync(request)
                .thenApply(response -> {
                    logger.info("[REST Gateway] Resposta assíncrona recebida do Provedor: {}", response);
                    return ResponseEntity.ok(response);
                })
                .whenComplete((res, ex) -> MDC.remove(TRACE_ID_KEY));
    }

    /**
     * Consulta as informações de um pedido por ID.
     */
    @GetMapping("/{orderId}")
    public ResponseEntity<OrderResponse> getOrderById(@PathVariable String orderId) {
        String traceId = "API-G-" + UUID.randomUUID().toString().substring(0, 8);
        MDC.put(TRACE_ID_KEY, traceId);
        try {
            logger.info("[REST Gateway] Consultando pedido ID: {}", orderId);
            OrderResponse response = orderService.getOrderById(orderId);
            return ResponseEntity.ok(response);
        } finally {
            MDC.remove(TRACE_ID_KEY);
        }
    }
}
