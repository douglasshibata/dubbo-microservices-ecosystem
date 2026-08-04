# 🚀 Apache Dubbo 3: Ecossistema de Microserviços de Alta Performance

Este diretório contém o código de referência completo e a especificação arquitetural de um ecossistema de microserviços escalável, resiliente e de ultra-baixa latência projetado com **Apache Dubbo 3.3.0**, **Spring Boot 3.4.3** (Java 21) e **Apache ZooKeeper**.

---

## 🏗️ 1. Arquitetura do Sistema e Fluxo de Comunicação

O Apache Dubbo 3 é uma estrutura de RPC e governança de serviços altamente escalável e otimizada para ambientes cloud-native. Abaixo está o diagrama detalhado que ilustra a arquitetura interna e o fluxo de interação entre os componentes do ecossistema:

```text
               ┌────────────────────────────────────────┐
               │         Dubbo Admin Dashboard          │
               │      (Governança e Config Center)      │
               └───────────────────┬────────────────────┘
                                   │ (Leitura / Gravação de Regras)
                                   ▼
┌───────────────────┐       ┌─────────────┐       ┌───────────────────┐
│     Registry      │◄──────┤Config Center├──────►│     Metadata      │
│ (ZooKeeper:2181)  │       └──────┬──────┘       │  (ZooKeeper:2181) │
└─────────▲─────────┘              │              └─────────▲─────────┘
          │ (Reg./Lookup)          │ (Notifica regras)      │ (Envia metadados)
          │                        ▼                        │
   ┌──────┴──────┐          ┌─────────────┐          ┌──────┴──────┐
   │  Consumer   ├─────────►│   Router    ├─────────►│  Provider   │
   │  (Gateway)  │          │   Filter    │          │  (Service)  │
   │  Port: 8080 │          └─────────────┘          │ Port: 50051 │
   └──────┬──────┘                                   └──────▲──────┘
          │                                                 │
          └───────────────── RPC (Protocolo Triple) ────────┘
                             (HTTP/2 + Protobuf)
```

### 🌟 Principais Diferenciais do Apache Dubbo 3:

1. **App-level Service Discovery (Registro Orientado a Instâncias):**
   - No Dubbo 2, o registro de serviços ocorria ao nível de *Interface* (método por método), o que inflava drasticamente o tamanho dos nós no ZooKeeper em ecossistemas com centenas de interfaces.
   - O Dubbo 3 introduz o **Service Discovery ao nível de Aplicação (App-level)**, similar ao Consul ou Spring Cloud Eureka. O registro ocorre por instância de aplicação, enquanto os metadados das interfaces são armazenados separadamente no **Metadata Report Center**. Isso otimiza o uso de memória no registry em até 90%.

2. **Protocolo Triple (Nativo do Dubbo 3):**
   - Totalmente baseado em **HTTP/2** e compatível com o padrão **gRPC**, permitindo comunicação bidirecional via streaming (Unary, Client Streaming, Server Streaming e Bi-directional Streaming).
   - Suporta formatos de serialização flexíveis (Protobuf, Hessian2, Fastjson2).
   - **Interoperabilidade:** Por rodar sobre HTTP/2, ele aceita conexões diretas do navegador ou comandos de ferramentas utilitárias como `curl`, facilitando integrações com Frontends e API Gateways de terceiros.

3. **Cloud-Native & GraalVM AOT:**
   - Suporte de primeira classe para compilação Ahead-of-Time (AOT), possibilitando empacotar os microsserviços como imagens nativas de inicialização instantânea e pegada de memória reduzida (ideal para ambientes Kubernetes/Serverless).

---

## 📦 2. Estrutura do Projeto

O ecossistema está organizado como um projeto multi-módulos Maven:

* **`api-module` (Contrato do Serviço):** Contém as classes de DTO compartilhadas (`OrderRequest`, `OrderResponse`) e a interface do serviço RPC (`OrderService`). Garante baixo acoplamento e um contrato unificado.
* **`provider-module` (Provedor do Serviço):** Expõe as implementações concretas do serviço. Sobe na porta HTTP `8081` e escuta chamadas RPC Triple na porta `50051`.
* **`consumer-module` (Consumidor / API REST Gateway):** Consome os serviços RPC do Provedor de forma transparente. Sobe na porta `8080`, expondo endpoints REST públicos que repassam as chamadas internamente via Triple Protocol.

