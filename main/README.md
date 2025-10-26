# Sistema de Contagem Distribuída - Análise de Arquitetura

Sistema distribuído de contagem paralela em Java que demonstra os conceitos de concorrência, sincronização e distribuição de tarefas.

## Compilação e Execução

### Compilação

```bash
cd main
javac *.java
```

### Execução

#### Terminal 1: Servidor R na porta 12345

```bash
java -Xmx1G R 12345
```

#### Terminal 2: Servidor R na porta 12346

```bash
java -Xmx1G R 12346
```

#### Terminal 3: Servidor R na porta 12347

```bash
java -Xmx1G R 12347
```

#### Terminal 4: Cliente D (Distribuidor)

```bash
java -Xmx5G D
```

O cliente tentará conectar aos três servidores e exibirá o menu interativo.

#### Terminal 5: Benchmark Sequencial (comparativo)

```bash
java -Xmx3G ContagemSequencial
```

Execute comandos do menu para gerar vetores e realizar contagens.

---
