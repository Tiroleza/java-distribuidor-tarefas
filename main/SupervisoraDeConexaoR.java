import java.io.*;
import java.net.*;
import java.util.*;

/**
 * Thread dedicada para gerenciar o ciclo de vida de UMA única conexão de cliente.
 * Recebe comunicados do cliente, processa pedidos usando paralelismo interno
 * e mantém a conexão ativa para múltiplas requisições.
 */
public class SupervisoraDeConexaoR extends Thread
{
    private Parceiro            usuario;
    private Socket              conexao;
    private ArrayList<Parceiro> usuarios;
    
    // Cores para logs
    private static final String RESET = "\033[0m";
    private static final String VERDE = "\033[32m";
    private static final String AZUL = "\033[34m";
    private static final String AMARELO = "\033[33m";
    private static final String VERMELHO = "\033[31m";
    private static final String CIANO = "\033[36m";
    private static final String MAGENTA = "\033[35m";

    public SupervisoraDeConexaoR
    (Socket conexao, ArrayList<Parceiro> usuarios)
    throws Exception
    {
        if (conexao==null)
            throw new Exception ("Conexao ausente");

        if (usuarios==null)
            throw new Exception ("Usuarios ausentes");

        this.conexao  = conexao;
        this.usuarios = usuarios;
    }

    public void run ()
    {
        ObjectOutputStream transmissor=null;
        try
        {
            transmissor =
            new ObjectOutputStream(
            this.conexao.getOutputStream());
        }
        catch (Exception erro)
        {
            return;
        }
        
        ObjectInputStream receptor=null;
        try
        {
            receptor=
            new ObjectInputStream(
            this.conexao.getInputStream());
        }
        catch (Exception err0)
        {
            try
            {
                transmissor.close();
            }
            catch (Exception falha)
            {} // so tentando fechar antes de acabar a thread
            
            return;
        }

        try
        {
            this.usuario =
            new Parceiro (this.conexao,
                          receptor,
                          transmissor);
        }
        catch (Exception erro)
        {} // sei que passei os parametros corretos

        try
        {
            synchronized (this.usuarios)
            {
                this.usuarios.add (this.usuario);
            }

            System.out.println(VERDE + "[R] ✓ Streams criados para " + conexao.getInetAddress().getHostAddress() + RESET);
            
            for(;;)
            {
                System.out.println(AZUL + "[R] Aguardando comunicado..." + RESET);
                
                Comunicado comunicado = this.usuario.envie();

                if (comunicado==null)
                    return;
                else if (comunicado instanceof Pedido)
                {
                    Pedido pedido = (Pedido)comunicado;
                    System.out.println(AMARELO + "[R] Pedido recebido de " + conexao.getInetAddress().getHostAddress() + RESET);
                    
                    // 1. Processa o pedido (faz a contagem, etc.)
                    long inicioProcessamento = System.currentTimeMillis();
                    int qtdProcessadores = Runtime.getRuntime().availableProcessors();
                    byte[] numeros = pedido.getNumeros();
                    int procurado = pedido.getProcurado();
                    
                    System.out.println(AZUL + "[R] Processando vetor de " + String.format("%,d", numeros.length) + " elementos com " + qtdProcessadores + " threads" + RESET);
                    
                    // Dividir vetor em partes
                    int tamanhoParte = numeros.length / qtdProcessadores;
                    int contagemTotal = 0;
                    
                    Contadora[] threads = new Contadora[qtdProcessadores];
                    
                    for (int i = 0; i < qtdProcessadores; i++)
                    {
                        final int inicio = i * tamanhoParte;
                        final int fim = (i == qtdProcessadores - 1) ? numeros.length : (i + 1) * tamanhoParte;
                        
                        threads[i] = new Contadora(numeros, inicio, fim, procurado, i);
                        threads[i].start();
                    }
                    
                    // Aguardar todas as threads
                    System.out.println(CIANO + "[R] Aguardando processamento das threads..." + RESET);
                    for (int i = 0; i < threads.length; i++)
                    {
                        try
                        {
                            threads[i].join();
                        }
                        catch (InterruptedException e)
                        {
                            System.err.println(VERMELHO + "[R] ✗ Erro ao aguardar Thread " + i + ": " + e.getMessage() + RESET);
                        }
                    }
                    
                    long fimProcessamento = System.currentTimeMillis();
                    long tempoTotal = fimProcessamento - inicioProcessamento;
                    
                    // Somar resultados
                    for (int i = 0; i < threads.length; i++)
                    {
                        contagemTotal += threads[i].getContagemParcial();
                    }
                    
                    System.out.println(VERDE + "[R] ✓ Contagem final: " + contagemTotal + RESET);
                    System.out.println(CIANO + "[R] 📊 MÉTRICAS DE TEMPO:" + RESET);
                    System.out.println(AMARELO + "[R]   • Tempo total de processamento: " + tempoTotal + "ms" + RESET);
                    
                    for (int i = 0; i < threads.length; i++)
                    {
                        System.out.println(AMARELO + "[R]   • Thread " + i + ": " + threads[i].getTempoThread() + "ms" + RESET);
                    }
                    
                    // 2. Envia a Resposta
                    Resposta resposta = new Resposta(contagemTotal);
                    this.usuario.receba(resposta);
                    System.out.println(VERDE + "[R] ✓ Resposta enviada: " + contagemTotal + RESET);
                    
                    // 3. Resposta enviada. Continua o loop para o próximo comunicado
                    System.out.println(CIANO + "[R] Aguardando próximo pedido..." + RESET);
                }
                else if (comunicado instanceof ComunicadoEncerramento)
                {
                    // 1. Remover cliente da lista e fechar conexão
                    System.out.println(AMARELO + "[R] Comunicado de encerramento recebido de " + conexao.getInetAddress().getHostAddress() + RESET);
                    
                    synchronized (this.usuarios)
                    {
                        this.usuarios.remove (this.usuario);
                    }
                    this.usuario.adeus();
                    
                    // 2. Cliente pediu encerramento. Sai do loop
                    return;
                }
            }
        }
        catch (Exception erro)
        {
            try
            {
                transmissor.close ();
                receptor   .close ();
            }
            catch (Exception falha)
            {} // so tentando fechar antes de acabar a thread

            return;
        }
    }
}
