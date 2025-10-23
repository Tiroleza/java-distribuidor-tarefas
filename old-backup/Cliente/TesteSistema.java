import java.util.*;

public class TesteSistema
{
    public static void main(String[] args)
    {
        System.out.println("=== TESTE DO SISTEMA DISTRIBUÍDO DE CONTAGEM ===");
        
        // Teste com vetor pequeno para demonstração
        byte[] vetorTeste = {1, 5, 3, 5, 7, 5, 9, 2, 5, 1};
        int numeroProcurado = 5;
        
        System.out.println("Vetor de teste: " + Arrays.toString(vetorTeste));
        System.out.println("Número procurado: " + numeroProcurado);
        
        // Contagem sequencial para verificação
        int contagemSequencial = contarSequencial(vetorTeste, numeroProcurado);
        System.out.println("Contagem sequencial: " + contagemSequencial);
        
        // Teste da classe Pedido
        System.out.println("\n=== TESTE DA CLASSE PEDIDO ===");
        // Converter byte[] para int[]
        int[] vetorTesteInt = new int[vetorTeste.length];
        for (int i = 0; i < vetorTeste.length; i++) {
            vetorTesteInt[i] = vetorTeste[i];
        }
        Pedido pedido = new Pedido(vetorTesteInt, numeroProcurado);
        int contagemPedido = pedido.contar();
        System.out.println("Contagem via Pedido.contar(): " + contagemPedido);
        
        // Teste da classe Resposta
        System.out.println("\n=== TESTE DA CLASSE RESPOSTA ===");
        Resposta resposta = new Resposta(contagemPedido);
        System.out.println("Resposta criada: " + resposta.toString());
        System.out.println("Valor da contagem: " + resposta.getContagem());
        
        // Teste com número inexistente
        System.out.println("\n=== TESTE COM NÚMERO INEXISTENTE ===");
        int numeroInexistente = 111;
        Pedido pedidoInexistente = new Pedido(vetorTesteInt, numeroInexistente);
        int contagemInexistente = pedidoInexistente.contar();
        System.out.println("Procurando número " + numeroInexistente + ": " + contagemInexistente + " ocorrências");
        
        // Teste de geração de vetor grande
        System.out.println("\n=== TESTE DE GERAÇÃO DE VETOR GRANDE ===");
        int tamanho = 1000;
        byte[] vetorGrande = gerarVetor(tamanho);
        System.out.println("Vetor de " + tamanho + " elementos gerado");
        
        // Contar um número específico no vetor grande
        int numeroTeste = 50;
        long inicio = System.currentTimeMillis();
        int contagemGrande = contarSequencial(vetorGrande, numeroTeste);
        long fim = System.currentTimeMillis();
        
        System.out.println("Procurando " + numeroTeste + " em vetor de " + tamanho + " elementos:");
        System.out.println("Resultado: " + contagemGrande + " ocorrências");
        System.out.println("Tempo: " + (fim - inicio) + " ms");
        
        System.out.println("\n=== TESTE CONCLUÍDO COM SUCESSO ===");
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
}
