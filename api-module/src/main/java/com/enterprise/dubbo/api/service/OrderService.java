package com.enterprise.dubbo.api.service;

import com.enterprise.dubbo.api.model.OrderRequest;
import com.enterprise.dubbo.api.model.OrderResponse;

import java.util.concurrent.CompletableFuture;

/**
 * Interface de Contrato para o microsserviço de gerenciamento de pedidos.
 * Demonstra comunicação RPC síncrona e assíncrona com Apache Dubbo 3.
 */
public interface OrderService {

    /**
     * Processa um pedido de forma síncrona.
     * @param request Detalhes do pedido.
     * @return Resposta com os detalhes e status do pedido processado.
     */
    OrderResponse createOrder(OrderRequest request);

    /**
     * Processa um pedido de forma assíncrona utilizando CompletableFuture (Nativo Dubbo 3).
     * @param request Detalhes do pedido.
     * @return CompletableFuture contendo a resposta do pedido processado.
     */
    CompletableFuture<OrderResponse> createOrderAsync(OrderRequest request);

    /**
     * Consulta as informações de um pedido existente por ID.
     * @param orderId ID do pedido.
     * @return Resposta do pedido.
     */
    OrderResponse getOrderById(String orderId);
}
