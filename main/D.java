import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.Semaphore;

/**
 * Classe principal do Distribuidor (Cliente).
 * Gerencia a UI (Menu) e coordena as threads 'TrabalhadoraD' para processar
 * requisições em paralelo distribuídas entre múltiplos servidores.
 */
public class D
{
    // Configuração estática dos endereços dos receptores
    private static final String[] IPS_SERVIDORES = {"localhost", "localhost", "localhost"};
    private static final int[] PORTAS_SERVIDORES = {12345, 12346, 12347};
    
    // Cores para logs
    private static final String RESET = "\033[0m";
    private static final String VERDE = "\033[32m";
    private static final String AZUL = "\033[34m";
    private static final String AMARELO = "\033[33m";
    private static final String VERMELHO = "\033[31m";
    private static final String CIANO = "\033[36m";
    
    // Mutex (Semaphore) para serializar a alocação de memória do segmento.
    // (Previne OutOfMemoryError por alocação concorrente)
    private static Semaphore semaforoCopia = new Semaphore(1, true);
    
    // Variáveis de estado para manter o vetor entre operações do menu
    private static byte[] vetorAtual = null; // Guarda o vetor gerado
    private static int tamanhoAtual = 0;   // Guarda o tamanho
    
    public static void main(String[] args)
    {
        System.out.println(CIANO + "[D] Iniciando distribuidor..." + RESET);
        
        // Conectar aos servidores usando Parceiro
        List<Parceiro> servidores = new ArrayList<>();
        
        System.out.println(AZUL + "[D] Conectando aos servidores..." + RESET);
        for (int i = 0; i < IPS_SERVIDORES.length; i++)
        {
            try
            {
                System.out.println(AMARELO + "[D] Tentando conectar com " + IPS_SERVIDORES[i] + ":" + PORTAS_SERVIDORES[i] + "..." + RESET);
                Socket conexao = new Socket(IPS_SERVIDORES[i], PORTAS_SERVIDORES[i]);
                ObjectOutputStream transmissor = new ObjectOutputStream(conexao.getOutputStream());
                ObjectInputStream receptor = new ObjectInputStream(conexao.getInputStream());
                Parceiro servidor = new Parceiro(conexao, receptor, transmissor);
                
                servidores.add(servidor);
                
                System.out.println(VERDE + "[D] Conectado ao servidor " + IPS_SERVIDORES[i] + ":" + PORTAS_SERVIDORES[i] + RESET);
            }
            catch (Exception e)
            {
                System.err.println(VERMELHO + "[D] Erro ao conectar com " + IPS_SERVIDORES[i] + ":" + PORTAS_SERVIDORES[i] + " - " + e.getMessage() + RESET);
            }
        }
        
        if (servidores.isEmpty())
        {
            System.err.println(VERMELHO + "[D] Nenhum servidor disponível!" + RESET);
            return;
        }
        
        System.out.println(VERDE + "[D] Conectado a " + servidores.size() + " servidores. Menu interativo ativo!" + RESET);
        
        // Menu interativo baseado no Cliente.java
        char opcao = ' ';
        do
        {
            // Exibir menu de opções
            System.out.println(CIANO + "\n--- MENU DO DISTRIBUIDOR ---" + RESET);
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
                    System.out.println(AZUL + "[D] Calculando tamanho máximo do vetor..." + RESET);
                    int tamMax = calcularTamanhoMaximoVetor();
                    gerarVetor(tamMax);
                    break;
                    
                case 'P': // Gerar Pequeno
                    System.out.println(AZUL + "[D] Gerando vetor pequeno (20 elementos)..." + RESET);
                    gerarVetor(20);
                    break;
                    
                case 'E': // Exibir Vetor
                    if (vetorAtual != null && vetorAtual.length <= 100)
                    {
                        System.out.println(CIANO + "[D] Vetor atual: " + Arrays.toString(vetorAtual) + RESET);
                    }
                    else
                    {
                        System.err.println(AMARELO + "[D] Vetor é muito grande (>100) ou nulo para exibir." + RESET);
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
                    processarContagem(servidores, num);
                    break;
                    
                case 'Z': // Contar Zero (Inexistente '111')
                    if (vetorAtual == null)
                    {
                        System.err.println(VERMELHO + "Gere um vetor primeiro." + RESET);
                        break;
                    }
                    processarContagem(servidores, 111);
                    break;
                    
                case 'T': // Terminar
                    System.out.println(CIANO + "[D] Encerrando..." + RESET);
                    break;
                    
                default:
                    System.err.println(VERMELHO + "Opção desconhecida!" + RESET);
                    break;
            }
        }
        while (opcao != 'T');
        
        encerrarConexoes(servidores);
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
            System.out.println(AMARELO + "[D] Limitando vetor para " + String.format("%,d", tamanhoLimitado) + " elementos (3GB máximo)" + RESET);
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
            System.out.println(AMARELO + "[D] Memória estimada: " + String.format("%.2f MB", memoriaMB) + RESET);
            
            // Gerar vetor de bytes aleatórios entre -100 e 100
            long inicioGeracao = System.currentTimeMillis();
            System.out.println(AZUL + "[D] Gerando vetor de " + String.format("%,d", tamanho) + " elementos..." + RESET);
            
            vetorAtual = new byte[tamanho];
            Random random = new Random();
            for (int i = 0; i < tamanho; i++)
            {
                vetorAtual[i] = (byte)(random.nextInt(201) - 100); // -100 a 100
            }
            tamanhoAtual = tamanho;
            
            long fimGeracao = System.currentTimeMillis();
            System.out.println(VERDE + "[D] Vetor gerado em " + (fimGeracao - inicioGeracao) + "ms!" + RESET);
        }
        catch (Exception e)
        {
            System.err.println(VERMELHO + "[D] Erro ao gerar vetor: " + e.getMessage() + RESET);
            vetorAtual = null;
            tamanhoAtual = 0;
        }
    }
    
    private static void processarContagem(List<Parceiro> servidores, int numeroProcurado)
    {
        try
        {
            // Verificar se vetor foi gerado
            if (vetorAtual == null)
            {
                System.err.println(VERMELHO + "Vetor ainda não foi gerado! Use [G], [A] ou [P] primeiro." + RESET);
                return;
            }
            
            System.out.println(CIANO + "[D] Procurando pelo número: " + numeroProcurado + RESET);
            
            // Dividir vetor entre servidores usando TrabalhadoraD
            int tamanhoParte = vetorAtual.length / servidores.size();
            
            System.out.println(AZUL + "[D] Processando vetor em " + servidores.size() + " partes paralelas..." + RESET);
            
            long inicioProcessamento = System.currentTimeMillis();
            
            // 1. Iniciar todas as tarefas
            System.out.println(CIANO + "[D] FASE 1: Iniciando todas as threads..." + RESET);
            TrabalhadoraD[] threads = new TrabalhadoraD[servidores.size()];
            
            for (int i = 0; i < servidores.size(); i++)
            {
                final int inicio = i * tamanhoParte;
                final int fim = (i == servidores.size() - 1) ? vetorAtual.length : (i + 1) * tamanhoParte;
                
                threads[i] = new TrabalhadoraD(vetorAtual, inicio, fim, servidores.get(i), numeroProcurado, semaforoCopia);
                threads[i].start();
                System.out.println(VERDE + "[D] Thread " + i + " iniciada (start())" + RESET);
            }
            
            // 2. Sincronizar (aguardar) todas as tarefas
            System.out.println(CIANO + "[D] FASE 2: Aguardando todas as threads..." + RESET);
            for (int i = 0; i < threads.length; i++)
            {
                try
                {
                    threads[i].join();
                    System.out.println(VERDE + "[D] Thread " + i + " finalizada (join())" + RESET);
                }
                catch (InterruptedException e)
                {
                    System.err.println(VERMELHO + "[D] Erro ao aguardar Thread " + i + ": " + e.getMessage() + RESET);
                }
            }
            
            System.out.println(VERDE + "[D] Todas as threads finalizadas!" + RESET);
            
            long fimProcessamento = System.currentTimeMillis();
            long tempoTotal = fimProcessamento - inicioProcessamento;
            
            // 3. Consolidar resultados parciais de todas as threads
            int contagemTotal = 0;
            for (int i = 0; i < threads.length; i++)
            {
                contagemTotal += threads[i].getContagemParcial();
            }
            
            // 4. Exibir resultados e métricas de tempo
            System.out.println(VERDE + "[D] Contagem final: " + contagemTotal + RESET);
            System.out.println(CIANO + "[D] MÉTRICAS DE TEMPO:" + RESET);
            System.out.println(AMARELO + "[D]   - Tempo total de processamento: " + tempoTotal + "ms" + RESET);
            
            for (int i = 0; i < threads.length; i++)
            {
                System.out.println(AMARELO + "[D]   - Thread " + i + ": " + threads[i].getTempoThread() + "ms" + RESET);
            }
            
            // 5. Retornar ao menu principal
            System.out.println(VERDE + "[D] Tarefa concluída!" + RESET);
        }
        catch (Exception e)
        {
            System.err.println(VERMELHO + "[D] Erro ao processar novo vetor: " + e.getMessage() + RESET);
        }
    }
    
    private static void encerrarConexoes(List<Parceiro> servidores)
    {
        System.out.println(AMARELO + "[D] Encerrando conexões..." + RESET);
        
        // Enviar comunicado de encerramento para todos os servidores
        for (int i = 0; i < servidores.size(); i++)
        {
            try
            {
                servidores.get(i).receba(new ComunicadoEncerramento());
                servidores.get(i).adeus();
                System.out.println(VERDE + "[D] Servidor " + i + " encerrado." + RESET);
            }
            catch (Exception e)
            {
                System.err.println(VERMELHO + "[D] Erro ao encerrar conexão com servidor " + i + ": " + e.getMessage() + RESET);
            }
        }
        
        System.out.println(CIANO + "[D] Conexões encerradas!" + RESET);
    }
}
