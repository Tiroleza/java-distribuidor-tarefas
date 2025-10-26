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

### 4. Executar Contagem Sequencial (Para Comparação)

```bash
java -Xmx3G ContagemSequencial
```

**⚠️ IMPORTANTE**:

- Use os limites de memória (`-Xmx`) para usar 3GB de memória disponível
- **NUNCA** execute `java D.java` - sempre use `javac *.java` seguido de `java D`
- Isso evita LinkageError causado por conflitos de ClassLoader

### 5. Opções do Menu Interativo

- **[G]erar Vetor**: Tamanho manual (usuário define)
- **[A]uto-Tamanho**: Gera vetor com tamanho máximo calculado
- **[P]equeno**: Gera vetor de teste (20 elementos)
- **[E]xibir Vetor**: Mostra vetor atual (se ≤ 100 elementos)
- **[C]ontar**: Conta número aleatório do vetor atual
- **[Z]ero**: Conta número inexistente '111' (deve retornar 0)
- **[T]erminar**: Encerra o programa

## Funcionamento

1. **Menu Interativo**: Cliente apresenta menu com opções de geração e contagem
2. **Geração de Vetor**: Usuário pode gerar vetor manual, automático ou pequeno
3. **Contagem Distribuída**: Divide vetor entre servidores para processamento paralelo
4. **Métricas**: Exibe tempo de processamento e resultados de cada thread
5. **Conexões Persistentes**: Servidores mantêm conexões abertas para múltiplas operações
6. **Testes Obrigatórios**: Suporte para vetores pequenos e contagem de números inexistentes

## Especificações Técnicas

- **Vetor**: Bytes entre -100 e 100, tamanho calculado automaticamente
- **Paralelismo**: Usa `Runtime.getRuntime().availableProcessors()` threads
- **Comunicação**: TCP/IP com serialização de objetos usando `Parceiro`
- **Métricas**: Tempo de geração, processamento e cada thread
- **Memória**: Estimativa automática baseada em 3GB disponível
- **Arquitetura**: Classes de thread separadas seguindo padrão de referência
- **Conexões**: Servidores mantêm conexões persistentes com loop `for(;;)`
- **Sincronização**: Semaphore protege seção crítica de cópia de memória

## Arquitetura Refatorada (Padrão de Referência)

### Servidor (R.java)

- **AceitadoraDeConexaoR.java**: Thread dedicada para `serverSocket.accept()`
- **SupervisoraDeConexaoR.java**: Thread dedicada para comunicação com cada cliente
- **R.java**: Apenas inicia `AceitadoraDeConexaoR` (padrão do Servidor.java)

### Cliente (D.java)

- **TrabalhadoraD.java**: Classe de thread separada (não mais interna)
- **D.java**: Menu interativo e coordenação das threads
- **Semaphore**: Protege seção crítica de alocação de memória

## Comparação de Performance

Para comparar a performance entre distribuição e processamento sequencial:

### 1. Teste com Sistema Distribuído

```bash
# Terminal 1: Servidores
java -Xmx1G R 12345
java -Xmx1G R 12346
java -Xmx1G R 12347

# Terminal 2: Cliente Distribuído
java -Xmx3G D
# Use opção [A] para gerar vetor máximo
# Use opção [C] para contar número aleatório
```

### 2. Teste com Sistema Sequencial

```bash
# Terminal: Contagem Sequencial
java -Xmx3G ContagemSequencial
# Use opção [A] para gerar vetor máximo
# Use opção [C] para contar número aleatório
```

### 3. Análise dos Resultados

Compare os tempos de processamento:

- **Distribuído**: Tempo total + tempo de cada thread
- **Sequencial**: Tempo total em uma única thread

O sistema distribuído deve ser mais rápido para vetores grandes devido ao paralelismo.

### 4. Exemplo de Comparação

```bash
# Sistema Distribuído (3 servidores + cliente)
[D] ✓ Contagem final: 1500000
[D] 📊 MÉTRICAS DE TEMPO:
[D]   • Tempo total de processamento: 2500ms
[D]   • Thread 0: 800ms
[D]   • Thread 1: 750ms
[D]   • Thread 2: 900ms

# Sistema Sequencial (1 thread)
[SEQ] ✓ Contagem final: 1500000
[SEQ] 📊 MÉTRICAS DE TEMPO (SEQUENCIAL):
[SEQ]   • Tempo total de processamento: 4500ms
```

**Resultado**: Sistema distribuído foi ~1.8x mais rápido!

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
