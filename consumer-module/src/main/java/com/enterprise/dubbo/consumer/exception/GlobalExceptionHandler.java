package com.enterprise.dubbo.consumer.exception;

import org.apache.dubbo.rpc.RpcException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Captura exceções globais REST e formata de maneira limpa para o cliente.
 * Trata RpcException do Dubbo, permitindo diferenciar erros de timeout, conexão e negócio.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(RpcException.class)
    public ResponseEntity<Map<String, Object>> handleRpcException(RpcException ex) {
        logger.error("[REST Gateway Error] Exceção RPC capturada: {}", ex.getMessage(), ex);

        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now().toString());
        body.put("status", HttpStatus.FAILED_DEPENDENCY.value());
        body.put("error", "Erro de Comunicação RPC");
        body.put("code", ex.getCode());

        String message = ex.getMessage();
        if (ex.isTimeout()) {
            body.put("message", "A requisição remota excedeu o tempo limite (Timeout).");
            body.put("details", message);
        } else if (ex.isNetwork()) {
            body.put("message", "Não foi possível se conectar ao provedor do serviço (Falha de Rede).");
            body.put("details", message);
        } else if (ex.isForbidden()) {
            body.put("message", "Acesso proibido ou falha de autorização no Provedor.");
            body.put("details", message);
        } else {
            body.put("message", "Ocorreu um erro interno na comunicação remota com o microsserviço.");
            body.put("details", message);
        }

        return new ResponseEntity<>(body, HttpStatus.FAILED_DEPENDENCY);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgument(IllegalArgumentException ex) {
        logger.warn("[REST Gateway Warning] Requisição inválida: {}", ex.getMessage());

        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now().toString());
        body.put("status", HttpStatus.BAD_REQUEST.value());
        body.put("error", "Parâmetro Inválido");
        body.put("message", ex.getMessage());

        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGeneralException(Exception ex) {
        logger.error("[REST Gateway Error] Exceção inesperada: {}", ex.getMessage(), ex);

        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now().toString());
        body.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
        body.put("error", "Erro Interno do Servidor");
        body.put("message", "Ocorreu um erro inesperado no gateway.");
        body.put("details", ex.getMessage());

        return new ResponseEntity<>(body, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
