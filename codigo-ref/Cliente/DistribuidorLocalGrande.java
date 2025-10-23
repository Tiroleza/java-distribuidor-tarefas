import java.io.*;
import java.net.*;
import java.util.*;

public class DistribuidorLocalGrande
{
    public static final int PORTA_PADRAO = 12345;
    
    // IPs para teste local (múltiplas portas)
    private static final String[] IPS_SERVIDORES = {
        "localhost:12345",
        "localhost:12346", 
        "localhost:12347",
        "localhost:12348"
    };
    
    public static void main(String[] args)
    {
        System.out.println("=== SISTEMA DISTRIBUÍDO DE CONTAGEM (VETOR GRANDE) ===");
        System.out.println("Processadores disponíveis: " + Runtime.getRuntime().availableProcessors());
        
        // Parâmetros configuráveis
        int tamanhoVetor = 100_000_000; // Padrão: 100 milhões
        int numeroProcurado = 50;
        
        // Permitir configuração via argumentos
        if (args.length > 0) {
            try {
                tamanhoVetor = Integer.parseInt(args[0]);
                System.out.println("Tamanho do vetor configurado via argumento: " + String.format("%,d", tamanhoVetor));
            } catch (NumberFormatException e) {
                System.out.println("Argumento inválido, usando tamanho padrão: " + String.format("%,d", tamanhoVetor));
            }
        }
        
        if (args.length > 1) {
            try {
                numeroProcurado = Integer.parseInt(args[1]);
                System.out.println("Número procurado configurado via argumento: " + numeroProcurado);
            } catch (NumberFormatException e) {
                System.out.println("Argumento inválido, usando número padrão: " + numeroProcurado);
            }
        }
        
        System.out.println("Tamanho do vetor: " + String.format("%,d", tamanhoVetor) + " elementos");
        System.out.printf("Memória estimada: %.2f MB%n", tamanhoVetor * 1.0 / (1024 * 1024));
        System.out.println("Número procurado: " + numeroProcurado);
        
        // Verificar memória disponível
        Runtime runtime = Runtime.getRuntime();
        long memoriaMaxima = runtime.maxMemory();
        long memoriaUsada = runtime.totalMemory() - runtime.freeMemory();
        long memoriaDisponivel = memoriaMaxima - memoriaUsada;
        
        System.out.printf("Memória máxima JVM: %.2f MB%n", memoriaMaxima / (1024.0 * 1024.0));
        System.out.printf("Memória disponível: %.2f MB%n", memoriaDisponivel / (1024.0 * 1024.0));
        
        // Gerar vetor
        byte[] vetorGrande = gerarVetor(tamanhoVetor);
        
        System.out.println("\n[D] Iniciando contagem distribuída...");
        System.out.println("[D] Procurando número: " + numeroProcurado);
        
        // Medir tempo de execução
        long inicioTempo = System.currentTimeMillis();
        
        // Dividir vetor e enviar para servidores
        int contagemTotal = executarContagemDistribuida(vetorGrande, numeroProcurado);
        
        long fimTempo = System.currentTimeMillis();
        long tempoExecucao = fimTempo - inicioTempo;
        
        System.out.println("\n=== RESULTADO FINAL ===");
        System.out.println("Número procurado: " + numeroProcurado);
        System.out.println("Total de ocorrências: " + String.format("%,d", contagemTotal));
        System.out.println("Tempo de execução: " + tempoExecucao + " ms");
        System.out.printf("Throughput: %.2f elementos/ms%n", tamanhoVetor * 1.0 / tempoExecucao);
        
        // Comparar com contagem sequencial
        System.out.println("\n=== COMPARAÇÃO COM CONTAGEM SEQUENCIAL ===");
        long inicioSeq = System.currentTimeMillis();
        int contagemSeq = contarSequencial(vetorGrande, numeroProcurado);
        long fimSeq = System.currentTimeMillis();
        long tempoSeq = fimSeq - inicioSeq;
        
        System.out.println("Contagem sequencial: " + String.format("%,d", contagemSeq) + " ocorrências em " + tempoSeq + " ms");
        System.out.println("Verificação: " + (contagemTotal == contagemSeq ? "✓ CORRETO" : "✗ ERRO"));
        System.out.printf("Throughput sequencial: %.2f elementos/ms%n", tamanhoVetor * 1.0 / tempoSeq);
        
        if (tempoSeq > 0) {
            double aceleracao = (double) tempoSeq / tempoExecucao;
            System.out.println("Aceleração: " + String.format("%.2f", aceleracao) + "x");
        }
        
        // Estatísticas de memória
        System.out.println("\n=== ESTATÍSTICAS DE MEMÓRIA ===");
        System.out.printf("Memória usada após execução: %.2f MB%n", 
            (runtime.totalMemory() - runtime.freeMemory()) / (1024.0 * 1024.0));
    }
    
