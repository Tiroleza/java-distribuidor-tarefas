# Sistema Distribuído de Contagem - Java

Este repositório contém duas versões completas do sistema distribuído de contagem conforme especificado no documento:

## Estrutura do Repositório

```
├── Cliente/           # Versão principal (documento oficial)
│   ├── Comunicado.java
│   ├── Pedido.java
│   ├── Resposta.java
│   ├── ComunicadoEncerramento.java
│   └── Distribuidor.java
├── Servidor/          # Versão principal (documento oficial)
│   ├── Comunicado.java
│   ├── Pedido.java
│   ├── Resposta.java
│   ├── ComunicadoEncerramento.java
│   └── Receptor.java
└── Testes/            # Versão para testes locais
    ├── Comunicado.java
    ├── Pedido.java
    ├── Resposta.java
    ├── ComunicadoEncerramento.java
    ├── ReceptorLocal.java      # 4 portas locais
    ├── DistribuidorLocal.java  # Vetor menor (metade do estimado)
    ├── ClienteServidorLocal.java # Teste automático
    └── MaiorVetorAproximado.java # Estimativa de tamanho máximo
```

## Versão Principal (Cliente/Servidor)

### Execução em Rede Local

1. **Descobrir IPs das máquinas:**

   ```bash
   # Windows
   ipconfig

   # Linux/macOS
   ifconfig
   ```

2. **Configurar IPs no Distribuidor:**
   Editar `Cliente/Distribuidor.java`:

   ```java
   private static final String[] IPS = {
       "192.168.1.100",  // IP da máquina 1
       "192.168.1.101",  // IP da máquina 2
       "192.168.1.102",  // IP da máquina 3
       "192.168.1.103"   // IP da máquina 4
   };
   ```

3. **Executar:**

   ```bash
   # Em cada máquina servidor
   cd Servidor
   javac *.java
   java Receptor

   # Na máquina cliente
   cd Cliente
   javac *.java
   java Distribuidor
   ```

## Versão de Testes Locais

### Teste Rápido (Recomendado)

```bash
cd Testes
javac *.java
java ClienteServidorLocal
```

### Teste Completo com Interface

```bash
cd Testes
javac *.java
java ReceptorLocal &    # Inicia 4 receptores em portas 12345-12348
java DistribuidorLocal  # Executa distribuidor
```

### Estimar Tamanho Máximo do Vetor

```bash
cd Testes
javac MaiorVetorAproximado.java
java -Xmx4G MaiorVetorAproximado
```

## Funcionalidades Implementadas

### ✅ Requisitos Obrigatórios

- Classes de comunicação serializáveis
- Programa R (Receptor) com ServerSocket e conexões persistentes
- Programa D (Distribuidor) com threads e Thread.join()
- Comunicação TCP/IP com serialização
- Encerramento com ComunicadoEncerramento

### ✅ Boas Práticas

- Tratamento de exceções adequado
- Interface do usuário (tamanho do vetor, exibição, número a procurar)
- Logs informativos
- Testes com vetores pequenos
- Comparação de performance (sequencial vs distribuído)
- Estimativa de tamanho máximo do vetor

### ✅ Funcionalidades Extras

- Versão de testes locais com 4 portas
- Teste automático completo
- Scripts de execução
- Documentação completa

## Exemplos de Logs

### Servidor (Receptor)

```
[R] Iniciando receptor na porta 12345
[R] Aguardando conexão...
[R] Conexão de 192.168.1.100
[R] Pedido recebido. Contagem=1250
[R] Comunicado de encerramento recebido. Fechando conexão.
```

### Cliente (Distribuidor)

```
=== DISTRIBUIDOR ===
Tamanho do vetor (ex.: 1000000): 1000000
Deseja exibir o vetor? (s/n): n
[D] Número a contar (posição 456789): 42
[D] Contagem total: 5023
```

### Testes Locais

```
=== TESTE AUTOMÁTICO LOCAL ===
[TESTE] Tamanho escolhido (metade): 500,000
[TESTE] Número escolhido do vetor: 15 (posição 123456)
[TESTE] Total contado: 2500
[TESTE] Sequencial: 2500 em 2 ms
[TESTE] Verificação: ✓ CORRETO
```

## Troubleshooting

### Problemas Comuns

1. **Erro de Compilação**: Verificar se todas as classes estão no classpath
2. **Erro de Conexão**: Verificar se os servidores estão rodando e portas disponíveis
3. **Erro de Serialização**: Verificar se as classes têm serialVersionUID idêntico

### Para Teste Local

- Use a pasta `Testes/` com `ReceptorLocal` e `DistribuidorLocal`
- Execute `ClienteServidorLocal` para teste automático completo

### Para Rede Local

- Configure os IPs corretamente no `Distribuidor.java`
- Execute `Receptor` em cada máquina servidor
- Execute `Distribuidor` na máquina cliente

## Performance

O sistema distribuído pode ser mais lento que o sequencial devido ao overhead de rede. Em ambiente real com múltiplos servidores físicos, a performance melhora significativamente.

Para demonstração, use a versão de testes locais que mostra claramente o funcionamento do sistema distribuído.
