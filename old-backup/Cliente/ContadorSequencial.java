import java.util.*;

public class ContadorSequencial
{
    public static void main(String[] args)
    {
        Scanner scanner = new Scanner(System.in);
        
        System.out.println("=== CONTADOR SEQUENCIAL (COMPARAÇÃO DE PERFORMANCE) ===");
        
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
        
        System.out.println("\n[SEQ] Iniciando contagem sequencial...");
        System.out.println("[SEQ] Procurando número: " + numeroProcurado);
        
        // Medir tempo de execução
        long inicioTempo = System.currentTimeMillis();
        
        // Executar contagem sequencial
        int contagemTotal = executarContagemSequencial(vetorGrande, numeroProcurado);
        
        long fimTempo = System.currentTimeMillis();
        long tempoExecucao = fimTempo - inicioTempo;
        
        System.out.println("\n=== RESULTADO FINAL (SEQUENCIAL) ===");
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
        System.out.println("[SEQ] Gerando vetor de " + tamanho + " elementos...");
        byte[] vetor = new byte[tamanho];
        Random random = new Random();
        
        for (int i = 0; i < tamanho; i++)
        {
            // Números aleatórios entre -100 e 100
            vetor[i] = (byte) (random.nextInt(201) - 100);
        }
        
        System.out.println("[SEQ] Vetor gerado com sucesso!");
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
    
    private static int executarContagemSequencial(byte[] vetor, int numeroProcurado)
    {
        System.out.println("[SEQ] Executando contagem sequencial...");
        
        int contagem = 0;
        for (byte numero : vetor)
        {
            if (numero == numeroProcurado)
            {
                contagem++;
            }
        }
        
        System.out.println("[SEQ] Contagem sequencial concluída!");
        return contagem;
    }
}
