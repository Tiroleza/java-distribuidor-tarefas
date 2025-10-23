# Sistema Distribuído de Contagem

Este projeto implementa um sistema distribuído de contagem em Java, onde um programa Distribuidor (D) coordena a contagem de números em um vetor grande, dividindo o trabalho entre múltiplos programas Receptores (R) que executam em paralelo.

## Estrutura do Sistema

### Classes de Comunicação

- **Comunicado**: Classe base serializável para todos os comunicados
- **Pedido**: Contém um vetor de números e o número a ser procurado
- **Resposta**: Contém o resultado da contagem
- **ComunicadoEncerramento**: Sinal para encerrar a comunicação

### Programas Principais

- **Receptor (R)**: Servidor que recebe pedidos e realiza contagens
- **Distribuidor (D)**: Cliente que coordena a contagem distribuída
- **ContadorSequencial**: Implementação sequencial para comparação
- **TesteSistema**: Testes com vetores pequenos
- **SistemaContagem**: Programa principal com menu integrado

## Como Executar

### 1. Compilação

```bash
# Compilar todas as classes
javac *.java
```

### 2. Execução

#### Opção 1: Usar o programa principal integrado

```bash
java SistemaContagem
```

#### Opção 2: Executar componentes individuais

**Para testar localmente (múltiplas instâncias do Receptor):**

```bash
# Terminal 1 - Receptor na porta 12345
java Receptor 12345

# Terminal 2 - Receptor na porta 12346 (modificar IPs no Distribuidor)
java Receptor 12346

# Terminal 3 - Distribuidor
java Distribuidor
```

**Para contagem sequencial:**

```bash
java ContadorSequencial
```

**Para testes:**

```bash
java TesteSistema
```

### 3. Configuração para Rede Local

1. Descubra os IPs das máquinas:

   - Windows: `ipconfig`
   - Linux/macOS: `ifconfig`

2. Edite o arquivo `Distribuidor.java` e modifique o array `IPS_SERVIDORES`:

```java
private static final String[] IPS_SERVIDORES = {
    "192.168.1.100",  // IP da máquina 1
    "192.168.1.101",  // IP da máquina 2
    "192.168.1.102",  // IP da máquina 3
    "192.168.1.103"   // IP da máquina 4
};
```

3. Execute o Receptor em cada máquina:

```bash
java Receptor
```

4. Execute o Distribuidor em uma das máquinas:

```bash
java Distribuidor
```

## Funcionalidades

### Interface do Usuário

- Escolha do tamanho do vetor
- Opção de exibir o vetor na tela
- Escolha do número a procurar
- Teste com número inexistente (111)
- Possibilidade de múltiplas rodadas

### Logs Informativos

- Mensagens de conexão e desconexão
- Status das threads
- Resultados das contagens
- Tempos de execução

### Tratamento de Exceções

- Captura de erros de conexão
- Tratamento de entradas inválidas
- Recuperação de falhas de comunicação

## Testes Sugeridos

1. **Teste com vetor pequeno**: Use o `TesteSistema` para verificar funcionamento
2. **Teste local**: Execute múltiplas instâncias do Receptor em portas diferentes
3. **Teste em rede**: Use máquinas diferentes na mesma rede local
4. **Comparação de performance**: Compare tempos entre sequencial e distribuído

## Exemplo de Logs

```
[R] Iniciando receptor na porta 12345
[R] Servidor ativo na porta 12345
[R] Aguardando conexão...
[R] Conexão estabelecida com 192.168.1.100
[R] Pedido recebido do cliente 192.168.1.100
[R] Pedido: procurar 50 em vetor de 2500 elementos
[R] Resposta enviada: 12 ocorrências

[D] Conectando ao servidor 192.168.1.100...
[D] Conexão estabelecida com 192.168.1.100
[D] Pedido enviado para 192.168.1.100
[D] Resposta recebida de 192.168.1.100: 12 ocorrências
```

## Requisitos

- Java 8 ou superior
- Múltiplas máquinas para teste em rede (opcional)
- Porta 12345 disponível (ou configurável)

## Observações

- O sistema usa conexões persistentes TCP/IP
- Cada servidor mantém sua conexão aberta até receber ComunicadoEncerramento
- O número de threads é limitado pelo número de processadores disponíveis
- Para teste local, modifique os IPs no Distribuidor para "localhost" e use portas diferentes
