import java.io.*;
import java.net.*;
import java.util.concurrent.Semaphore;

/**
 * Encapsula a lógica de comunicação (Socket, ObjectStreams) e thread-safety.
 * Gerencia o fluxo bidirecional de mensagens entre cliente e servidor,
 * protegendo o buffer 'proximoComunicado' com um mutex (Semaphore).
 */
public class Parceiro
{
    private Socket             conexao;
    private ObjectInputStream  receptor;
    private ObjectOutputStream transmissor;
    
    // Buffer para armazenar o próximo comunicado recebido
    private Comunicado proximoComunicado=null;

    // Mutex (Semaphore) para garantir thread-safety no buffer 'proximoComunicado'
    // Permite que espie() e envie() sejam chamados de threads diferentes sem race condition
    private final Semaphore mutEx = new Semaphore (1,true);

    public Parceiro (Socket             conexao,
                     ObjectInputStream  receptor,
                     ObjectOutputStream transmissor)
                     throws Exception // se parametro nulos
    {
        if (conexao==null)
            throw new Exception ("Conexao ausente");

        if (receptor==null)
            throw new Exception ("Receptor ausente");

        if (transmissor==null)
            throw new Exception ("Transmissor ausente");

        this.conexao     = conexao;
        this.receptor    = receptor;
        this.transmissor = transmissor;
    }

    /**
     * Envia um comunicado para o servidor.
     * @param x Comunicado a ser enviado
     * @throws Exception Se houver erro de transmissão
     */
    public void receba (Comunicado x) throws Exception
    {
        try
        {
            this.transmissor.writeObject (x);
            this.transmissor.flush       ();
        }
        catch (IOException erro)
        {
            throw new Exception ("Erro de transmissao");
        }
    }

    /**
     * Espia o próximo comunicado sem consumi-lo.
     * Garante que o buffer 'proximoComunicado' seja acessado por apenas uma thread por vez.
     * @return O próximo comunicado disponível
     * @throws Exception Se houver erro de recepção
     */
    public Comunicado espie () throws Exception
    {
        try
        {
            this.mutEx.acquireUninterruptibly();
            if (this.proximoComunicado==null) this.proximoComunicado = (Comunicado)this.receptor.readObject();
            this.mutEx.release();
            return this.proximoComunicado;
        }
        catch (Exception erro)
        {
            throw new Exception ("Erro de recepcao");
        }
    }

    /**
     * Consome e retorna o próximo comunicado do buffer.
     * Garante que o buffer 'proximoComunicado' seja acessado por apenas uma thread por vez.
     * @return O próximo comunicado disponível
     * @throws Exception Se houver erro de recepção
     */
    public Comunicado envie () throws Exception
    {
        try
        {
            this.mutEx.acquireUninterruptibly();
            if (this.proximoComunicado==null) this.proximoComunicado = (Comunicado)this.receptor.readObject();
            Comunicado ret         = this.proximoComunicado;
            this.proximoComunicado = null;
            this.mutEx.release();
            return ret;
        }
        catch (Exception erro)
        {
            this.mutEx.release();
            throw new Exception ("Erro de recepcao");
        }
    }

    public void adeus () throws Exception
    {
        try
        {
            this.transmissor.close();
            this.receptor   .close();
            this.conexao    .close();
        }
        catch (Exception erro)
        {
            throw new Exception ("Erro de desconexao");
        }
    }
}
