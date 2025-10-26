import java.util.*;
import java.util.concurrent.Semaphore;

/**
 * Thread 'worker' do cliente D.
 * Gerencia a comunicação com UM único servidor R.
 * Cada TrabalhadoraD copia um segmento do vetor e solicita ao servidor a contagem.
 */
public class TrabalhadoraD extends Thread
{
    private byte[] grandeVetor;
    private int inicio;
    private int fim;
    private Parceiro servidor;
    private int procurado;
    private int contagemParcial;
    private long tempoThread;
    private Semaphore semaforo;
    
    // Cores para logs
    private static final String RESET = "\033[0m";
    private static final String VERDE = "\033[32m";
    private static final String AZUL = "\033[34m";
    private static final String AMARELO = "\033[33m";
    private static final String VERMELHO = "\033[31m";
    private static final String CIANO = "\033[36m";
    private static final String MAGENTA = "\033[35m";
    
    public TrabalhadoraD(byte[] grandeVetor, int inicio, int fim, Parceiro servidor, int procurado, Semaphore semaforo)
    {
        this.grandeVetor = grandeVetor;
        this.inicio = inicio;
        this.fim = fim;
        this.servidor = servidor;
        this.procurado = procurado;
        this.semaforo = semaforo;
        this.contagemParcial = 0;
    }
    
    public int getContagemParcial()
    {
        return this.contagemParcial;
    }
    
    public long getTempoThread()
    {
        return this.tempoThread;
    }
    
    /**
     * Executa a contagem distribuída para este segmento.
     * Processo: copia o segmento com proteção de semáforo,
     * envia para o servidor e recebe o resultado.
     */
    @Override
    public void run()
    {
        long inicioThread = System.currentTimeMillis();
        byte[] minhaParte = null;
        
        try
        {
            // 1. Início da seção crítica (alocação de memória)
            //    Garante que apenas uma thread por vez crie sua cópia do vetor
            this.semaforo.acquire();
            
            minhaParte = Arrays.copyOfRange(this.grandeVetor, this.inicio, this.fim);
            
            // 2. Fim da seção crítica
            this.semaforo.release();
            
            System.out.println(AMARELO + "[TrabalhadoraD] Thread " + Thread.currentThread().getName() + 
                             " processando " + String.format("%,d", minhaParte.length) + " elementos" + RESET);
            
            // 3. Envio/recebimento (bloqueio de rede)
            //    Ocorre fora da seção crítica para permitir paralelismo de rede
            this.servidor.receba(new Pedido(minhaParte, this.procurado));
            Comunicado c = this.servidor.envie();
            
            if (c instanceof Resposta)
            {
                Resposta resposta = (Resposta) c;
                this.contagemParcial = resposta.getContagem();
                System.out.println(VERDE + "[TrabalhadoraD] Thread " + Thread.currentThread().getName() + 
                                 " recebeu resposta: " + this.contagemParcial + RESET);
            }
            else
            {
                System.err.println(VERMELHO + "[TrabalhadoraD] Thread " + Thread.currentThread().getName() + 
                                 " recebeu comunicado inesperado: " + c.getClass().getSimpleName() + RESET);
                this.contagemParcial = 0;
            }
            
            minhaParte = null;
            System.gc();
        }
        catch (Exception e)
        {
            System.err.println(VERMELHO + "[TrabalhadoraD] Erro na thread " + Thread.currentThread().getName() + 
                             ": " + e.getMessage() + RESET);
            this.contagemParcial = 0;
        }
        finally
        {
            // Garantir que o semáforo seja liberado mesmo em caso de erro
            if (this.semaforo.availablePermits() == 0)
            {
                this.semaforo.release();
            }
        }
        
        long fimThread = System.currentTimeMillis();
        this.tempoThread = fimThread - inicioThread;
        
        System.out.println(CIANO + "[TrabalhadoraD] Thread " + Thread.currentThread().getName() + 
                         " finalizada em " + this.tempoThread + "ms" + RESET);
    }
}
