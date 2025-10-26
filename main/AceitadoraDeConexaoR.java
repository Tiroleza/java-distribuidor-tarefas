import java.net.*;
import java.util.*;

/**
 * Thread dedicada para escutar por novas conexões de clientes (serverSocket.accept()).
 * Para cada conexão aceita, delega o gerenciamento para uma instância de SupervisoraDeConexaoR.
 */
public class AceitadoraDeConexaoR extends Thread
{
    private ServerSocket        pedido;
    private ArrayList<Parceiro> usuarios;

    public AceitadoraDeConexaoR
    (String porta, ArrayList<Parceiro> usuarios)
    throws Exception
    {
        if (porta==null)
            throw new Exception ("Porta ausente");

        try
        {
            this.pedido =
            new ServerSocket (Integer.parseInt(porta));
        }
        catch (Exception  erro)
        {
            throw new Exception ("Porta invalida");
        }

        if (usuarios==null)
            throw new Exception ("Usuarios ausentes");

        this.usuarios = usuarios;
    }

    /**
     * Loop infinito para aceitar conexões.
     * Para cada conexão aceita, delega para uma nova SupervisoraDeConexaoR.
     */
    @Override
    public void run ()
    {
        for(;;)
        {
            // 1. Aguardar uma nova conexão de cliente
            Socket conexao=null;
            try
            {
                conexao = this.pedido.accept();
            }
            catch (Exception erro)
            {
                continue;
            }

            // 2. Conexão aceita. Criar e iniciar uma SupervisoraDeConexaoR para gerenciar.
            SupervisoraDeConexaoR supervisoraDeConexao=null;
            try
            {
                supervisoraDeConexao =
                new SupervisoraDeConexaoR (conexao, usuarios);
            }
            catch (Exception erro)
            {} // sei que passei parametros corretos para o construtor
            supervisoraDeConexao.start();
        }
    }
}
