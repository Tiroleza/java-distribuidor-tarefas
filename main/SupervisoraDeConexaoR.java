import java.io.*;
import java.net.*;
import java.util.*;

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

    public SupervisoraDeConexaoR(Socket conexao, ArrayList<Parceiro> usuarios) throws Exception
    {
        if (conexao==null) throw new Exception ("Conexao ausente");
        if (usuarios==null) throw new Exception ("Usuarios ausentes");
        this.conexao  = conexao;
        this.usuarios = usuarios;
    }

    public void run ()
    {
        ObjectOutputStream transmissor=null;
        try {
            transmissor = new ObjectOutputStream(this.conexao.getOutputStream());
        } catch (Exception erro) { return; }
        
        ObjectInputStream receptor=null;
        try {
            receptor = new ObjectInputStream(this.conexao.getInputStream());
        } catch (Exception err0) {
            try { transmissor.close(); } catch (Exception falha) {}
            return;
        }

        try {
            this.usuario = new Parceiro (this.conexao, receptor, transmissor);
        } catch (Exception erro) {}

        try
        {
            synchronized (this.usuarios) {
                this.usuarios.add (this.usuario);
            }

            System.out.println(VERDE + "[R] Streams criados para " + conexao.getInetAddress().getHostAddress() + RESET);
            
            for(;;)
            {
                System.out.println(AZUL + "[R] Aguardando comunicado..." + RESET);
                
                Comunicado comunicado = this.usuario.envie();

                if (comunicado==null) return;
                
                else if (comunicado instanceof Pedido)
                {
                    Pedido pedido = (Pedido)comunicado;
                    System.out.println(AMARELO + "[R] Pedido de ordenação recebido de " + conexao.getInetAddress().getHostAddress() + RESET);
                    
                    long inicioProcessamento = System.currentTimeMillis();
                    int qtdProcessadores = Runtime.getRuntime().availableProcessors();
                    byte[] numeros = pedido.getNumeros();
                    
                    System.out.println(AZUL + "[R] Ordenando vetor de " + String.format("%,d", numeros.length) + " elementos com " + qtdProcessadores + " threads" + RESET);
                    
                    int tamanhoParte = numeros.length / qtdProcessadores;
                    
                    // Vetor de Threads Ordenadoras (substitui Contadora)
                    Ordenadora[] threads = new Ordenadora[qtdProcessadores];
                    
                    for (int i = 0; i < qtdProcessadores; i++)
                    {
                        final int inicio = i * tamanhoParte;
                        final int fim = (i == qtdProcessadores - 1) ? numeros.length : (i + 1) * tamanhoParte;
                        
                        // Cria uma cópia da fatia para a thread ordenar
                        byte[] fatia = Arrays.copyOfRange(numeros, inicio, fim);
                        
                        threads[i] = new Ordenadora(fatia, i);
                        threads[i].start();
                    }
                    
                    System.out.println(CIANO + "[R] Aguardando ordenação das threads..." + RESET);
                    for (int i = 0; i < threads.length; i++)
                    {
                        try { threads[i].join(); }
                        catch (InterruptedException e) {
                            System.err.println(VERMELHO + "[R] Erro ao aguardar Thread " + i + RESET);
                        }
                    }
                    
                    // --- FASE DE MERGE (Intercalação) DOS RESULTADOS LOCAIS ---
                    System.out.println(CIANO + "[R] Intercalando resultados das threads..." + RESET);
                    
                    // Pega o vetor da primeira thread como base
                    byte[] vetorOrdenadoTotal = threads[0].getVetorOrdenado();
                    
                    // Intercala com os vetores das outras threads sequencialmente
                    for (int i = 1; i < threads.length; i++)
                    {
                        vetorOrdenadoTotal = intercalar(vetorOrdenadoTotal, threads[i].getVetorOrdenado());
                    }
                    
                    long fimProcessamento = System.currentTimeMillis();
                    long tempoTotal = fimProcessamento - inicioProcessamento;
                    
                    System.out.println(VERDE + "[R] Ordenação finalizada. Tamanho: " + String.format("%,d", vetorOrdenadoTotal.length) + RESET);
                    System.out.println(AMARELO + "[R] Tempo total de processamento: " + tempoTotal + "ms" + RESET);
                    
                    // Envia a Resposta com o vetor ordenado
                    Resposta resposta = new Resposta(vetorOrdenadoTotal);
                    this.usuario.receba(resposta);
                    System.out.println(VERDE + "[R] Vetor ordenado enviado ao cliente." + RESET);
                }
                else if (comunicado instanceof ComunicadoEncerramento)
                {
                    System.out.println(AMARELO + "[R] Encerrando conexão com " + conexao.getInetAddress().getHostAddress() + RESET);
                    synchronized (this.usuarios) { this.usuarios.remove (this.usuario); }
                    this.usuario.adeus();
                    return;
                }
            }
        }
        catch (Exception erro)
        {
            try { transmissor.close(); receptor.close(); } catch (Exception falha) {}
            return;
        }
    }

    // Método Utilitário para Intercalar (Merge) dois vetores ordenados
    private byte[] intercalar(byte[] A, byte[] B) {
        byte[] C = new byte[A.length + B.length];
        int i = 0, j = 0, k = 0;
        while (i < A.length && j < B.length) {
            if (A[i] <= B[j]) C[k++] = A[i++];
            else              C[k++] = B[j++];
        }
        while (i < A.length) C[k++] = A[i++];
        while (j < B.length) C[k++] = B[j++];
        return C;
    }
}