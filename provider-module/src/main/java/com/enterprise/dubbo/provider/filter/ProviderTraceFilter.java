package com.enterprise.dubbo.provider.filter;

import org.apache.dubbo.common.constants.CommonConstants;
import org.apache.dubbo.common.extension.Activate;
import org.apache.dubbo.rpc.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.util.UUID;

/**
 * Filtro customizado do Apache Dubbo para interceptar as chamadas no Provedor (Provider).
 * Gera/propaga Trace ID e loga a latência das execuções.
 */
@Activate(group = CommonConstants.PROVIDER, order = -9000)
public class ProviderTraceFilter implements Filter {

    private static final Logger logger = LoggerFactory.getLogger(ProviderTraceFilter.class);
    private static final String TRACE_ID_KEY = "traceId";

    @Override
    public Result invoke(Invoker<?> invoker, Invocation invocation) throws RpcException {
        // Extrai o traceId passado pelo cliente através dos attachments do contexto RPC
        String traceId = RpcContext.getServiceContext().getAttachment(TRACE_ID_KEY);

        if (traceId == null || traceId.trim().isEmpty()) {
            traceId = "PROVIDER-GEN-" + UUID.randomUUID().toString().substring(0, 8);
        }

        // Coloca o traceId no MDC do SLF4J para rastreamento centralizado de logs
        MDC.put(TRACE_ID_KEY, traceId);

        long startTime = System.currentTimeMillis();
        try {
            logger.info("[Trace Interceptor] Iniciando chamada RPC para {}.{} | TraceId: {}",
                    invoker.getInterface().getSimpleName(), invocation.getMethodName(), traceId);

            // Executa o próximo filtro ou o serviço propriamente dito
            Result result = invoker.invoke(invocation);

            long elapsed = System.currentTimeMillis() - startTime;
            logger.info("[Trace Interceptor] Chamada RPC executada com SUCESSO. Latência: {} ms", elapsed);
            return result;

        } catch (RpcException e) {
            long elapsed = System.currentTimeMillis() - startTime;
            logger.error("[Trace Interceptor] Erro RPC ao executar o método. Latência: {} ms | Erro: {}",
                    elapsed, e.getMessage(), e);
            throw e;
        } finally {
            MDC.remove(TRACE_ID_KEY);
        }
    }
}
