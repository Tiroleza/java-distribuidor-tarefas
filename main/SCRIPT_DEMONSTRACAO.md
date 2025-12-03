# Script de Demonstração para o Professor

## 🎯 OBJETIVO
Demonstrar que o escalonamento de threads mergeadoras foi implementado corretamente.

---

## 📋 PREPARAÇÃO (Antes da Aula)

### 1. Compilar tudo
```bash
cd main
javac *.java
```

### 2. Preparar 3 terminais
- Terminal 1: Executar R na porta 12345
- Terminal 2: Executar R na porta 12346  
- Terminal 3: Executar D

### 3. Ter o código aberto
- `Mergeadora.java` - para mostrar a thread mergeadora
- `SupervisoraDeConexaoR.java` - para mostrar o loop de múltiplas rodadas
- `D.java` - para mostrar o merge escalável no distribuidor

---

## 🎬 DEMONSTRAÇÃO PASSO A PASSO

### PASSO 1: Explicar o Problema Anterior (30 segundos)

**Falar:**
> "Professor, na versão anterior, o merge era feito sequencialmente. Depois que as threads ordenadoras terminavam, eu fazia merge de um vetor por vez, não aproveitava o paralelismo."

**Mostrar (se tiver código antigo):**
- Código que fazia merge sequencial (um por vez)

---

### PASSO 2: Mostrar a Solução - Classe Mergeadora (1 minuto)

**Falar:**
> "Agora implementei threads mergeadoras. Esta é a classe Mergeadora que faz merge de dois vetores ordenados em paralelo."

**Mostrar código `Mergeadora.java`:**
```java
public class Mergeadora extends Thread {
    private byte[] vetorA;
    private byte[] vetorB;
    private byte[] resultado;
    
    @Override
    public void run() {
        // Faz merge de 2 vetores ordenados
        this.resultado = intercalar(this.vetorA, this.vetorB);
    }
}
```

**Falar:**
> "Cada thread mergeadora recebe dois vetores ordenados e faz o merge deles. Várias dessas threads podem trabalhar em paralelo."

---

### PASSO 3: Mostrar Múltiplas Rodadas no Receptor (1 minuto)

**Falar:**
> "No Receptor, depois que as threads ordenadoras terminam, eu faço múltiplas rodadas de merge usando threads mergeadoras."

**Mostrar código em `SupervisoraDeConexaoR.java` (linhas ~108-150):**
```java
// Faz múltiplas rodadas de merge usando threads mergeadoras
int rodada = 1;
while (vetoresParaMerge.size() > 1) {
    System.out.println("[R] Rodada " + rodada + " de merge: " + 
                      vetoresParaMerge.size() + " vetores restantes");
    
    // Cria lote de threads mergeadoras (máximo = processadores)
    ArrayList<Mergeadora> loteAtual = new ArrayList<>();
    while (indice < vetoresParaMerge.size() && 
           loteAtual.size() < qtdProcessadores) {
        // Cria thread mergeadora para fazer merge de 2 vetores
        Mergeadora mergeadora = new Mergeadora(...);
        mergeadora.start();
        loteAtual.add(mergeadora);
    }
    
    // Aguarda todas terminarem
    for (Mergeadora m : loteAtual) {
        m.join();
        proximaRodada.add(m.getResultado());
    }
    
    rodada++;
}
```

**Falar:**
> "Veja que eu faço um loop enquanto há mais de um vetor. Em cada rodada, crio threads mergeadoras limitadas pelo número de processadores. Elas fazem merge de 2 em 2 em paralelo. Quando todas terminam, passo para a próxima rodada até ter apenas um vetor."

---

### PASSO 4: Mostrar Escalabilidade no Distribuidor (1 minuto)

**Falar:**
> "No Distribuidor, a mesma lógica. Recebo vetores ordenados de todos os servidores R, e faço múltiplas rodadas de merge usando threads mergeadoras."

