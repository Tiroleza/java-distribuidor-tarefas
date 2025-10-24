# Sistema de Contagem Distribuída

Sistema distribuído de contagem em Java conforme especificação do trabalho de Programação Paralela e Distribuída.

## Estrutura

### Classes de Comunicação

- **Comunicado.java**: Classe base Serializable
- **Pedido.java**: Contém vetor de inteiros e número procurado, com método `contar()`
- **Resposta.java**: Contém resultado da contagem (Integer)
- **ComunicadoEncerramento.java**: Sinal de encerramento

### Programas

- **R.java**: Servidor (Receptor) - aceita conexões e processa contagens em paralelo
- **D.java**: Cliente (Distribuidor) - gera vetores e coordena contagem distribuída

### Utilitários

- **Teclado.java**: Entrada do usuário

## Como Usar

### 1. Compilar

```bash
./teste.sh
```

### 2. Executar Servidores

Em terminais separados (com limite de memória):

```bash
java -Xmx1G R 12345
java -Xmx1G R 12346
java -Xmx1G R 12347
```

### 3. Executar Cliente

```bash
java -Xmx3G D
```

**⚠️ IMPORTANTE**: Use os limites de memória (`-Xmx`) para usar 3GB de memória disponível.

### 4. Opções do Cliente

- **N**: Novo vetor (gera vetor aleatório entre -100 e 100)
- **M**: Mostrar vetor
- **S**: Sair

## Funcionamento

1. Cliente calcula tamanho máximo do vetor usando estimativa de memória
2. Gera vetor de bytes aleatórios entre -100 e 100
3. Divide vetor entre servidores
4. Cada servidor processa sua parte usando paralelismo interno
5. Cliente agrega resultados e exibe métricas de tempo
6. Sistema desconecta após cada tarefa (não mantém conexões persistentes)

## Especificações Técnicas

- **Vetor**: Bytes entre -100 e 100, tamanho calculado automaticamente
- **Paralelismo**: Usa `Runtime.getRuntime().availableProcessors()` threads
- **Comunicação**: TCP/IP com serialização de objetos usando `Parceiro`
- **Conexões**: Desconecta após cada tarefa
- **Métricas**: Tempo de geração, processamento e cada thread
- **Memória**: Estimativa automática baseada em 3GB disponível
- **Arquitetura**: Classe interna `TrabalhadoraD` para evitar OutOfMemoryError

## Conexão TCP/IP Real (Não Local)

Para testar com IPs reais em máquinas diferentes:

### 1. Descobrir IPs das Máquinas

```bash
# Linux/macOS
ifconfig

# Windows
ipconfig
```

### 2. Modificar D.java

Altere as constantes no início do arquivo:

```java
private static final String[] IPS_SERVIDORES = {"192.168.1.100", "192.168.1.101", "192.168.1.102"};
private static final int[] PORTAS_SERVIDORES = {12345, 12346, 12347};
```

**Exemplo atual no código:**

```java
private static final String[] IPS_SERVIDORES = {"localhost", "localhost", "192.168.15.3"};
```

### 3. Executar em Máquinas Diferentes

- **Máquina 1**: `java -Xmx1G R 12345`
- **Máquina 2**: `java -Xmx1G R 12346`
- **Máquina 3**: `java -Xmx1G R 12347`
- **Máquina Cliente**: `java -Xmx3G D`

### 4. Verificar Conectividade

```bash
# Testar se as portas estão abertas
telnet 192.168.1.100 12345
telnet 192.168.1.101 12346
telnet 192.168.1.102 12347
```
