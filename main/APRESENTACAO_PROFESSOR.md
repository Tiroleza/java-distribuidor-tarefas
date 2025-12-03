# Apresentação: Escalonamento de Threads Mergeadoras

## 🎯 O QUE FOI CORRIGIDO

### Problema Anterior
- Merge era feito **sequencialmente** (um por vez)
- Não aproveitava o paralelismo disponível
- Não escalava para muitos servidores R

### Solução Implementada
- **Threads mergeadoras** fazem merge de 2 em 2 em **paralelo**
- **Múltiplas rodadas** quando há muitos vetores
- **Escalável** para qualquer quantidade de R's (2, 3, 500, 1000...)

---

## 📊 COMO FUNCIONA AGORA

### No Receptor (R)

**Antes:**
```
Thread 1 ordena → Thread 2 ordena → ... → Thread N ordena
Depois: merge sequencial (1 por vez)
```

**Agora:**
```
Thread 1 ordena → Thread 2 ordena → ... → Thread N ordena
Depois: 
  Rodada 1: Threads mergeadoras fazem merge de 2 em 2 (paralelo)
  Rodada 2: Threads mergeadoras fazem merge de 2 em 2 (paralelo)
  ...
  Rodada N: Resultado final (1 vetor)
```

**Exemplo prático:**
- 8 threads ordenadoras terminam → 8 vetores ordenados
- **Rodada 1**: 4 threads mergeadoras fazem merge de 2 em 2 → 4 vetores
- **Rodada 2**: 2 threads mergeadoras fazem merge de 2 em 2 → 2 vetores  
- **Rodada 3**: 1 thread mergeadora faz merge → **1 vetor final**

### No Distribuidor (D)

**Antes:**
```
Recebe vetor do R1 → merge sequencial
Recebe vetor do R2 → merge sequencial
Recebe vetor do R3 → merge sequencial
...
```

**Agora:**
```
Recebe vetores de todos os R's
Rodada 1: Threads mergeadoras fazem merge de 2 em 2 (paralelo)
Rodada 2: Threads mergeadoras fazem merge de 2 em 2 (paralelo)
...
Rodada N: Resultado final (1 vetor)
```

**Exemplo prático com 10 servidores R:**
- Recebe 10 vetores ordenados
- **Rodada 1**: 4 threads mergeadoras (limitado por processadores) → 5 vetores
- **Rodada 2**: 4 threads mergeadoras → 3 vetores
- **Rodada 3**: 2 threads mergeadoras → 2 vetores
- **Rodada 4**: 1 thread mergeadora → **1 vetor final**

---

## 🔑 PONTOS-CHAVE PARA DEMONSTRAÇÃO

### 1. **Threads Mergeadoras em Paralelo**
- Não é mais sequencial (um por vez)
- Várias threads fazem merge simultaneamente
- Limitado pelo número de processadores

### 2. **Múltiplas Rodadas**
- Quando há muitos vetores, precisa de várias rodadas
- Cada rodada reduz pela metade (aproximadamente)
- Continua até ter apenas 1 vetor

### 3. **Escalabilidade**
- Funciona com 2 servidores R
- Funciona com 10 servidores R
- Funciona com 500 servidores R
- Funciona com 1000 servidores R
- **Não pressupõe número fixo!**

---

## 💻 COMO DEMONSTRAR

### Demonstração 1: Ver os Logs
Execute o programa e observe os logs:

```
[R] Rodada 1 de merge: 8 vetores restantes
[R] Rodada 2 de merge: 4 vetores restantes
[R] Rodada 3 de merge: 2 vetores restantes
[R] Rodada 4 de merge: 1 vetor restante
```

### Demonstração 2: Mostrar o Código
Mostre a classe `Mergeadora.java`:
- É uma Thread
- Faz merge de 2 vetores ordenados
- Usada tanto no R quanto no D

Mostre o loop de múltiplas rodadas em `SupervisoraDeConexaoR.java`:
```java
while (vetoresParaMerge.size() > 1) {
    // Cria lote de threads mergeadoras (máximo = processadores)
    // Processa em paralelo
    // Aguarda todas terminarem
    // Próxima rodada
}
```

### Demonstração 3: Testar com Diferentes Quantidades
1. Teste com 2 servidores R
2. Teste com 3 servidores R
3. Mostre que funciona com qualquer quantidade

---

## 📝 FRASES-CHAVE PARA FALAR

1. **"Implementei threads mergeadoras que fazem merge de 2 em 2 em paralelo"**

2. **"Quando há muitos vetores, o sistema faz múltiplas rodadas de merge até reduzir a um único vetor"**

3. **"O número máximo de threads mergeadoras simultâneas é limitado pelo número de processadores da máquina"**

4. **"O sistema é escalável e funciona com qualquer quantidade de servidores R, não pressupõe um número fixo"**

5. **"Tanto no Receptor quanto no Distribuidor, o merge é feito usando threads mergeadoras em múltiplas rodadas"**

---

## 🎬 ROTEIRO DE APRESENTAÇÃO (2-3 minutos)

1. **Problema anterior** (30s)
   - "Antes o merge era sequencial, um por vez"

2. **Solução implementada** (1min)
   - "Agora uso threads mergeadoras que fazem merge de 2 em 2 em paralelo"
   - "Quando há muitos vetores, faço múltiplas rodadas"
   - "Escalável para qualquer quantidade de R's"

3. **Demonstração prática** (1min)
   - Mostrar logs durante execução
   - Mostrar código da classe Mergeadora
   - Mostrar loop de múltiplas rodadas

4. **Conclusão** (30s)
   - "Agora está exatamente como o senhor pediu: threads mergeadoras em múltiplas rodadas, escalável para qualquer quantidade de servidores"

---

## ✅ CHECKLIST ANTES DA APRESENTAÇÃO

- [ ] Compilar todos os arquivos (`javac *.java`)
- [ ] Testar localmente com 2-3 instâncias do R
- [ ] Verificar que os logs mostram "Rodada X de merge"
- [ ] Ter o código aberto para mostrar a classe Mergeadora
- [ ] Ter o código aberto para mostrar o loop de múltiplas rodadas
- [ ] Preparar para explicar a diferença entre antes e agora

