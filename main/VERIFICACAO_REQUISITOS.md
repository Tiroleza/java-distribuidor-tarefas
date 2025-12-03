# Verificação de Requisitos da Atividade #2

## ✅ REQUISITOS ATENDIDOS

### 1. Classes de Comunicação
- ✅ **Comunicado**: Implementa Serializable, sem atributos, serve como superclasse
- ✅ **Pedido**: Extende Comunicado, tem byte[] numeros, construtor, método ordenar() com Merge Sort
- ✅ **Resposta**: Extende Comunicado, tem vetor ordenado, construtor, getter getVetor()
- ✅ **ComunicadoEncerramento**: Extende Comunicado, sem atributos, sinal de término

### 2. Programa R (Receptor)
- ✅ ServerSocket em porta fixa (12345 ou configurável)
- ✅ Aceita conexões
- ✅ ObjectInputStream e ObjectOutputStream associados
- ✅ Loop lendo objetos do cliente
- ✅ Se Pedido: executa ordenar(), envia Resposta
- ✅ Se ComunicadoEncerramento: encerra conexão e volta a aceitar novas conexões
- ✅ Conexões persistentes (mantém aberta até encerramento)

### 3. Programa D (Distribuidor)
- ✅ Vetor de IPs hard coded (IPS_SERVIDORES)
- ✅ Gera vetor grande de bytes aleatórios
- ✅ Divide vetor em partes semelhantes
- ✅ Thread para cada servidor (TrabalhadoraD)
- ✅ Estabelece e mantém conexão aberta
- ✅ Envia Pedidos sucessivos
- ✅ Recebe Respostas sucessivas
- ✅ Usa join() para aguardar término das threads
- ✅ Envia ComunicadoEncerramento quando não deseja mais ordenar
- ✅ Fecha transmissores, receptores e conexões

### 4. Threads Ordenadoras (no R)
- ✅ Cada thread ordenadora ordena sua parte recursivamente pelo Merge Sort
- ✅ Quantidade de threads = quantidade de processadores (no máximo)
- ✅ Usa join() para aguardar término

### 5. Threads Mergeadoras (no R)
- ✅ Threads mergeadoras fazem merge de 2 pedacinhos ordenados
- ✅ Múltiplas rodadas de merge até obter um único vetor
- ✅ Quantidade máxima de threads = quantidade de processadores
- ✅ Processa em lotes para aproveitar paralelismo

### 6. Threads Mergeadoras (no D)
- ✅ Threads mergeadoras fazem merge dos vetores ordenados recebidos dos R's
- ✅ Merge sempre 2 a 2
- ✅ Múltiplas rodadas de merge até reduzir a um único vetor
- ✅ Quantidade máxima de threads = quantidade de processadores
- ✅ Escalável para qualquer quantidade de R's (não pressupõe número fixo)
- ✅ Funciona com 2, 3, 500 ou 1000 servidores R

### 7. Comunicação TCP/IP
- ✅ Serialização de objetos (ObjectInputStream/ObjectOutputStream)
- ✅ Conexões persistentes
- ✅ Cada servidor R mantém conexão aberta até receber ComunicadoEncerramento

### 8. Programa Sequencial
- ✅ Programa OrdenacaoSequencial.java criado
- ✅ Realiza ordenação sem paralelismo ou distribuição
- ✅ Usa Merge Sort (mesma lógica)
- ✅ Mede tempo de execução

### 9. Medição de Tempo
- ✅ D mede tempo total de ordenação distribuída
- ✅ R mede tempo de processamento
- ✅ Programa sequencial mede tempo de execução
- ✅ Comparação possível entre distribuído e sequencial

### 10. Salvar em Arquivo
- ✅ Solicita nome do arquivo ao usuário
- ✅ Salva vetor ordenado em arquivo texto
- ✅ Implementado no D após ordenação

### 11. Boas Práticas
- ✅ Captura e trata exceções adequadamente
- ✅ Usa join() para aguardar threads (no R e no D)
- ✅ Mensagens de log informativas em ambos os programas
- ✅ Tratamento de erros de conexão

### 12. Estrutura do Sistema
- ✅ D atua como cliente
- ✅ R atua como servidor
- ✅ Threads ordenadoras em cada R
- ✅ Threads mergeadoras em cada R (múltiplas rodadas)
- ✅ Threads mergeadoras no D (múltiplas rodadas)
- ✅ Escalabilidade garantida

## 📋 RESUMO

**Total de Requisitos Verificados: 12/12 ✅**

Todos os requisitos da atividade foram implementados e verificados:
- Classes de comunicação corretas
- Programa R funcionando com threads ordenadoras e mergeadoras
- Programa D funcionando com threads mergeadoras escaláveis
- Programa sequencial para comparação
- Medição de tempo
- Salvamento em arquivo
- Boas práticas de programação

O programa está completo e pronto para demonstração!

