import java.io.*;
import java.net.*;

public class R
{
    public static final int PORTA_PADRAO = 12345;
    
    // Cores para logs
    private static final String RESET = "\033[0m";
    private static final String VERDE = "\033[32m";
    private static final String AZUL = "\033[34m";
    private static final String AMARELO = "\033[33m";
    private static final String VERMELHO = "\033[31m";
    private static final String CIANO = "\033[36m";
    private static final String MAGENTA = "\033[35m";
    
    public static void main (String[] args)
    {
        int porta = PORTA_PADRAO;
        
        if (args.length == 1)
        {
            try
            {
                porta = Integer.parseInt(args[0]);
            }
            catch (NumberFormatException e)
            {
                System.err.println("Porta inválida! Usando porta padrão: " + PORTA_PADRAO);
            }
        }
        
        System.out.println(CIANO + "[R] Iniciando servidor na porta " + porta + RESET);
        
        try (ServerSocket serverSocket = new ServerSocket(porta))
        {
            System.out.println(VERDE + "[R] ✓ Servidor iniciado com sucesso!" + RESET);
            System.out.println(AZUL + "[R] Aguardando conexões..." + RESET);
            
            while (true)
            {
                Socket conexao = serverSocket.accept();
                System.out.println(AMARELO + "[R] Conexão aceita de " + conexao.getInetAddress().getHostAddress() + RESET);
                
                // Criar thread para tratar a conexão
                Thread threadConexao = new Thread(() -> {
                    tratarConexao(conexao);
                });
                threadConexao.start();
            }
        }
        catch (IOException e)
        {
            System.err.println(VERMELHO + "[R] ✗ Erro no servidor: " + e.getMessage() + RESET);
        }
    }
    
    private static void tratarConexao(Socket conexao)
    {
        try (ObjectInputStream receptor = new ObjectInputStream(conexao.getInputStream());
             ObjectOutputStream transmissor = new ObjectOutputStream(conexao.getOutputStream()))
        {
            System.out.println(VERDE + "[R] ✓ Streams criados para " + conexao.getInetAddress().getHostAddress() + RESET);
            System.out.println(AZUL + "[R] Aguardando pedido..." + RESET);
            
            // Aceitar apenas um pedido por conexão
            Comunicado comunicado = (Comunicado) receptor.readObject();
            
            if (comunicado instanceof Pedido)
            {
                Pedido pedido = (Pedido) comunicado;
                System.out.println(AMARELO + "[R] Pedido recebido de " + conexao.getInetAddress().getHostAddress() + RESET);
                
                // Executar contagem em paralelo com métricas de tempo
                long inicioProcessamento = System.currentTimeMillis();
                int qtdProcessadores = Runtime.getRuntime().availableProcessors();
                byte[] numeros = pedido.getNumeros();
                int procurado = pedido.getProcurado();
                
                System.out.println(AZUL + "[R] Processando vetor de " + String.format("%,d", numeros.length) + " elementos com " + qtdProcessadores + " threads" + RESET);
                
                // Dividir vetor em partes
                int tamanhoParte = numeros.length / qtdProcessadores;
                int contagemTotal = 0;
                
                Thread[] threads = new Thread[qtdProcessadores];
                int[] resultados = new int[qtdProcessadores];
                long[] temposThreads = new long[qtdProcessadores];
                
                for (int i = 0; i < qtdProcessadores; i++)
                {
                    final int indice = i;
                    final int inicio = i * tamanhoParte;
                    final int fim = (i == qtdProcessadores - 1) ? numeros.length : (i + 1) * tamanhoParte;
                    
                    threads[i] = new Thread(() -> {
                        long inicioThread = System.currentTimeMillis();
                        int contagemParcial = 0;
                        for (int j = inicio; j < fim; j++)
                        {
                            if (numeros[j] == procurado)
                            {
                                contagemParcial++;
                            }
                        }
                        long fimThread = System.currentTimeMillis();
                        resultados[indice] = contagemParcial;
                        temposThreads[indice] = fimThread - inicioThread;
                        System.out.println(MAGENTA + "[R] Thread " + indice + " processou " + String.format("%,d", fim - inicio) + " elementos, encontrou " + contagemParcial + " ocorrências (tempo: " + temposThreads[indice] + "ms)" + RESET);
                    });
                    threads[i].start();
                }
                
                // Aguardar todas as threads
                System.out.println(CIANO + "[R] Aguardando processamento das threads..." + RESET);
                for (Thread thread : threads)
                {
                    thread.join();
                }
                
                long fimProcessamento = System.currentTimeMillis();
                long tempoTotal = fimProcessamento - inicioProcessamento;
                
                // Somar resultados
                for (int resultado : resultados)
                {
                    contagemTotal += resultado;
                }
                
                System.out.println(VERDE + "[R] ✓ Contagem final: " + contagemTotal + RESET);
                System.out.println(CIANO + "[R] 📊 MÉTRICAS DE TEMPO:" + RESET);
                System.out.println(AMARELO + "[R]   • Tempo total de processamento: " + tempoTotal + "ms" + RESET);
                
                for (int i = 0; i < temposThreads.length; i++)
                {
                    System.out.println(AMARELO + "[R]   • Thread " + i + ": " + temposThreads[i] + "ms" + RESET);
                }
                
                // Enviar resposta
                Resposta resposta = new Resposta(contagemTotal);
                transmissor.writeObject(resposta);
                transmissor.flush();
                System.out.println(VERDE + "[R] ✓ Resposta enviada: " + contagemTotal + RESET);
                
                // Desconectar após a tarefa
                System.out.println(AMARELO + "[R] Tarefa concluída, desconectando..." + RESET);
            }
            else if (comunicado instanceof ComunicadoEncerramento)
            {
                System.out.println(AMARELO + "[R] Comunicado de encerramento recebido de " + conexao.getInetAddress().getHostAddress() + RESET);
            }
        }
        catch (Exception e)
        {
            System.err.println(VERMELHO + "[R] ✗ Erro na conexão com " + conexao.getInetAddress().getHostAddress() + ": " + e.getMessage() + RESET);
        }
        finally
        {
            try
            {
                conexao.close();
                System.out.println(CIANO + "[R] Conexão fechada com " + conexao.getInetAddress().getHostAddress() + RESET);
            }
            catch (IOException e)
            {
                System.err.println(VERMELHO + "[R] ✗ Erro ao fechar conexão: " + e.getMessage() + RESET);
            }
        }
    }
}