---

## 🐳 3. Infraestrutura Containerizada (`docker-compose.yml`)

O ecossistema é facilmente provisionado utilizando Docker Compose. O arquivo `docker-compose.yml` localizado na raiz deste diretório gerencia os seguintes serviços:

| Serviço | Imagem | Porta Exposta | Função |
|---|---|---|---|
| `zookeeper` | `zookeeper:3.9.2` | `2181` | Registro de Serviços, Config Center e Metadata Report |
| `dubbo-admin` | `apache/dubbo-admin:0.7.0` | `38080` (Interno: `8080`) | Console Web de Governança, Monitoramento e Roteamento |
| `provider-service`| Custom (Java 21 Alpine) | `8081` (Web), `50051` (Triple RPC), `22222` (QOS) | Microsserviço Provedor de Pedidos |
| `consumer-service`| Custom (Java 21 Alpine) | `8080` (Web API), `33333` (QOS) | API Gateway Consumidora |
| `prometheus` | `prom/prometheus:v2.54.1` | `9090` | Coleta de métricas operacionais e latência |
| `grafana` | `grafana/grafana:11.2.0` | `3000` | Visualização de Dashboards de performance |

### Como Executar:

1. Compile o projeto Maven a partir do diretório do ecossistema:
   ```bash
   mvn clean package
   ```
2. Inicialize todos os containers em segundo plano:
   ```bash
   docker-compose up -d --build
   ```
3. Acesse as plataformas:
   - **REST Gateway (Consumer):** `http://localhost:8080/api/orders/ORD-123`
   - **Dubbo Admin Dashboard:** `http://localhost:38080` (credenciais padrão: `root` / `root` se ativadas)
   - **Prometheus:** `http://localhost:9090`
   - **Grafana:** `http://localhost:3000` (credenciais: `admin` / `admin`)

---

## 🛠️ 4. Código e Implementação de Referência

### `@DubboService` vs `@DubboReference`

O ecossistema demonstra o uso correto e idiomático do framework no ecossistema Spring Boot.

#### No Provedor (`OrderServiceImpl.java`):
A anotação `@DubboService` exporta a interface de serviço para o ZooKeeper automaticamente:
```java
@DubboService(version = "1.0.0", timeout = 3000, retries = 2)
public class OrderServiceImpl implements OrderService {
    // ...
}
```

#### No Consumidor (`OrderGatewayController.java`):
A anotação `@DubboReference` injeta de forma transparente um Proxy dinâmico cliente do Dubbo 3 que lida com Service Discovery, Load Balancing e Resiliência de rede:
```java
@DubboReference(
        version = "1.0.0",
        loadbalance = "roundrobin",
        check = false, // Impede que a aplicação quebre na inicialização caso o Provedor esteja temporariamente offline
        timeout = 3000
)
private OrderService orderService;
```

---

## 🛡️ 5. Resiliência, Roteamento Dinâmico e Governança

### Estratégias de Tolerância a Falhas (Fault Tolerance)

No ecossistema distribuído, o Dubbo oferece várias estratégias prontas de resiliência configuráveis via `@DubboReference(cluster = "...")` ou pelo painel do Dubbo Admin:

* **`failover` (Padrão):** Tenta reconectar a outro Provedor disponível caso ocorra uma falha de comunicação ou timeout. O número de tentativas é definido pelo atributo `retries` (definido como `2` no exemplo). Recomendado para operações **Idempotentes** (leituras/consultas).
* **`failfast`:** Retorna um erro imediatamente no primeiro sinal de falha da chamada. Indicado para operações não-idempotentes (ex: transações financeiras e criações de pedidos onde não se deseja duplicidade).
* **`failsafe`:** Captura e engole qualquer exceção que ocorra na chamada remota, logando o evento e retornando um objeto de resposta vazio ou nulo de forma segura. Ideal para sistemas de auditoria ou escrita de logs secundários.
* **`failback`:** Loga a falha e agenda de forma assíncrona uma nova chamada para o Provedor em intervalos periódicos até que a operação seja concluída com sucesso. Excelente para envio de notificações e sincronizações.
* **`forking`:** Executa a chamada RPC em paralelo em múltiplos provedores ao mesmo tempo. Assim que o primeiro provedor responder com sucesso, a resposta é entregue ao cliente e as demais requisições concorrentes são canceladas. Reduz a cauda de latência ao custo de maior consumo de recursos de rede.

