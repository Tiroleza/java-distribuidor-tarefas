import java.util.*;

public class SistemaContagem
{
    public static void main(String[] args)
    {
        Scanner scanner = new Scanner(System.in);
        
        System.out.println("=== SISTEMA DISTRIBUÍDO DE CONTAGEM ===");
        System.out.println("Processadores disponíveis: " + Runtime.getRuntime().availableProcessors());
        System.out.println();
        
        while (true)
        {
            exibirMenu();
            
            try
            {
                int opcao = scanner.nextInt();
                scanner.nextLine(); // Limpar buffer
                
                switch (opcao)
                {
                    case 1:
                        executarContagemDistribuida(scanner);
                        break;
                    case 2:
                        executarContagemSequencial(scanner);
                        break;
                    case 3:
                        executarTesteSistema();
                        break;
                    case 4:
                        executarComparacaoPerformance(scanner);
                        break;
                    case 5:
                        System.out.println("Saindo do sistema...");
                        scanner.close();
                        return;
                    default:
                        System.out.println("Opção inválida! Tente novamente.");
                }
            }
            catch (InputMismatchException e)
            {
                System.out.println("Entrada inválida! Digite um número.");
                scanner.nextLine(); // Limpar buffer
            }
            
            System.out.println("\n" + "=".repeat(50) + "\n");
        }
    }
    
    private static void exibirMenu()
    {
        System.out.println("Escolha uma opção:");
        System.out.println("1. Executar contagem distribuída");
        System.out.println("2. Executar contagem sequencial");
        System.out.println("3. Executar teste do sistema");
        System.out.println("4. Comparação de performance");
        System.out.println("5. Sair");
        System.out.print("Opção: ");
    }
    
    private static void executarContagemDistribuida(Scanner scanner)
    {
        System.out.println("\n=== CONTAGEM DISTRIBUÍDA ===");
        System.out.println("IMPORTANTE: Certifique-se de que os servidores R estão rodando!");
        System.out.println("Para testar localmente, execute várias instâncias do Receptor em portas diferentes.");
        
        // Chamar o Distribuidor
        String[] args = {};
        Distribuidor.main(args);
    }
    
    private static void executarContagemSequencial(Scanner scanner)
    {
        System.out.println("\n=== CONTAGEM SEQUENCIAL ===");
        
        // Chamar o ContadorSequencial
        String[] args = {};
        ContadorSequencial.main(args);
    }
    
    private static void executarTesteSistema()
    {
        System.out.println("\n=== TESTE DO SISTEMA ===");
        
        // Chamar o TesteSistema
        String[] args = {};
        TesteSistema.main(args);
    }
    
    private static void executarComparacaoPerformance(Scanner scanner)
    {
        System.out.println("\n=== COMPARAÇÃO DE PERFORMANCE ===");
        
        // Solicitar tamanho do vetor
        int tamanhoVetor = solicitarTamanhoVetor(scanner);
        
        // Gerar vetor uma única vez
        byte[] vetor = gerarVetor(tamanhoVetor);
        int numeroProcurado = 50; // Número fixo para teste
        
        System.out.println("Testando com vetor de " + tamanhoVetor + " elementos, procurando número " + numeroProcurado);
        
        // Executar contagem sequencial
        System.out.println("\nExecutando contagem sequencial...");
        long inicioSeq = System.currentTimeMillis();
        int resultadoSeq = contarSequencial(vetor, numeroProcurado);
        long fimSeq = System.currentTimeMillis();
        long tempoSeq = fimSeq - inicioSeq;
        
        System.out.println("Resultado sequencial: " + resultadoSeq + " ocorrências em " + tempoSeq + " ms");
        
        // Nota sobre contagem distribuída
        System.out.println("\nPara testar a contagem distribuída, execute a opção 1 do menu principal.");
        System.out.println("A performance distribuída dependerá do número de servidores disponíveis.");
        
        // Calcular estimativa teórica
        int numProcessadores = Runtime.getRuntime().availableProcessors();
        System.out.println("\nCom " + numProcessadores + " processadores, a contagem distribuída poderia ser");
        System.out.println("aproximadamente " + numProcessadores + "x mais rápida em condições ideais.");
    }
    
    private static int solicitarTamanhoVetor(Scanner scanner)
    {
        System.out.print("Digite o tamanho do vetor para teste: ");
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
    
    private static byte[] gerarVetor(int tamanho)
    {
        byte[] vetor = new byte[tamanho];
        Random random = new Random();
        
        for (int i = 0; i < tamanho; i++)
        {
            vetor[i] = (byte) (random.nextInt(201) - 100);
        }
        
        return vetor;
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