**Mostrar código em `D.java` (linhas ~140-188):**
```java
// Faz múltiplas rodadas de merge usando threads mergeadoras
int qtdProcessadores = Runtime.getRuntime().availableProcessors();
int rodada = 1;

while (vetoresParaMerge.size() > 1) {
    System.out.println("[D] Rodada " + rodada + " de merge: " + 
                      vetoresParaMerge.size() + " vetores restantes");
    
    // Processa em lotes (limitado por processadores)
    // ... mesmo padrão do R
}
```

**Falar:**
> "O importante é que isso funciona com qualquer quantidade de servidores R. Se eu tiver 2 servidores, faço 1 rodada. Se tiver 10 servidores, faço várias rodadas. Se tiver 500 servidores, também funciona, fazendo quantas rodadas forem necessárias. Não pressupõe um número fixo."

---

### PASSO 5: Demonstração Prática (1 minuto)

**Executar o programa:**

1. Iniciar R na porta 12345 (Terminal 1)
2. Iniciar R na porta 12346 (Terminal 2)
3. Iniciar D (Terminal 3)

**No D:**
- Gerar vetor (ex: 1000 elementos)
- Ordenar

**Mostrar os logs:**
```
[R] Rodada 1 de merge: 8 vetores restantes
[R] Rodada 2 de merge: 4 vetores restantes
[R] Rodada 3 de merge: 2 vetores restantes
[R] Rodada 4 de merge: 1 vetor restante

[D] Rodada 1 de merge: 2 vetores restantes
[D] Rodada 2 de merge: 1 vetor restante
```

**Falar:**
> "Veja nos logs que aparecem as rodadas de merge. Isso mostra que o sistema está fazendo múltiplas rodadas até reduzir a um único vetor."

---

### PASSO 6: Conclusão (30 segundos)

**Falar:**
> "Então, professor, agora está exatamente como o senhor pediu: 
> - Threads mergeadoras que fazem merge de 2 em 2
> - Múltiplas rodadas quando necessário
> - Escalável para qualquer quantidade de servidores R
> - Limitado pelo número de processadores
> - Funciona tanto no Receptor quanto no Distribuidor"

---

## 🎯 PONTOS-CHAVE PARA ENFATIZAR

1. ✅ **Threads mergeadoras** (não é mais sequencial)
2. ✅ **Múltiplas rodadas** (quando há muitos vetores)
3. ✅ **Escalável** (funciona com qualquer quantidade de R's)
4. ✅ **Limitado por processadores** (não cria threads infinitas)
5. ✅ **2 a 2 sempre** (sempre merge de 2 vetores)

---

## 💡 RESPOSTAS PARA PERGUNTAS POSSÍVEIS

**P: "Como você garante que não cria threads demais?"**
R: "Uso `Runtime.getRuntime().availableProcessors()` para limitar o número de threads simultâneas ao número de processadores."

**P: "E se tiver 1000 servidores R?"**
R: "Funciona normalmente. O sistema faz quantas rodadas forem necessárias. Na primeira rodada, faz merge de 2 em 2 limitado pelos processadores. Depois faz outra rodada, e assim por diante até ter apenas 1 vetor."

**P: "Como você sabe quando parar?"**
R: "O loop continua enquanto `vetoresParaMerge.size() > 1`. Quando sobra apenas 1 vetor, significa que terminou."

**P: "E se tiver número ímpar de vetores?"**
R: "O vetor ímpar passa direto para a próxima rodada sem fazer merge. Na próxima rodada ele será pareado com outro."

---

## ✅ CHECKLIST FINAL

- [ ] Código compilado sem erros
- [ ] Classe Mergeadora pronta para mostrar
- [ ] Loop de múltiplas rodadas no R pronto para mostrar
- [ ] Loop de múltiplas rodadas no D pronto para mostrar
- [ ] Programa testado localmente
- [ ] Logs mostrando "Rodada X de merge"
- [ ] Entender o fluxo completo
- [ ] Saber explicar a diferença entre antes e agora

