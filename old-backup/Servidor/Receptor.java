import java.io.*;
import java.net.*;

public class Receptor
{
    public static final int PORTA_PADRAO = 12345;
    
    public static void main(String[] args)
    {
        int porta = PORTA_PADRAO;
        
        if (args.length > 0)
        {
            try
            {
                porta = Integer.parseInt(args[0]);
            }
            catch (NumberFormatException e)
            {
                System.err.println("Porta inválida! Usando porta padrão: " + PORTA_PADRAO);
            }
        }
        
        System.out.println("[R] Iniciando receptor na porta " + porta);
        
        try (ServerSocket serverSocket = new ServerSocket(porta))
        {
            System.out.println("[R] Servidor ativo na porta " + porta);
            
            while (true)
            {
                System.out.println("[R] Aguardando conexão...");
                Socket conexao = serverSocket.accept();
                System.out.println("[R] Conexão estabelecida com " + conexao.getInetAddress().getHostAddress());
                
                // Criar thread para tratar a conexão
                Thread tratadora = new Thread(new TratadoraDeConexao(conexao));
                tratadora.start();
            }
        }
        catch (IOException e)
        {
            System.err.println("[R] Erro ao criar servidor: " + e.getMessage());
        }
    }
}

class TratadoraDeConexao implements Runnable
{
    private final Socket conexao;
    
    public TratadoraDeConexao(Socket conexao)
    {
        this.conexao = conexao;
    }
    
    @Override
    public void run()
    {
        ObjectInputStream receptor = null;
        ObjectOutputStream transmissor = null;
        
        try
        {
            receptor = new ObjectInputStream(conexao.getInputStream());
            transmissor = new ObjectOutputStream(conexao.getOutputStream());
            
            System.out.println("[R] Streams de comunicação estabelecidos");
            
            while (true)
            {
                try
                {
                    Comunicado comunicado = (Comunicado) receptor.readObject();
                    
                    if (comunicado instanceof Pedido)
                    {
                        Pedido pedido = (Pedido) comunicado;
                        System.out.println("[R] Pedido recebido do cliente " + conexao.getInetAddress().getHostAddress());
                        System.out.println("[R] " + pedido.toString());
                        
                        // Executar contagem
                        int contagem = pedido.contar();
                        Resposta resposta = new Resposta(contagem);
                        
                        // Enviar resposta
                        transmissor.writeObject(resposta);
                        transmissor.flush();
                        
                        System.out.println("[R] Resposta enviada: " + contagem + " ocorrências");
                    }
                    else if (comunicado instanceof ComunicadoEncerramento)
                    {
                        System.out.println("[R] Comunicado de encerramento recebido");
                        break;
                    }
                }
                catch (ClassNotFoundException e)
                {
                    System.err.println("[R] Erro ao ler objeto: " + e.getMessage());
                    break;
                }
            }
        }
        catch (IOException e)
        {
            System.err.println("[R] Erro de comunicação: " + e.getMessage());
        }
        finally
        {
            try
            {
                if (transmissor != null) transmissor.close();
                if (receptor != null) receptor.close();
                if (conexao != null) conexao.close();
                System.out.println("[R] Conexão encerrada");
            }
            catch (IOException e)
            {
                System.err.println("[R] Erro ao fechar conexão: " + e.getMessage());
            }
        }
    }
}
