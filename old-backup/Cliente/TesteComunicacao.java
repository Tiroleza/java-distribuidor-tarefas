import java.io.*;
import java.net.*;
import java.util.*;

public class TesteComunicacao
{
    public static void main(String[] args)
    {
        System.out.println("=== TESTE DE COMUNICAÇÃO SIMPLES ===");
        
        // Teste com vetor pequeno
        byte[] vetorTeste = {1, 5, 3, 5, 7, 5, 9, 2, 5, 1};
        int numeroProcurado = 5;
        
        System.out.println("Vetor de teste: " + Arrays.toString(vetorTeste));
        System.out.println("Número procurado: " + numeroProcurado);
        
        // Contagem local para verificação
        int contagemLocal = 0;
        for (byte numero : vetorTeste) {
            if (numero == numeroProcurado) {
                contagemLocal++;
            }
        }
        System.out.println("Contagem local: " + contagemLocal);
        
        // Converter para int[]
        int[] vetorInt = new int[vetorTeste.length];
        for (int i = 0; i < vetorTeste.length; i++) {
            vetorInt[i] = vetorTeste[i];
        }
        
        // Testar comunicação com servidor
        Socket conexao = null;
        ObjectOutputStream transmissor = null;
        ObjectInputStream receptor = null;
        
        try
        {
            System.out.println("\nConectando ao servidor...");
            conexao = new Socket("localhost", 12345);
            
            transmissor = new ObjectOutputStream(conexao.getOutputStream());
            receptor = new ObjectInputStream(conexao.getInputStream());
            
            System.out.println("Conexão estabelecida!");
            
            // Enviar pedido
            Pedido pedido = new Pedido(vetorInt, numeroProcurado);
            transmissor.writeObject(pedido);
            transmissor.flush();
            
            System.out.println("Pedido enviado!");
            
            // Receber resposta
            Comunicado comunicado = (Comunicado) receptor.readObject();
            if (comunicado instanceof Resposta resposta)
            {
                System.out.println("Resposta recebida: " + resposta.getContagem() + " ocorrências");
                System.out.println("Verificação: " + (resposta.getContagem() == contagemLocal ? "✓ CORRETO" : "✗ ERRO"));
            }
            
            // Enviar comunicado de encerramento
            ComunicadoEncerramento encerramento = new ComunicadoEncerramento();
            transmissor.writeObject(encerramento);
            transmissor.flush();
            
            System.out.println("Comunicado de encerramento enviado!");
            
        }
        catch (Exception e)
        {
            System.err.println("Erro na comunicação: " + e.getMessage());
        }
        finally
        {
            try
            {
                if (transmissor != null) transmissor.close();
                if (receptor != null) receptor.close();
                if (conexao != null) conexao.close();
                System.out.println("Conexão encerrada!");
            }
            catch (IOException e)
            {
                System.err.println("Erro ao fechar conexão: " + e.getMessage());
            }
        }
        
        System.out.println("\n=== TESTE CONCLUÍDO ===");
    }
}