    private static byte[] gerarVetor(int tamanho)
    {
        System.out.println("[D] Gerando vetor de " + String.format("%,d", tamanho) + " elementos...");
        long inicioGeracao = System.currentTimeMillis();
        
        byte[] vetor = new byte[tamanho];
        Random random = new Random();
        
        for (int i = 0; i < tamanho; i++)
        {
            // Números aleatórios entre -100 e 100
            vetor[i] = (byte) (random.nextInt(201) - 100);
            
            // Mostrar progresso a cada 10 milhões de elementos
            if (i > 0 && i % 10_000_000 == 0) {
                System.out.printf("[D] Progresso: %.1f%% (%d/%d)%n", 
                    (i * 100.0 / tamanho), i, tamanho);
            }
        }
        
        long fimGeracao = System.currentTimeMillis();
        System.out.println("[D] Vetor gerado com sucesso em " + (fimGeracao - inicioGeracao) + " ms!");
        return vetor;
    }
    
    private static int executarContagemDistribuida(byte[] vetor, int numeroProcurado)
    {
        int numServidores = IPS_SERVIDORES.length;
        int tamanhoParte = vetor.length / numServidores;
        
        List<Thread> threads = new ArrayList<>();
        List<ContadorThreadLocalGrande> contadores = new ArrayList<>();
        
        // Criar threads para cada servidor
        for (int i = 0; i < numServidores; i++)
        {
            int inicio = i * tamanhoParte;
            int fim = (i == numServidores - 1) ? vetor.length : (i + 1) * tamanhoParte;
            
            byte[] parteVetor = Arrays.copyOfRange(vetor, inicio, fim);
            
            ContadorThreadLocalGrande contador = new ContadorThreadLocalGrande(IPS_SERVIDORES[i], parteVetor, numeroProcurado);
            contadores.add(contador);
            
            Thread thread = new Thread(contador);
            threads.add(thread);
            thread.start();
            
            System.out.println("[D] Thread " + (i + 1) + " iniciada para " + IPS_SERVIDORES[i] + 
                             " (elementos " + String.format("%,d", inicio) + " a " + String.format("%,d", fim - 1) + ")");
        }
        
        // Aguardar todas as threads terminarem
        for (Thread thread : threads)
        {
            try
            {
                thread.join();
            }
            catch (InterruptedException e)
            {
                System.err.println("[D] Thread interrompida: " + e.getMessage());
            }
        }
        
        // Somar resultados
        int contagemTotal = 0;
        for (ContadorThreadLocalGrande contador : contadores)
        {
            contagemTotal += contador.getResultado();
        }
        
        return contagemTotal;
    }
    
    private static int contarSequencial(byte[] vetor, int numeroProcurado)
    {
        int contagem = 0;
        for (byte numero : vetor)
        {
            if (numero == numeroProcurado)
            {
                contagem++;
            }
        }
        return contagem;
    }
}

class ContadorThreadLocalGrande implements Runnable
{
    private final String servidorInfo;
    private final byte[] parteVetor;
    private final int numeroProcurado;
    private int resultado;
    private boolean sucesso;
    
    public ContadorThreadLocalGrande(String servidorInfo, byte[] parteVetor, int numeroProcurado)
    {
        this.servidorInfo = servidorInfo;
        this.parteVetor = parteVetor;
        this.numeroProcurado = numeroProcurado;
        this.resultado = 0;
        this.sucesso = false;
    }
    
    @Override
    public void run()
    {
        Socket conexao = null;
        ObjectOutputStream transmissor = null;
        ObjectInputStream receptor = null;
        
        try
        {
            String[] partes = servidorInfo.split(":");
            String host = partes[0];
            int porta = Integer.parseInt(partes[1]);
            
            System.out.println("[D] Conectando ao servidor " + host + ":" + porta + "...");
            conexao = new Socket(host, porta);
            
            try (ObjectOutputStream trans = new ObjectOutputStream(conexao.getOutputStream());
                 ObjectInputStream rec = new ObjectInputStream(conexao.getInputStream()))
            {
                transmissor = trans;
                receptor = rec;
            
            System.out.println("[D] Conexão estabelecida com " + host + ":" + porta);
            
            // Converter byte[] para int[]
            int[] parteVetorInt = new int[parteVetor.length];
            for (int i = 0; i < parteVetor.length; i++) {
                parteVetorInt[i] = parteVetor[i];
            }
            
            // Enviar pedido
            Pedido pedido = new Pedido(parteVetorInt, numeroProcurado);
            transmissor.writeObject(pedido);
            transmissor.flush();
            
            System.out.println("[D] Pedido enviado para " + host + ":" + porta);
            
                // Receber resposta
                Comunicado comunicado = (Comunicado) receptor.readObject();
                if (comunicado instanceof Resposta resposta)
                {
                    this.resultado = resposta.getContagem();
                    this.sucesso = true;
                    System.out.println("[D] Resposta recebida de " + host + ":" + porta + ": " + String.format("%,d", resultado) + " ocorrências");
                }
            }
        }
        catch (IOException | ClassNotFoundException e)
        {
            System.err.println("[D] Erro na comunicação com " + servidorInfo + ": " + e.getMessage());
            this.resultado = 0;
            this.sucesso = false;
        }
        finally
        {
            try
            {
                if (conexao != null) conexao.close();
            }
            catch (IOException e)
            {
                System.err.println("[D] Erro ao fechar conexão com " + servidorInfo + ": " + e.getMessage());
            }
        }
    }
    
    public int getResultado()
    {
        return resultado;
    }
    
    public boolean isSucesso()
    {
        return sucesso;
    }
}
