import java.util.*;

/**
 * Programa de benchmark sequencial.
 * Usado para comparar performance com o 'D.java' distribuído.
 * Executa a contagem em uma única thread local sem comunicação de rede.
 */
public class ContagemSequencial
{
    // Cores para logs
    private static final String RESET = "\033[0m";
    private static final String VERDE = "\033[32m";
    private static final String AZUL = "\033[34m";
    private static final String AMARELO = "\033[33m";
    private static final String VERMELHO = "\033[31m";
    private static final String CIANO = "\033[36m";
    
    // Variáveis de estado para manter o vetor entre operações do menu
    private static byte[] vetorAtual = null; // Guarda o vetor gerado
    private static int tamanhoAtual = 0;   // Guarda o tamanho
    
    public static void main(String[] args)
    {
        System.out.println(CIANO + "[SEQ] Iniciando contagem sequencial..." + RESET);
        
        System.out.println(VERDE + "[SEQ] Sistema sequencial ativo! Menu interativo disponível." + RESET);
        
        // Menu interativo baseado no Cliente.java
        char opcao = ' ';
        do
        {
            // Exibir menu de opções
            System.out.println(CIANO + "\n--- MENU DO CONTADOR SEQUENCIAL ---" + RESET);
            System.out.println("[G]erar Vetor (Tamanho Manual)");
            System.out.println("[A]uto-Tamanho (Gerar Vetor Máximo)");
            System.out.println("[P]equeno (Gerar Vetor de Teste, 20 elementos)");
            System.out.println("[E]xibir Vetor Atual");
            System.out.println("[C]ontar (Número Aleatório do Vetor)");
            System.out.println("[Z]ero (Contar Número Inexistente '111')");
            System.out.println("[T]erminar");
            System.out.print("> ");
            
            // Capturar entrada usando Teclado.java
            try
            {
                opcao = Character.toUpperCase(Teclado.getUmString().charAt(0));
            }
            catch (Exception e)
            {
                System.err.println(VERMELHO + "Opção inválida!" + RESET);
                continue;
            }
            
            // Processar opção
            switch (opcao)
            {
                case 'G': // Gerar Manual
                    try
                    {
                        System.out.print(CIANO + "Digite o tamanho do vetor: " + RESET);
                        int tam = Teclado.getUmInt();
                        gerarVetor(tam);
                    }
                    catch (Exception e)
                    {
                        System.err.println(VERMELHO + "Tamanho inválido." + RESET);
                    }
                    break;
                    
                case 'A': // Gerar Auto (Máximo)
                    System.out.println(AZUL + "[SEQ] Calculando tamanho máximo do vetor..." + RESET);
                    int tamMax = calcularTamanhoMaximoVetor();
                    gerarVetor(tamMax);
                    break;
                    
                case 'P': // Gerar Pequeno
                    System.out.println(AZUL + "[SEQ] Gerando vetor pequeno (20 elementos)..." + RESET);
                    gerarVetor(20);
                    break;
                    
                case 'E': // Exibir Vetor
                    if (vetorAtual != null && vetorAtual.length <= 100)
                    {
                        System.out.println(CIANO + "[SEQ] Vetor atual: " + Arrays.toString(vetorAtual) + RESET);
                    }
                    else
                    {
                        System.err.println(AMARELO + "[SEQ] Vetor é muito grande (>100) ou nulo para exibir." + RESET);
                    }
                    break;
                    
                case 'C': // Contar Aleatório
                    if (vetorAtual == null)
                    {
                        System.err.println(VERMELHO + "Gere um vetor primeiro." + RESET);
                        break;
                    }
                    Random random = new Random();
                    int pos = random.nextInt(tamanhoAtual);
                    int num = vetorAtual[pos];
                    processarContagemSequencial(num);
                    break;
                    
                case 'Z': // Contar Zero (Inexistente '111')
                    if (vetorAtual == null)
                    {
                        System.err.println(VERMELHO + "Gere um vetor primeiro." + RESET);
                        break;
                    }
                    processarContagemSequencial(111);
                    break;
                    
                case 'T': // Terminar
                    System.out.println(CIANO + "[SEQ] Encerrando..." + RESET);
                    break;
                    
                default:
                    System.err.println(VERMELHO + "Opção desconhecida!" + RESET);
                    break;
            }
        }
        while (opcao != 'T');
        
        System.out.println(CIANO + "[SEQ] Programa encerrado!" + RESET);
    }
    
