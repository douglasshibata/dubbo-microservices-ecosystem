package com.enterprise.dubbo.consumer.filter;

import org.apache.dubbo.common.constants.CommonConstants;
import org.apache.dubbo.common.extension.Activate;
import org.apache.dubbo.rpc.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.util.UUID;

/**
 * Filtro customizado do Apache Dubbo para interceptar as chamadas no Consumidor (Consumer).
 * Injeta o Trace ID no contexto RPC de modo que seja propagado automaticamente para o Provedor.
 */
@Activate(group = CommonConstants.CONSUMER, order = -9000)
public class ConsumerTraceFilter implements Filter {

    private static final Logger logger = LoggerFactory.getLogger(ConsumerTraceFilter.class);
    private static final String TRACE_ID_KEY = "traceId";

    @Override
    public Result invoke(Invoker<?> invoker, Invocation invocation) throws RpcException {
        // Obtém o Trace ID existente no MDC ou gera um novo caso seja o início da cadeia de chamadas
        String traceId = MDC.get(TRACE_ID_KEY);
        if (traceId == null || traceId.trim().isEmpty()) {
            traceId = "TRACE-C-" + UUID.randomUUID().toString().substring(0, 8);
            MDC.put(TRACE_ID_KEY, traceId);
        }

        // Anexa o Trace ID no contexto de serviço do Dubbo para transmissão via RPC
        RpcContext.getServiceContext().setAttachment(TRACE_ID_KEY, traceId);

        long startTime = System.currentTimeMillis();
        logger.info("[Consumer Filter] Iniciando chamada RPC externa para {}.{} | TraceId: {}",
                invoker.getInterface().getSimpleName(), invocation.getMethodName(), traceId);

        try {
            Result result = invoker.invoke(invocation);
            long elapsed = System.currentTimeMillis() - startTime;
            logger.info("[Consumer Filter] Retorno da chamada RPC. Latência: {} ms", elapsed);
            return result;
        } catch (RpcException e) {
            long elapsed = System.currentTimeMillis() - startTime;
            logger.error("[Consumer Filter] Falha na chamada RPC. Latência: {} ms | Erro: {}",
                    elapsed, e.getMessage(), e);
            throw e;
        }
    }
}
