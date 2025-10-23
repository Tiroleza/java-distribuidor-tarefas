import java.io.*;
import java.net.*;
import java.util.*;

public class Distribuidor
{
    public static final int PORTA_PADRAO = 12345;
    
    // IPs dos servidores R (hard coded conforme especificação)
    private static final String[] IPS_SERVIDORES = {
        "localhost",  // Para teste local
        "192.168.1.100",  // Exemplo de IPs de servidores
        "192.168.1.101",
        "192.168.1.102"
    };
    
    public static void main(String[] args)
    {
        Scanner scanner = new Scanner(System.in);
        
        System.out.println("=== SISTEMA DISTRIBUÍDO DE CONTAGEM ===");
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
        
        // Perguntar se deseja nova rodada
        if (solicitarNovaRodada(scanner))
        {
            main(args); // Recursão para nova rodada
        }
        else
        {
            System.out.println("[D] Encerrando sistema...");
            scanner.close();
        }
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
        List<ContadorThread> contadores = new ArrayList<>();
        
        // Criar threads para cada servidor
        for (int i = 0; i < numServidores; i++)
        {
            int inicio = i * tamanhoParte;
            int fim = (i == numServidores - 1) ? vetor.length : (i + 1) * tamanhoParte;
            
            byte[] parteVetor = Arrays.copyOfRange(vetor, inicio, fim);
            
            ContadorThread contador = new ContadorThread(IPS_SERVIDORES[i], parteVetor, numeroProcurado);
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
        for (ContadorThread contador : contadores)
        {
            contagemTotal += contador.getResultado();
        }
        
        return contagemTotal;
    }
    
    private static boolean solicitarNovaRodada(Scanner scanner)
    {
        System.out.print("\nDeseja executar uma nova rodada? (s/n): ");
        String resposta = scanner.next().toLowerCase();
        return resposta.equals("s") || resposta.equals("sim");
    }
}

class ContadorThread implements Runnable
{
    private final String ipServidor;
    private final byte[] parteVetor;
    private final int numeroProcurado;
    private int resultado;
    private boolean sucesso;
    
    public ContadorThread(String ipServidor, byte[] parteVetor, int numeroProcurado)
    {
        this.ipServidor = ipServidor;
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
            System.out.println("[D] Conectando ao servidor " + ipServidor + "...");
            conexao = new Socket(ipServidor, Distribuidor.PORTA_PADRAO);
            
            transmissor = new ObjectOutputStream(conexao.getOutputStream());
            receptor = new ObjectInputStream(conexao.getInputStream());
            
            System.out.println("[D] Conexão estabelecida com " + ipServidor);
            
            // Converter byte[] para int[]
            int[] parteVetorInt = new int[parteVetor.length];
            for (int i = 0; i < parteVetor.length; i++) {
                parteVetorInt[i] = parteVetor[i];
            }
            
            // Enviar pedido
            Pedido pedido = new Pedido(parteVetorInt, numeroProcurado);
            transmissor.writeObject(pedido);
            transmissor.flush();
            
            System.out.println("[D] Pedido enviado para " + ipServidor);
            
            // Receber resposta
            Comunicado comunicado = (Comunicado) receptor.readObject();
            if (comunicado instanceof Resposta)
            {
                Resposta resposta = (Resposta) comunicado;
                this.resultado = resposta.getContagem();
                this.sucesso = true;
                System.out.println("[D] Resposta recebida de " + ipServidor + ": " + resultado + " ocorrências");
            }
        }
        catch (Exception e)
        {
            System.err.println("[D] Erro na comunicação com " + ipServidor + ": " + e.getMessage());
            this.resultado = 0;
            this.sucesso = false;
        }
        finally
        {
            try
            {
                if (transmissor != null) transmissor.close();
                if (receptor != null) receptor.close();
                if (conexao != null) conexao.close();
            }
            catch (IOException e)
            {
                System.err.println("[D] Erro ao fechar conexão com " + ipServidor + ": " + e.getMessage());
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
