import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.Semaphore;

public class D
{
    private static final String[] IPS_SERVIDORES = {"localhost", "localhost", "localhost"};
    private static final int[] PORTAS_SERVIDORES = {12345, 12346, 12347};
    
    // Cores
    private static final String RESET = "\033[0m";
    private static final String VERDE = "\033[32m";
    private static final String AZUL = "\033[34m";
    private static final String AMARELO = "\033[33m";
    private static final String VERMELHO = "\033[31m";
    private static final String CIANO = "\033[36m";
    
    private static Semaphore semaforoCopia = new Semaphore(1, true);
    private static byte[] vetorAtual = null;
    private static int tamanhoAtual = 0;
    
    public static void main(String[] args)
    {
        System.out.println(CIANO + "[D] Iniciando Distribuidor de Ordenação..." + RESET);
        List<Parceiro> servidores = new ArrayList<>();
        
        // --- Conexão (Igual ao anterior) ---
        System.out.println(AZUL + "[D] Conectando aos servidores..." + RESET);
        for (int i = 0; i < IPS_SERVIDORES.length; i++) {
            try {
                Socket conexao = new Socket(IPS_SERVIDORES[i], PORTAS_SERVIDORES[i]);
                ObjectOutputStream transmissor = new ObjectOutputStream(conexao.getOutputStream());
                ObjectInputStream receptor = new ObjectInputStream(conexao.getInputStream());
                servidores.add(new Parceiro(conexao, receptor, transmissor));
                System.out.println(VERDE + "[D] Conectado a " + IPS_SERVIDORES[i] + ":" + PORTAS_SERVIDORES[i] + RESET);
            } catch (Exception e) {
                System.err.println(VERMELHO + "[D] Falha ao conectar: " + e.getMessage() + RESET);
            }
        }
        
        if (servidores.isEmpty()) return;
        
        char opcao = ' ';
        do {
            System.out.println(CIANO + "\n--- MENU DE ORDENAÇÃO DISTRIBUÍDA ---" + RESET);
            System.out.println("[G]erar Vetor");
            System.out.println("[P]equeno (Teste)");
            System.out.println("[E]xibir Vetor Atual");
            System.out.println("[O]rdenar (Distribuído)");
            System.out.println("[T]erminar");
            System.out.print("> ");
            
            try { opcao = Character.toUpperCase(Teclado.getUmString().charAt(0)); }
            catch (Exception e) { continue; }
            
            switch (opcao) {
                case 'G':
                    try {
                        System.out.print("Tamanho: ");
                        int tam = Teclado.getUmInt();
                        gerarVetor(tam);
                    } catch (Exception e) {}
                    break;
                case 'P': gerarVetor(20); break;
                case 'E':
                    if (vetorAtual != null && vetorAtual.length <= 100)
                        System.out.println(Arrays.toString(vetorAtual));
                    else System.out.println("Vetor nulo ou muito grande.");
                    break;
                case 'O':
                    processarOrdenacao(servidores);
                    break;
                case 'T': System.out.println("Encerrando..."); break;
            }
        } while (opcao != 'T');
        
        encerrarConexoes(servidores);
    }
    
    private static void gerarVetor(int tamanho) {
        // (Código idêntico ao anterior para gerar vetor aleatório)
        try {
            System.out.println(AZUL + "Gerando " + tamanho + " elementos..." + RESET);
            vetorAtual = new byte[tamanho];
            Random r = new Random();
            r.nextBytes(vetorAtual); // Jeito rápido de gerar bytes
            tamanhoAtual = tamanho;
            System.out.println(VERDE + "Vetor gerado." + RESET);
        } catch (Exception e) {
            System.err.println("Erro ao gerar vetor.");
        }
    }
    
    private static void processarOrdenacao(List<Parceiro> servidores)
    {
        if (vetorAtual == null) {
            System.err.println(VERMELHO + "Gere o vetor antes!" + RESET);
            return;
        }
        
        System.out.println(AZUL + "[D] Iniciando Ordenação Distribuída..." + RESET);
        long inicioTotal = System.currentTimeMillis();
        
        // 1. Scatter: Enviar partes para servidores
        int tamanhoParte = vetorAtual.length / servidores.size();
        TrabalhadoraD[] threads = new TrabalhadoraD[servidores.size()];
        
        for (int i = 0; i < servidores.size(); i++) {
            final int inicio = i * tamanhoParte;
            final int fim = (i == servidores.size() - 1) ? vetorAtual.length : (i + 1) * tamanhoParte;
            
            // Note que removemos 'procurado' do construtor
            threads[i] = new TrabalhadoraD(vetorAtual, inicio, fim, servidores.get(i), semaforoCopia);
            threads[i].start();
        }
        
        // 2. Join: Esperar resultados
        for (int i = 0; i < threads.length; i++) {
            try { threads[i].join(); } catch (Exception e) {}
        }
        
        // 3. Gather: Intercalar (Merge) os vetores recebidos
        System.out.println(CIANO + "[D] Intercalando resultados dos servidores..." + RESET);
        
        // Pega a parte do primeiro servidor como base
        byte[] vetorFinal = threads[0].getVetorOrdenadoParcial();
        
        if (vetorFinal == null) {
            System.err.println(VERMELHO + "Erro: Servidor retornou nulo." + RESET);
            return;
        }

        // Intercala sequencialmente com os demais
        for (int i = 1; i < threads.length; i++) {
            byte[] parte = threads[i].getVetorOrdenadoParcial();
            if (parte != null) {
                vetorFinal = intercalar(vetorFinal, parte);
            }
        }
        
        long fimTotal = System.currentTimeMillis();
        System.out.println(VERDE + "[D] Ordenação concluída! Tempo total: " + (fimTotal - inicioTotal) + "ms" + RESET);
        
        // 4. Salvar em Arquivo
        System.out.print("Digite o nome do arquivo para salvar o resultado: ");
        try {
            String nomeArq = Teclado.getUmString();
            salvarArquivo(nomeArq, vetorFinal);
        } catch (Exception e) {}
    }

    // Método Utilitário para Intercalar (Merge) - O mesmo usado no servidor
    private static byte[] intercalar(byte[] A, byte[] B) {
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
    
    private static void salvarArquivo(String nome, byte[] vetor) {
        try (PrintWriter out = new PrintWriter(new FileWriter(nome))) {
            System.out.println(AZUL + "Salvando em disco..." + RESET);
            out.println(Arrays.toString(vetor)); // Ou loop para formatar como quiser
            System.out.println(VERDE + "Arquivo salvo com sucesso!" + RESET);
        } catch (IOException e) {
            System.err.println("Erro ao salvar arquivo: " + e.getMessage());
        }
    }

    private static void encerrarConexoes(List<Parceiro> servidores) {
        // (Mesmo código de encerramento do anterior)
        for (Parceiro p : servidores) {
            try { p.receba(new ComunicadoEncerramento()); p.adeus(); } catch(Exception e){}
        }
    }
}