    private static int calcularTamanhoMaximoVetor()
    {
        System.out.println(AZUL + "[SEQ] Estimando o maior tamanho possível de vetor em Java..." + RESET);
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
                System.out.println(VERDE + "[SEQ] Alocado com sucesso: " + String.format("%,d", ultimoBemSucedido) + " elementos" + RESET);
            }
            catch (OutOfMemoryError e)
            {
                System.out.println(VERMELHO + "[SEQ] Falhou em " + String.format("%,d", tamanho) + " elementos" + RESET);
                break;
            }
        }
        
        long fim = System.currentTimeMillis();
        System.out.println(CIANO + "[SEQ] Maior vetor que coube (aproximadamente): " + String.format("%,d", ultimoBemSucedido) + RESET);
        System.out.println(AMARELO + "[SEQ] Memória estimada: " + String.format("%.2f MB", ultimoBemSucedido * 1.0 / (1024 * 1024)) + RESET);
        System.out.println(AMARELO + "[SEQ] Tempo total: " + String.format("%.2f segundos", (fim - inicio) / 1000.0) + RESET);
        
        // Limitar tamanho para 2GB (2 bilhões de bytes)
        int tamanhoLimitado = Math.min(ultimoBemSucedido, 2_000_000_000); // Máximo 2GB (limite do int)
        if (tamanhoLimitado < ultimoBemSucedido)
        {
            System.out.println(AMARELO + "[SEQ]  Limitando vetor para " + String.format("%,d", tamanhoLimitado) + " elementos (2GB máximo)" + RESET);
        }
        
        return tamanhoLimitado;
    }
    
    private static void gerarVetor(int tamanho)
    {
        try
        {
            // Calcular estimativa de memória
            long memoriaEstimada = tamanho * 1L; // 1 byte por elemento
            double memoriaMB = memoriaEstimada / (1024.0 * 1024.0);
            System.out.println(AMARELO + "[SEQ] Memória estimada: " + String.format("%.2f MB", memoriaMB) + RESET);
            
            // Gerar vetor de bytes aleatórios entre -100 e 100
            long inicioGeracao = System.currentTimeMillis();
            System.out.println(AZUL + "[SEQ] Gerando vetor de " + String.format("%,d", tamanho) + " elementos..." + RESET);
            
            vetorAtual = new byte[tamanho];
            Random random = new Random();
            for (int i = 0; i < tamanho; i++)
            {
                vetorAtual[i] = (byte)(random.nextInt(201) - 100); // -100 a 100
            }
            tamanhoAtual = tamanho;
            
            long fimGeracao = System.currentTimeMillis();
            System.out.println(VERDE + "[SEQ] Vetor gerado em " + (fimGeracao - inicioGeracao) + "ms!" + RESET);
        }
        catch (Exception e)
        {
            System.err.println(VERMELHO + "[SEQ] Erro ao gerar vetor: " + e.getMessage() + RESET);
            vetorAtual = null;
            tamanhoAtual = 0;
        }
    }
    
    /**
     * Processa a contagem localmente em uma única thread.
     * Mede o tempo de execução.
     */
    private static void processarContagemSequencial(int numeroProcurado)
    {
        // 1. Validar se o vetor existe
        if (vetorAtual == null)
        {
            System.err.println(VERMELHO + "Vetor ainda não foi gerado! Use [G], [A] ou [P] primeiro." + RESET);
            return;
        }

        System.out.println(CIANO + "[SEQ] Iniciando contagem sequencial para o número: " + numeroProcurado + RESET);
        System.out.println(AZUL + "[SEQ] Processando " + String.format("%,d", vetorAtual.length) + " elementos em 1 thread..." + RESET);

        int contagemTotal = 0;

        // 2. Medir o tempo
        long inicioProcessamento = System.currentTimeMillis();

        // 3. Executar a contagem em um loop simples (sem paralelismo)
        try
        {
            for (int i = 0; i < vetorAtual.length; i++)
            {
                if (vetorAtual[i] == numeroProcurado)
                {
                    contagemTotal++;
                }
            }
        }
        catch (Exception e)
        {
            System.err.println(VERMELHO + "[SEQ] Erro durante a contagem: " + e.getMessage() + RESET);
            return;
        }

        long fimProcessamento = System.currentTimeMillis();
        long tempoTotal = fimProcessamento - inicioProcessamento;

        // 4. Exibir resultados
        System.out.println(VERDE + "[SEQ] Contagem final: " + contagemTotal + RESET);
        System.out.println(CIANO + "[SEQ] MÉTRICAS DE TEMPO (SEQUENCIAL):" + RESET);
        System.out.println(AMARELO + "[SEQ]   - Tempo total de processamento: " + tempoTotal + "ms" + RESET);
        
        // Tarefa concluída, voltando ao menu
        System.out.println(VERDE + "[SEQ] Tarefa concluída!" + RESET);
    }
}