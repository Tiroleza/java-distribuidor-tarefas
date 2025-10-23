import java.io.*;
import java.net.*;
import java.util.*;

public class DistribuidorLocal
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
        Scanner scanner = new Scanner(System.in);
        
        System.out.println("=== SISTEMA DISTRIBUÍDO DE CONTAGEM (TESTE LOCAL) ===");
        System.out.println("Processadores disponíveis: " + Runtime.getRuntime().availableProcessors());
        
        // Solicitar tamanho do vetor
        int tamanhoVetor = solicitarTamanhoVetor(scanner);
        
        // Solicitar se deve exibir o vetor
        boolean exibirVetor = solicitarExibicaoVetor(scanner);
        
        // Solicitar número a procurar
        int numeroProcurado = solicitarNumeroProcurado(scanner);
        
        // Gerar vetor
        byte[] vetorGrande = gerarVetor(tamanhoVetor);
        
        if (exibirVetor)
        {
            exibirVetor(vetorGrande);
        }
        
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
        System.out.println("Total de ocorrências: " + contagemTotal);
        System.out.println("Tempo de execução: " + tempoExecucao + " ms");
        
        scanner.close();
    }
    
    private static int solicitarTamanhoVetor(Scanner scanner)
    {
        System.out.print("Digite o tamanho do vetor (recomendado: 10000-100000): ");
        try
        {
            int tamanho = scanner.nextInt();
            if (tamanho <= 0)
            {
                System.out.println("Tamanho inválido! Usando tamanho padrão: 10000");
                return 10000;
            }
            return tamanho;
        }
        catch (InputMismatchException e)
        {
            System.out.println("Entrada inválida! Usando tamanho padrão: 10000");
            scanner.nextLine(); // Limpar buffer
            return 10000;
        }
    }
    
    private static boolean solicitarExibicaoVetor(Scanner scanner)
    {
        System.out.print("Deseja exibir o vetor na tela? (s/n): ");
        String resposta = scanner.next().toLowerCase();
        return resposta.equals("s") || resposta.equals("sim");
    }
    
    private static int solicitarNumeroProcurado(Scanner scanner)
    {
        System.out.print("Digite o número a procurar (ou '111' para testar número inexistente): ");
        try
        {
            return scanner.nextInt();
        }
        catch (InputMismatchException e)
        {
            System.out.println("Entrada inválida! Usando número padrão: 0");
            scanner.nextLine(); // Limpar buffer
            return 0;
        }
    }
    
    private static byte[] gerarVetor(int tamanho)
    {
        System.out.println("[D] Gerando vetor de " + tamanho + " elementos...");
        byte[] vetor = new byte[tamanho];
        Random random = new Random();
        
        for (int i = 0; i < tamanho; i++)
        {
            // Números aleatórios entre -100 e 100
            vetor[i] = (byte) (random.nextInt(201) - 100);
        }
        
        System.out.println("[D] Vetor gerado com sucesso!");
        return vetor;
    }
    
    private static void exibirVetor(byte[] vetor)
    {
        System.out.println("\n=== VETOR GERADO ===");
        for (int i = 0; i < Math.min(vetor.length, 50); i++) // Exibir apenas os primeiros 50 elementos
        {
            System.out.print(vetor[i] + " ");
            if ((i + 1) % 10 == 0) System.out.println();
        }
        if (vetor.length > 50)
        {
            System.out.println("... (mostrando apenas os primeiros 50 elementos)");
        }
        System.out.println();
    }
    
    private static int executarContagemDistribuida(byte[] vetor, int numeroProcurado)
    {
        int numServidores = IPS_SERVIDORES.length;
        int tamanhoParte = vetor.length / numServidores;
        
        List<Thread> threads = new ArrayList<>();
        List<ContadorThreadLocal> contadores = new ArrayList<>();
        
        // Criar threads para cada servidor
        for (int i = 0; i < numServidores; i++)
        {
            int inicio = i * tamanhoParte;
            int fim = (i == numServidores - 1) ? vetor.length : (i + 1) * tamanhoParte;
            
            byte[] parteVetor = Arrays.copyOfRange(vetor, inicio, fim);
            
            ContadorThreadLocal contador = new ContadorThreadLocal(IPS_SERVIDORES[i], parteVetor, numeroProcurado);
            contadores.add(contador);
            
            Thread thread = new Thread(contador);
            threads.add(thread);
            thread.start();
            
            System.out.println("[D] Thread " + (i + 1) + " iniciada para " + IPS_SERVIDORES[i] + 
                             " (elementos " + inicio + " a " + (fim - 1) + ")");
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
        for (ContadorThreadLocal contador : contadores)
        {
            contagemTotal += contador.getResultado();
        }
        
        return contagemTotal;
    }
}

class ContadorThreadLocal implements Runnable
{
    private final String servidorInfo;
    private final byte[] parteVetor;
    private final int numeroProcurado;
    private int resultado;
    private boolean sucesso;
    
    public ContadorThreadLocal(String servidorInfo, byte[] parteVetor, int numeroProcurado)
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
                    System.out.println("[D] Resposta recebida de " + host + ":" + porta + ": " + resultado + " ocorrências");
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
