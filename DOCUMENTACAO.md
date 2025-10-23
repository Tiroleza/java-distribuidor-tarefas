# Sistema Distribuído de Contagem - Documentação Completa

## Visão Geral

Este projeto implementa um sistema distribuído de contagem em Java conforme especificado no documento de requisitos. O sistema é composto por:

- **Distribuidor (D)**: Cliente que coordena a contagem distribuída
- **Receptor (R)**: Servidor que realiza contagens em paralelo
- **Classes de Comunicação**: Pedido, Resposta, ComunicadoEncerramento
- **Programas Auxiliares**: Contagem sequencial, testes, interface integrada

## Estrutura de Arquivos

```
codigo-ref/
├── Cliente/
│   ├── Comunicado.java                    # Classe base serializável
│   ├── Pedido.java                        # Contém vetor e número a procurar
│   ├── Resposta.java                      # Contém resultado da contagem
│   ├── ComunicadoEncerramento.java        # Sinal de encerramento
│   ├── Distribuidor.java                  # Programa principal (cliente)
│   ├── DistribuidorLocal.java             # Versão para teste local
│   ├── ContadorSequencial.java            # Contagem sequencial
│   ├── TesteSistema.java                  # Testes com vetores pequenos
│   ├── SistemaContagem.java               # Interface integrada
│   └── Parceiro.java                      # Classe de comunicação
└── Servidor/
    ├── Comunicado.java                    # Classe base serializável
    ├── Pedido.java                        # Contém vetor e número a procurar
    ├── Resposta.java                      # Contém resultado da contagem
    ├── ComunicadoEncerramento.java        # Sinal de encerramento
    ├── Receptor.java                      # Programa principal (servidor)
    └── Parceiro.java                      # Classe de comunicação
```

## Como Executar

### Opção 1: Script Automatizado

```bash
./executar.sh
```

### Opção 2: Teste Local Completo

```bash
./teste-local.sh
```

### Opção 3: Execução Manual

#### 1. Compilação

```bash
# Compilar classes do servidor
cd codigo-ref/Servidor
javac *.java

# Compilar classes do cliente
cd ../Cliente
javac *.java
```

#### 2. Execução

**Para teste local (múltiplas instâncias):**

```bash
# Terminal 1 - Receptor na porta 12345
cd codigo-ref/Servidor
java Receptor 12345

# Terminal 2 - Receptor na porta 12346
java Receptor 12346

# Terminal 3 - Receptor na porta 12347
java Receptor 12347

# Terminal 4 - Receptor na porta 12348
java Receptor 12348

# Terminal 5 - DistribuidorLocal
cd ../Cliente
java DistribuidorLocal
```

**Para teste em rede:**

```bash
# Em cada máquina servidor
cd codigo-ref/Servidor
java Receptor

# Na máquina cliente
cd codigo-ref/Cliente
java Distribuidor
```

## Funcionalidades Implementadas

### ✅ Requisitos Obrigatórios

1. **Classes de Comunicação**

   - `Comunicado`: Classe base serializável
   - `Pedido`: Vetor de números + número procurado + método `contar()`
   - `Resposta`: Resultado da contagem
   - `ComunicadoEncerramento`: Sinal de término

2. **Programa R (Receptor)**

   - ServerSocket na porta 12345 (configurável)
   - Aceita conexões persistentes
   - Processa pedidos em threads separadas
   - Executa método `contar()` do Pedido
   - Envia Resposta com resultado
   - Encerra conexão ao receber ComunicadoEncerramento

3. **Programa D (Distribuidor)**
   - IPs hard-coded dos servidores
   - Gera vetor grande de números aleatórios (-100 a 100)
   - Divide vetor em partes iguais
   - Cria thread para cada servidor
   - Mantém conexões persistentes
   - Soma resultados de todas as threads
   - Usa `Thread.join()` para sincronização

### ✅ Boas Práticas

