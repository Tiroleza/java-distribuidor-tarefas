# 📡 Sistema de Contagem Distribuída em Java

> Sistema distribuído de contagem paralela que demonstra concorrência, sincronização via semáforos, comunicação cliente-servidor via sockets TCP e distribuição de carga em múltiplos nós — implementado integralmente em Java puro.

---

## 📋 Sumário

- [Visão Geral](#-visão-geral)
- [Arquitetura](#-arquitetura)
- [Estrutura do Projeto](#-estrutura-do-projeto)
- [Protocolo de Comunicação](#-protocolo-de-comunicação)
- [Concorrência e Sincronização](#-concorrência-e-sincronização)
- [Compilação e Execução](#-compilação-e-execução)
- [Menu Interativo](#-menu-interativo)
- [Benchmark: Distribuído vs Sequencial](#-benchmark-distribuído-vs-sequencial)
- [Tecnologias e Conceitos](#-tecnologias-e-conceitos)

---

## 🔍 Visão Geral

O problema resolvido é simples: **contar quantas vezes um número aparece em um vetor de bytes**. A complexidade está na **escala** e na **estratégia de distribuição**.

O sistema gera vetores massivos (até ~2 GB), divide-os entre múltiplos servidores via rede TCP e consolida os resultados parciais — tudo com paralelismo em dois níveis:

1. **Nível 1 — Distribuição de rede:** o cliente (`D`) divide o vetor entre N servidores (`R`) em paralelo.
2. **Nível 2 — Paralelismo local:** cada servidor `R` subdivide seu segmento entre todas as CPUs disponíveis usando threads `Contadora`.

Um modo **sequencial** (`ContagemSequencial`) serve como baseline para medir o ganho real da distribuição.

---

## 🏗️ Arquitetura

```
┌─────────────────────────────────────────────────────────────┐
│                     CLIENTE (D.java)                        │
│                                                             │
│  ┌─────────┐   ┌──────────────┐   ┌──────────────┐         │
│  │  Menu    │──▶│ Gera vetor   │──▶│ Divide vetor │         │
│  │ (stdin)  │   │ byte[] até   │   │ em N partes  │         │
│  └─────────┘   │ ~2 GB        │   └──────┬───────┘         │
│                └──────────────┘          │                  │
│                                          ▼                  │
│  ┌──────────────┐ ┌──────────────┐ ┌──────────────┐        │
│  │TrabalhadoraD │ │TrabalhadoraD │ │TrabalhadoraD │        │
│  │  (Thread 0)  │ │  (Thread 1)  │ │  (Thread 2)  │        │
│  └──────┬───────┘ └──────┬───────┘ └──────┬───────┘        │
│         │                │                │                 │
│    Semáforo de     Semáforo de      Semáforo de             │
│    alocação        alocação         alocação                │
└─────────┼────────────────┼────────────────┼─────────────────┘
          │ TCP            │ TCP            │ TCP
          ▼                ▼                ▼
┌─────────────┐  ┌─────────────┐  ┌─────────────┐
│ SERVIDOR R  │  │ SERVIDOR R  │  │ SERVIDOR R  │
│ porta 12345 │  │ porta 12346 │  │ porta 12347 │
│             │  │             │  │             │
│ ┌─────────┐ │  │ ┌─────────┐ │  │ ┌─────────┐ │
│ │Contadora│ │  │ │Contadora│ │  │ │Contadora│ │
│ │ x CPUs  │ │  │ │ x CPUs  │ │  │ │ x CPUs  │ │
│ └─────────┘ │  │ └─────────┘ │  │ └─────────┘ │
└─────────────┘  └─────────────┘  └─────────────┘
```

---

## 📁 Estrutura do Projeto

```
main/
├── D.java                      # Cliente distribuidor (ponto de entrada)
├── R.java                      # Servidor receptor (ponto de entrada)
├── ContagemSequencial.java     # Benchmark sequencial (baseline)
│
├── TrabalhadoraD.java          # Thread worker do cliente
├── SupervisoraDeConexaoR.java  # Thread que gerencia cada conexão no servidor
├── AceitadoraDeConexaoR.java   # Thread que aceita novas conexões TCP
├── Contadora.java              # Thread worker do servidor (contagem local)
│
├── Parceiro.java               # Comunicação bidirecional thread-safe via sockets
├── Comunicado.java             # Interface base do protocolo (Serializable)
├── Pedido.java                 # Mensagem: vetor + número procurado
├── Resposta.java               # Mensagem: resultado da contagem
├── ComunicadoEncerramento.java # Mensagem: sinal de encerramento
│
└── Teclado.java                # Utilitário de leitura do stdin
```

### Papel de Cada Classe

| Classe | Tipo | Responsabilidade |
|--------|------|------------------|
| `D` | Cliente (main) | Menu interativo, geração de vetor, coordenação de threads `TrabalhadoraD` |
| `R` | Servidor (main) | Escuta em porta TCP, delega conexões para `SupervisoraDeConexaoR` |
| `ContagemSequencial` | Benchmark (main) | Contagem local single-thread para comparação de performance |
| `TrabalhadoraD` | Thread (cliente) | Copia segmento do vetor, envia ao servidor, recebe resultado parcial |
| `SupervisoraDeConexaoR` | Thread (servidor) | Gerencia ciclo de vida de uma conexão; recebe pedidos, processa, responde |
| `AceitadoraDeConexaoR` | Thread (servidor) | `ServerSocket.accept()` em loop, cria `SupervisoraDeConexaoR` por conexão |
| `Contadora` | Thread (servidor) | Conta ocorrências em sub-vetor local, retorna resultado parcial |
| `Parceiro` | Comunicação | Encapsula `Socket` + `ObjectStreams` com mutex via `Semaphore` |
| `Comunicado` | Protocolo | Classe base `Serializable` para mensagens do protocolo |
| `Pedido` | Protocolo | Transporta `byte[]` + `int procurado` |
| `Resposta` | Protocolo | Transporta `int contagem` (resultado) |
| `ComunicadoEncerramento` | Protocolo | Sinal de encerramento gracioso |
| `Teclado` | Utilitário | Leitura tipada do `System.in` |

---

## 📡 Protocolo de Comunicação

A comunicação usa **serialização Java** (`ObjectOutputStream` / `ObjectInputStream`) sobre **TCP**.

```
Cliente (D)                          Servidor (R)
    │                                      │
    │──── Pedido(byte[], procurado) ──────▶│
    │                                      │  processa com
    │                                      │  N threads Contadora
    │◀──── Resposta(contagem) ─────────────│
    │                                      │
    │  ... (loop: múltiplos pedidos) ...   │
    │                                      │
    │──── ComunicadoEncerramento ─────────▶│
    │                                      │  fecha conexão
```

Hierarquia de classes do protocolo:

```
Comunicado (Serializable, Cloneable)
├── Pedido              → byte[] numeros, int procurado
├── Resposta            → int contagem
└── ComunicadoEncerramento  → (sem dados, apenas sinal)
```

---

## 🔒 Concorrência e Sincronização

O sistema implementa dois mecanismos de sincronização distintos:

### 1. Semáforo de Alocação (`D.java` → `TrabalhadoraD`)

```java
private static Semaphore semaforoCopia = new Semaphore(1, true);
```

**Problema:** múltiplas `TrabalhadoraD` fazendo `Arrays.copyOfRange()` simultaneamente causam `OutOfMemoryError` por pico de consumo de memória.

**Solução:** semáforo justo (`fair=true`) serializa as cópias — apenas uma thread aloca memória por vez. A transmissão de rede ocorre **fora** da seção crítica, preservando o paralelismo real.

### 2. Mutex do Buffer (`Parceiro.java`)

```java
private final Semaphore mutEx = new Semaphore(1, true);
```

**Problema:** `espie()` e `envie()` podem ser chamados de threads diferentes, causando race condition no buffer `proximoComunicado`.

**Solução:** mutex com `acquireUninterruptibly()` protege o acesso ao buffer, garantindo que leitura e consumo sejam atômicos.

---

## 🚀 Compilação e Execução

### Pré-requisitos

- **Java JDK 8+** (utiliza apenas APIs nativas — `java.net`, `java.io`, `java.util.concurrent`)
- Nenhuma dependência externa

### Compilação

```bash
cd main
javac *.java
```

### Execução

São necessários **4 terminais** para o sistema distribuído completo:

#### Terminal 1 — Servidor R (porta 12345)
```bash
java -Xmx1G R 12345
```

#### Terminal 2 — Servidor R (porta 12346)
```bash
java -Xmx1G R 12346
```

#### Terminal 3 — Servidor R (porta 12347)
```bash
java -Xmx1G R 12347
```

#### Terminal 4 — Cliente D (Distribuidor)
```bash
java -Xmx5G D
```

#### Terminal 5 — Benchmark Sequencial (opcional)
```bash
java -Xmx3G ContagemSequencial
```

> **Nota:** os flags `-Xmx` controlam a memória heap máxima da JVM. Ajuste conforme sua máquina. O cliente precisa de mais memória pois mantém o vetor original completo.

---

## 🎮 Menu Interativo

Tanto o cliente `D` quanto o `ContagemSequencial` compartilham o mesmo menu:

```
--- MENU DO DISTRIBUIDOR ---
[G]erar Vetor (Tamanho Manual)
[A]uto-Tamanho (Gerar Vetor Máximo)
[P]equeno (Gerar Vetor de Teste, 20 elementos)
[E]xibir Vetor Atual
[C]ontar (Número Aleatório do Vetor)
[Z]ero (Contar Número Inexistente '111')
[T]erminar
```

| Opção | Descrição |
|-------|-----------|
| **G** | Gera vetor de tamanho informado pelo usuário (bytes aleatórios entre -100 e 100) |
| **A** | Calcula automaticamente o maior vetor que cabe na memória disponível |
| **P** | Gera vetor pequeno de 20 elementos (útil para verificação visual) |
| **E** | Exibe conteúdo do vetor (limitado a vetores ≤ 100 elementos) |
| **C** | Seleciona um número aleatório do vetor e conta suas ocorrências |
| **Z** | Conta ocorrências do número 111 (fora do range -100 a 100, resultado esperado: 0) |
| **T** | Encerra o programa e fecha conexões |

---

## 📊 Benchmark: Distribuído vs Sequencial

O sistema inclui `ContagemSequencial.java` como baseline. Para comparar:

1. Gere o mesmo tamanho de vetor em ambos os modos
2. Use `[C]` para contar e compare os tempos

**O que medir:**
- Tempo total de processamento (inclui overhead de rede no modo distribuído)
- Tempo individual de cada thread
- Escalabilidade ao adicionar/remover servidores

**Comportamento esperado:**
- Vetores pequenos: sequencial mais rápido (overhead de rede > ganho de paralelismo)
- Vetores grandes: distribuído mais rápido (paralelismo supera overhead)

---

## 🛠️ Tecnologias e Conceitos

- **Java SE** — sem frameworks externos
- **Sockets TCP** — `java.net.Socket` / `ServerSocket`
- **Serialização Java** — `ObjectOutputStream` / `ObjectInputStream`
- **Threads** — `java.lang.Thread` com `start()` / `join()`
- **Semáforos** — `java.util.concurrent.Semaphore` (mutex justo)
- **Modelo Cliente-Servidor** — múltiplos servidores, um cliente coordenador
- **Divisão e Conquista** — particionamento do vetor em segmentos
- **Paralelismo em dois níveis** — distribuição de rede + multithread local

---

## 📄 Licença

Este projeto foi desenvolvido como estudo prático de sistemas distribuídos e programação concorrente em Java.
