import java.util.*;
import java.io.*;

/**
 * Programa sequencial de ordenação usando Merge Sort.
 * Realiza a ordenação sem paralelismo ou distribuição.
 * Usado para comparar performance com o programa distribuído.
 */
public class OrdenacaoSequencial {
    // Cores para logs
    private static final String RESET = "\033[0m";
    private static final String VERDE = "\033[32m";
    private static final String AZUL = "\033[34m";
    private static final String AMARELO = "\033[33m";
    private static final String VERMELHO = "\033[31m";
    private static final String CIANO = "\033[36m";
    
    private static byte[] vetorAtual = null;
    private static int tamanhoAtual = 0;
    
    public static void main(String[] args) {
        System.out.println(CIANO + "[SEQ] Programa de Ordenação Sequencial (Merge Sort)" + RESET);
        System.out.println(CIANO + "[SEQ] Sem paralelismo ou distribuição" + RESET);
        
        char opcao = ' ';
        do {
            System.out.println(CIANO + "\n--- MENU DE ORDENAÇÃO SEQUENCIAL ---" + RESET);
            System.out.println("[G]erar Vetor");
            System.out.println("[P]equeno (Teste - 20 elementos)");
            System.out.println("[E]xibir Vetor Atual");
            System.out.println("[O]rdenar (Sequencial)");
            System.out.println("[S]alvar em Arquivo");
            System.out.println("[T]erminar");
            System.out.print("> ");
            
            try {
                opcao = Character.toUpperCase(Teclado.getUmString().charAt(0));
            } catch (Exception e) {
                continue;
            }
            
            switch (opcao) {
                case 'G':
                    try {
                        System.out.print("Tamanho: ");
                        int tam = Teclado.getUmInt();
                        gerarVetor(tam);
                    } catch (Exception e) {
                        System.err.println(VERMELHO + "Erro ao ler tamanho." + RESET);
                    }
                    break;
                case 'P':
                    gerarVetor(20);
                    break;
                case 'E':
                    if (vetorAtual != null && vetorAtual.length <= 100) {
                        System.out.println(Arrays.toString(vetorAtual));
                    } else if (vetorAtual != null) {
                        System.out.println("Vetor com " + String.format("%,d", vetorAtual.length) + " elementos (muito grande para exibir)");
                    } else {
                        System.out.println("Vetor nulo.");
                    }
                    break;
                case 'O':
                    processarOrdenacaoSequencial();
                    break;
                case 'S':
                    if (vetorAtual != null) {
                        System.out.print("Digite o nome do arquivo: ");
                        try {
                            String nomeArq = Teclado.getUmString();
                            salvarArquivo(nomeArq, vetorAtual);
                        } catch (Exception e) {
                            System.err.println(VERMELHO + "Erro ao ler nome do arquivo." + RESET);
                        }
                    } else {
                        System.err.println(VERMELHO + "Gere e ordene o vetor antes!" + RESET);
                    }
                    break;
                case 'T':
                    System.out.println("Encerrando...");
                    break;
                default:
                    System.err.println("Opção inválida!");
            }
        } while (opcao != 'T');
    }
    
    private static void gerarVetor(int tamanho) {
        try {
            System.out.println(AZUL + "[SEQ] Gerando " + String.format("%,d", tamanho) + " elementos..." + RESET);
            vetorAtual = new byte[tamanho];
            Random r = new Random();
            r.nextBytes(vetorAtual);
            tamanhoAtual = tamanho;
            System.out.println(VERDE + "[SEQ] Vetor gerado com sucesso." + RESET);
        } catch (Exception e) {
            System.err.println(VERMELHO + "[SEQ] Erro ao gerar vetor: " + e.getMessage() + RESET);
        }
    }
    
    private static void processarOrdenacaoSequencial() {
        if (vetorAtual == null) {
            System.err.println(VERMELHO + "[SEQ] Gere o vetor antes!" + RESET);
            return;
        }
        
        System.out.println(AZUL + "[SEQ] Iniciando ordenação sequencial..." + RESET);
        System.out.println(AZUL + "[SEQ] Vetor de " + String.format("%,d", vetorAtual.length) + " elementos" + RESET);
        
        long inicioTotal = System.currentTimeMillis();
        
        // Faz uma cópia para não modificar o original (caso queira salvar depois)
        byte[] vetorParaOrdenar = Arrays.copyOf(vetorAtual, vetorAtual.length);
        
        // Ordena usando Merge Sort (mesma lógica do Pedido)
        mergeSort(vetorParaOrdenar, 0, vetorParaOrdenar.length - 1);
        
        long fimTotal = System.currentTimeMillis();
        long tempoTotal = fimTotal - inicioTotal;
        
        // Atualiza o vetor atual com o ordenado
        vetorAtual = vetorParaOrdenar;
        
        System.out.println(VERDE + "[SEQ] Ordenação concluída!" + RESET);
        System.out.println(AMARELO + "[SEQ] Tempo total de processamento: " + tempoTotal + "ms" + RESET);
        System.out.println(AMARELO + "[SEQ] Tempo total: " + String.format("%.2f", tempoTotal / 1000.0) + " segundos" + RESET);
        
        // Verifica se está ordenado (validação)
        boolean ordenado = true;
        for (int i = 1; i < vetorAtual.length; i++) {
            if (vetorAtual[i-1] > vetorAtual[i]) {
                ordenado = false;
                break;
            }
        }
        
        if (ordenado) {
            System.out.println(VERDE + "[SEQ] Validação: Vetor está ordenado corretamente!" + RESET);
        } else {
            System.err.println(VERMELHO + "[SEQ] ERRO: Vetor NÃO está ordenado!" + RESET);
        }
    }
    
    /**
     * Implementação do Merge Sort (mesma lógica do Pedido.java)
     */
    private static void mergeSort(byte[] vetor, int inicio, int fim) {
        if (inicio < fim) {
            int meio = (inicio + fim) / 2;
            mergeSort(vetor, inicio, meio);
            mergeSort(vetor, meio + 1, fim);
            merge(vetor, inicio, meio, fim);
        }
    }
    
    /**
     * Operação de intercalação (merge) de dois vetores ordenados
     */
    private static void merge(byte[] vetor, int inicio, int meio, int fim) {
        byte[] left = Arrays.copyOfRange(vetor, inicio, meio + 1);
        byte[] right = Arrays.copyOfRange(vetor, meio + 1, fim + 1);
        
        int i = 0, j = 0, k = inicio;
        
        while (i < left.length && j < right.length) {
            if (left[i] <= right[j]) {
                vetor[k++] = left[i++];
            } else {
                vetor[k++] = right[j++];
            }
        }
        
        while (i < left.length) vetor[k++] = left[i++];
        while (j < right.length) vetor[k++] = right[j++];
    }
    
    private static void salvarArquivo(String nome, byte[] vetor) {
        try (PrintWriter out = new PrintWriter(new FileWriter(nome))) {
            System.out.println(AZUL + "[SEQ] Salvando em disco..." + RESET);
            out.println(Arrays.toString(vetor));
            System.out.println(VERDE + "[SEQ] Arquivo salvo com sucesso: " + nome + RESET);
        } catch (IOException e) {
            System.err.println(VERMELHO + "[SEQ] Erro ao salvar arquivo: " + e.getMessage() + RESET);
        }
    }
}