1. **Tratamento de Exceções**

   - Captura de IOException, ClassNotFoundException
   - Tratamento de erros de conexão
   - Recuperação de falhas de comunicação

2. **Interface do Usuário**

   - Escolha do tamanho do vetor
   - Opção de exibir vetor na tela
   - Escolha do número a procurar
   - Teste com número inexistente (111)
   - Múltiplas rodadas

3. **Logs Informativos**

   - Mensagens de conexão/desconexão
   - Status das threads
   - Resultados das contagens
   - Tempos de execução

4. **Testes**
   - Programa de teste com vetores pequenos
   - Comparação de performance
   - Teste local com múltiplas instâncias

### ✅ Funcionalidades Extras

1. **Programa Integrado**

   - Menu principal com todas as opções
   - Interface unificada

2. **Contagem Sequencial**

   - Implementação para comparação de performance
   - Medição de tempos

3. **Scripts de Automação**

   - Script de execução principal
   - Script de teste local
   - Compilação automática

4. **Documentação**
   - README detalhado
   - Instruções de uso
   - Exemplos de logs

## Configuração para Rede Local

### 1. Descobrir IPs

```bash
# Windows
ipconfig

# Linux/macOS
ifconfig
```

### 2. Configurar IPs no Distribuidor

Editar `codigo-ref/Cliente/Distribuidor.java`:

```java
private static final String[] IPS_SERVIDORES = {
    "192.168.1.100",  // IP da máquina 1
    "192.168.1.101",  // IP da máquina 2
    "192.168.1.102",  // IP da máquina 3
    "192.168.1.103"   // IP da máquina 4
};
```

### 3. Executar

```bash
# Em cada máquina servidor
java Receptor

# Na máquina cliente
java Distribuidor
```

## Exemplos de Uso

### Teste Básico

```bash
# Executar teste do sistema
java TesteSistema
```

### Contagem Sequencial

```bash
# Executar contagem sequencial
java ContadorSequencial
```

### Sistema Completo

```bash
# Executar sistema integrado
java SistemaContagem
```

## Logs de Exemplo

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

## Troubleshooting

### Problemas Comuns

1. **Erro de Compilação**

   - Verificar se todas as classes estão no classpath
   - Compilar na ordem correta (Servidor primeiro, depois Cliente)

2. **Erro de Conexão**

   - Verificar se os servidores estão rodando
   - Verificar se as portas estão disponíveis
   - Verificar firewall/antivírus

3. **Erro de Serialização**
   - Verificar se todas as classes implementam Serializable
   - Verificar se as classes estão no classpath de ambos os programas

### Soluções

1. **Para Teste Local**

   - Use `DistribuidorLocal.java` em vez de `Distribuidor.java`
   - Execute múltiplas instâncias do Receptor em portas diferentes

2. **Para Rede Local**
   - Configure os IPs corretamente no Distribuidor
   - Execute o Receptor em cada máquina servidor
   - Verifique conectividade de rede

## Performance

### Fatores que Afetam Performance

1. **Número de Servidores**: Mais servidores = melhor paralelização
2. **Tamanho do Vetor**: Vetores maiores mostram melhor diferença
3. **Latência de Rede**: Afeta tempo total de execução
4. **Número de Processadores**: Limita paralelização real

### Estimativas Teóricas

- Com 4 servidores e 4 processadores: ~4x mais rápido que sequencial
- Com 8 servidores e 8 processadores: ~8x mais rápido que sequencial
- Overhead de rede reduz ganho real

## Conclusão

O sistema implementa completamente todos os requisitos especificados:

- ✅ Classes de comunicação conforme especificação
- ✅ Programa R (Receptor) com ServerSocket e threads
- ✅ Programa D (Distribuidor) com paralelização
- ✅ Tratamento de exceções adequado
- ✅ Interface do usuário completa
- ✅ Logs informativos
- ✅ Testes e comparação de performance
- ✅ Documentação completa

O sistema está pronto para demonstração e uso em ambiente de produção.
