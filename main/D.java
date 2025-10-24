import java.io.*;
import java.net.*;
import java.util.*;

public class D
{
    // IPs dos servidores (hard-coded conforme enunciado)
    private static final String[] IPS_SERVIDORES = {"localhost", "localhost", "localhost"};
    private static final int[] PORTAS_SERVIDORES = {12345, 12346, 12347};
    
    // Cores para logs
    private static final String RESET = "\033[0m";
    private static final String VERDE = "\033[32m";
    private static final String AZUL = "\033[34m";
    private static final String AMARELO = "\033[33m";
    private static final String VERMELHO = "\033[31m";
    private static final String CIANO = "\033[36m";
    
    public static void main(String[] args)
    {
        System.out.println(CIANO + "[D] Iniciando distribuidor..." + RESET);
        
        // Conectar aos servidores
        List<Socket> conexoes = new ArrayList<>();
        List<ObjectOutputStream> transmissores = new ArrayList<>();
        List<ObjectInputStream> receptores = new ArrayList<>();
        
        System.out.println(AZUL + "[D] Conectando aos servidores..." + RESET);
        for (int i = 0; i < IPS_SERVIDORES.length; i++)
        {
            try
            {
                System.out.println(AMARELO + "[D] Tentando conectar com " + IPS_SERVIDORES[i] + ":" + PORTAS_SERVIDORES[i] + "..." + RESET);
                Socket conexao = new Socket(IPS_SERVIDORES[i], PORTAS_SERVIDORES[i]);
                ObjectOutputStream transmissor = new ObjectOutputStream(conexao.getOutputStream());
                ObjectInputStream receptor = new ObjectInputStream(conexao.getInputStream());
                
                conexoes.add(conexao);
                transmissores.add(transmissor);
                receptores.add(receptor);
                
                System.out.println(VERDE + "[D] ✓ Conectado ao servidor " + IPS_SERVIDORES[i] + ":" + PORTAS_SERVIDORES[i] + RESET);
            }
            catch (Exception e)
            {
                System.err.println(VERMELHO + "[D] ✗ Erro ao conectar com " + IPS_SERVIDORES[i] + ":" + PORTAS_SERVIDORES[i] + " - " + e.getMessage() + RESET);
            }
        }
        
        if (conexoes.isEmpty())
        {
            System.err.println(VERMELHO + "[D] Nenhum servidor disponível!" + RESET);
            return;
        }
        
        System.out.println(VERDE + "[D] ✓ Conectado a " + conexoes.size() + " servidores. Iniciando processamento automático..." + RESET);
        
        // Iniciar processamento automaticamente
        processarNovoVetor(conexoes, transmissores, receptores);
        
        // Encerrar após processamento
        encerrarConexoes(conexoes, transmissores, receptores);
        System.out.println(CIANO + "[D] Programa encerrado!" + RESET);
    }
    
    private static int calcularTamanhoMaximoVetor()
    {
        System.out.println(AZUL + "[D] Estimando o maior tamanho possível de vetor em Java..." + RESET);
        long inicio = System.currentTimeMillis();
        int tamanho = 1_000_000; // começa com 1 milhão
        int ultimoBemSucedido = 0;
        
        while (true)
        {
            try
            {
                byte[] vetor = new byte[tamanho];
                ultimoBemSucedido = tamanho;
                vetor = null; // libera
                System.gc();
                
                // aumenta o tamanho em 50% para a próxima tentativa
                if (tamanho > Integer.MAX_VALUE / 3 * 2) break;
                tamanho /= 2;
                tamanho *= 3;
                System.out.println(VERDE + "[D] Alocado com sucesso: " + String.format("%,d", ultimoBemSucedido) + " elementos" + RESET);
            }
            catch (OutOfMemoryError e)
            {
                System.out.println(VERMELHO + "[D] Falhou em " + String.format("%,d", tamanho) + " elementos" + RESET);
                break;
            }
        }
        
        long fim = System.currentTimeMillis();
        System.out.println(CIANO + "[D] Maior vetor que coube (aproximadamente): " + String.format("%,d", ultimoBemSucedido) + RESET);
        System.out.println(AMARELO + "[D] Memória estimada: " + String.format("%.2f MB", ultimoBemSucedido * 1.0 / (1024 * 1024)) + RESET);
        System.out.println(AMARELO + "[D] Tempo total: " + String.format("%.2f segundos", (fim - inicio) / 1000.0) + RESET);
        
        // Limitar tamanho para 2GB (2 bilhões de bytes)
        int tamanhoLimitado = Math.min(ultimoBemSucedido, 2_000_000_000); // Máximo 2GB (limite do int)
        if (tamanhoLimitado < ultimoBemSucedido)
        {
            System.out.println(AMARELO + "[D] ⚠️  Limitando vetor para " + String.format("%,d", tamanhoLimitado) + " elementos (3GB máximo)" + RESET);
        }
        
        return tamanhoLimitado;
    }
    