### Roteamento Dinâmico de Tráfego

O Dubbo 3 permite criar regras de roteamento avançadas dinamicamente:

1. **Tag Routing (Canary / Blue-Green Deployments):**
   Diferencia os provedores com base em etiquetas configuradas via parâmetro (ex: `dubbo.provider.tag=v2`). As requisições que chegam ao Consumidor com o cabeçalho dinâmico correspondente são direcionadas exclusivamente para as instâncias tageadas:
   ```yaml
   # Regra enviada via Dubbo Admin Config
   configVersion: v3.0
   force: true
   enabled: true
   key: provider-service
   tags:
     - name: canary
       addresses: ["192.168.1.100:50051"]
   ```

2. **Condition Routing:**
   Direciona o tráfego com base em condições lógicas das propriedades da chamada:
   ```text
   # Exemplo: Redirecionar todas as chamadas do consumidor de teste para o provedor de homologação
   method = createOrder & consumer.host = 192.168.0.15 => provider.host = 192.168.0.20
   ```

### Logs Correlacionados e Rastreamento de Latência (Filtros SPI)

Demonstramos a criação de interceptores customizados usando a arquitetura de **Extension Loader (SPI - Service Provider Interface)** do Dubbo. Os filtros `ConsumerTraceFilter` e `ProviderTraceFilter`:
1. Geram um `traceId` único no gateway consumidor.
2. Injetam o ID nos attachments de transporte do Dubbo RPC.
3. Extraem o ID no servidor provedor e o configuram no **MDC (Mapped Diagnostic Context)** do SLF4J/Logback.
4. Calculam a latência exata da chamada e limpam o contexto após a finalização para evitar vazamentos de memória.

---

## 📈 6. Guia Operacional e Boas Práticas de Performance

### Otimização de Serialização

A eficiência de serialização dita diretamente a latência máxima e vazão do ecossistema RPC:

* **Hessian2 (Padrão no Dubbo 3.3):** Altamente estável, maduro, seguro e compatível com JDK 17/21. Excelente escolha genérica para ecossistemas puramente Java.
* **Protobuf (Protocol Buffers):** Ideal para alto desempenho de rede. Exige a compilação de esquemas `.proto`, mas gera payloads binários compactos e possui velocidade superior. Obrigatório para interoperabilidade poliglotas (Java -> Python -> Go).
* **Fastjson2:** Ótimo para integrações REST-RPC híbridas devido ao processamento ultra-veloz de strings JSON em memória.

### Governança em Tempo Real via `dubbo-admin`

O console do `dubbo-admin` permite alterar parâmetros operacionais a quente, sem redeploy ou reinicialização dos microsserviços:
- **Alteração de Load Balancing:** Mude a estratégia do Consumidor de `roundrobin` para `leastactive` dinamicamente.
- **Circuit Breaking:** Configure limites de erro para abrir o circuito e rejeitar requisições de forma rápida (*Fail-Fast*).
- **Rate Limiting:** Limite a quantidade de requisições por segundo para proteger o banco de dados do Provedor.

### Diagnóstico e Telnet via QOS (Quality of Service)

O Dubbo 3 expõe nativamente uma CLI de diagnóstico via terminal Telnet/Netcat chamada **QOS**. É possível conectar-se localmente para analisar a saúde dos nós de forma instantânea.

Para se conectar ao QOS do Provedor (porta `22222`):
```bash
# Conectando usando telnet
telnet localhost 22222

# Alternativa rápida com Netcat
nc localhost 22222
```

#### Principais Comandos de Diagnóstico:

* `ls`: Lista todos os serviços expostos e consumidos por aquela instância, incluindo versões, status de registro e protocolos utilizados.
* `online`: Registra dinamicamente a aplicação de volta ao ZooKeeper (caso tenha sido tirada temporariamente para manutenção).
* `offline`: Remove dinamicamente a instância atual do ZooKeeper. Muito útil para cenários de **Graceful Shutdown**, esvaziando o tráfego de um container antes de matá-lo.
* `help`: Exibe todos os comandos QOS disponíveis e parâmetros suportados.

---

## 🎯 Conclusão

Este ecossistema demonstra uma modelagem limpa seguindo padrões corporativos de arquitetura distribuída, provando a eficácia do **Apache Dubbo 3** em manter ambientes cloud-native estáveis, resilientes e de alta performance.