    private static void processarNovoVetor(List<Socket> conexoes, List<ObjectOutputStream> transmissores, List<ObjectInputStream> receptores)
    {
        try
        {
            // Calcular tamanho máximo do vetor usando o código fornecido
            System.out.println(AZUL + "[D] Calculando tamanho máximo do vetor..." + RESET);
            int tamanho = calcularTamanhoMaximoVetor();
            
            System.out.println(VERDE + "[D] ✓ Tamanho do vetor definido: " + String.format("%,d", tamanho) + " elementos" + RESET);
            
            // Calcular estimativa de memória
            long memoriaEstimada = tamanho * 1L; // 1 byte por elemento
            double memoriaMB = memoriaEstimada / (1024.0 * 1024.0);
            System.out.println(AMARELO + "[D] Memória estimada: " + String.format("%.2f MB", memoriaMB) + RESET);
            
            // Gerar vetor de bytes aleatórios entre -100 e 100
            long inicioGeracao = System.currentTimeMillis();
            System.out.println(AZUL + "[D] Gerando vetor de " + String.format("%,d", tamanho) + " elementos..." + RESET);
            byte[] vetor = new byte[tamanho];
            Random random = new Random();
            for (int i = 0; i < tamanho; i++)
            {
                vetor[i] = (byte)(random.nextInt(201) - 100); // -100 a 100
            }
            long fimGeracao = System.currentTimeMillis();
            System.out.println(VERDE + "[D] ✓ Vetor gerado em " + (fimGeracao - inicioGeracao) + "ms!" + RESET);
            
            // Escolher aleatoriamente um número do vetor para contar
            int posicaoAleatoria = random.nextInt(tamanho);
            int numeroProcurado = vetor[posicaoAleatoria];
            
            System.out.println(CIANO + "[D] Procurando pelo número: " + numeroProcurado + " (posição " + String.format("%,d", posicaoAleatoria) + ")" + RESET);
            
            // Dividir vetor entre servidores usando paralelismo correto
            int tamanhoParte = vetor.length / transmissores.size();
            List<Integer> resultados = Collections.synchronizedList(new ArrayList<>());
            List<Long> temposThreads = Collections.synchronizedList(new ArrayList<>());
            
            System.out.println(AZUL + "[D] Processando vetor em " + transmissores.size() + " partes paralelas..." + RESET);
            
            long inicioProcessamento = System.currentTimeMillis();
            
            // Fase 1: Iniciar Todas as Tarefas
            System.out.println(CIANO + "[D] FASE 1: Iniciando todas as threads..." + RESET);
            Thread[] threads = new Thread[transmissores.size()];
            
            for (int i = 0; i < transmissores.size(); i++)
            {
                final int indice = i;
                final int inicio = i * tamanhoParte;
                final int fim = (i == transmissores.size() - 1) ? vetor.length : (i + 1) * tamanhoParte;
                
                threads[i] = new Thread(() -> {
                    long inicioThread = System.currentTimeMillis();
                    try
                    {
                        System.out.println(AMARELO + "[D] Thread " + indice + " iniciada (elementos " + String.format("%,d", inicio) + " a " + String.format("%,d", fim-1) + ")" + RESET);
                        
                        // Criar parte do vetor
                        byte[] parteVetor = new byte[fim - inicio];
                        System.arraycopy(vetor, inicio, parteVetor, 0, fim - inicio);
                        
                        System.out.println(AMARELO + "[D] Thread " + indice + " enviando pedido (parte: " + String.format("%,d", parteVetor.length) + " elementos)" + RESET);
                        
                        // Enviar pedido
                        Pedido pedido = new Pedido(parteVetor, numeroProcurado);
                        transmissores.get(indice).writeObject(pedido);
                        transmissores.get(indice).flush();
                        
                        System.out.println(VERDE + "[D] ✓ Thread " + indice + " pedido enviado" + RESET);
                        
                        // Receber resposta
                        Resposta resposta = (Resposta) receptores.get(indice).readObject();
                        resultados.add(resposta.getContagem());
                        
                        long fimThread = System.currentTimeMillis();
                        long tempoThread = fimThread - inicioThread;
                        temposThreads.add(tempoThread);
                        
                        System.out.println(CIANO + "[D] ✓ Thread " + indice + " resposta recebida: " + resposta.getContagem() + " (tempo: " + tempoThread + "ms)" + RESET);
                        
                        // Liberar memória da parte processada
                        parteVetor = null;
                        System.gc();
                    }
                    catch (Exception e)
                    {
                        System.err.println(VERMELHO + "[D] ✗ Erro na Thread " + indice + ": " + e.getMessage() + RESET);
                        resultados.add(0);
                        temposThreads.add(0L);
                    }
                });
                
                // Iniciar thread (Fase 1)
                threads[i].start();
                System.out.println(VERDE + "[D] ✓ Thread " + indice + " iniciada (start())" + RESET);
            }
            
            // Fase 2: Sincronizar (Aguardar) Todas as Tarefas
            System.out.println(CIANO + "[D] FASE 2: Aguardando todas as threads..." + RESET);
            for (int i = 0; i < threads.length; i++)
            {
                try
                {
                    threads[i].join();
                    System.out.println(VERDE + "[D] ✓ Thread " + i + " finalizada (join())" + RESET);
                }
                catch (InterruptedException e)
                {
                    System.err.println(VERMELHO + "[D] ✗ Erro ao aguardar Thread " + i + ": " + e.getMessage() + RESET);
                }
            }
            
            System.out.println(VERDE + "[D] ✓ Todas as threads finalizadas!" + RESET);
            
            long fimProcessamento = System.currentTimeMillis();
            long tempoTotal = fimProcessamento - inicioProcessamento;
            
            // Somar resultados
            int contagemTotal = 0;
            for (Integer resultado : resultados)
            {
                contagemTotal += resultado;
            }
            
            // Mostrar métricas de tempo
            System.out.println(VERDE + "[D] ✓ Contagem final: " + contagemTotal + RESET);
            System.out.println(CIANO + "[D] 📊 MÉTRICAS DE TEMPO:" + RESET);
            System.out.println(AMARELO + "[D]   • Tempo total de processamento: " + tempoTotal + "ms" + RESET);
            System.out.println(AMARELO + "[D]   • Tempo de geração do vetor: " + (fimGeracao - inicioGeracao) + "ms" + RESET);
            
            for (int i = 0; i < temposThreads.size(); i++)
            {
                System.out.println(AMARELO + "[D]   • Thread " + i + ": " + temposThreads.get(i) + "ms" + RESET);
            }
            
            // Desconectar após calcular
            System.out.println(AMARELO + "[D] Tarefa concluída, desconectando dos servidores..." + RESET);
            encerrarConexoes(conexoes, transmissores, receptores);
        }
        catch (Exception e)
        {
            System.err.println(VERMELHO + "[D] ✗ Erro ao processar novo vetor: " + e.getMessage() + RESET);
        }
    }
    
    
    
    private static void encerrarConexoes(List<Socket> conexoes, List<ObjectOutputStream> transmissores, List<ObjectInputStream> receptores)
    {
        System.out.println("[D] Encerrando conexões...");
        
        // Enviar comunicado de encerramento para todos os servidores
        for (ObjectOutputStream transmissor : transmissores)
        {
            try
            {
                transmissor.writeObject(new ComunicadoEncerramento());
                transmissor.flush();
            }
            catch (Exception e)
            {
                System.err.println("[D] Erro ao enviar comunicado de encerramento: " + e.getMessage());
            }
        }
        
        // Fechar todas as conexões
        for (Socket conexao : conexoes)
        {
            try
            {
                conexao.close();
            }
            catch (Exception e)
            {
                System.err.println("[D] Erro ao fechar conexão: " + e.getMessage());
            }
        }
        
        System.out.println("[D] Conexões encerradas!");
    }
}