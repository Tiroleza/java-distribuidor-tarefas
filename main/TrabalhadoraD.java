import java.util.*;
import java.util.concurrent.Semaphore;

public class TrabalhadoraD extends Thread
{
    private byte[] grandeVetor;
    private int inicio;
    private int fim;
    private Parceiro servidor;
    // Procurado removido, pois agora é ordenação completa
    private byte[] vetorOrdenadoParcial;
    private long tempoThread;
    private Semaphore semaforo;
    
    // Cores
    private static final String RESET = "\033[0m";
    private static final String VERDE = "\033[32m";
    private static final String AMARELO = "\033[33m";
    private static final String VERMELHO = "\033[31m";
    private static final String CIANO = "\033[36m";
    
    public TrabalhadoraD(byte[] grandeVetor, int inicio, int fim, Parceiro servidor, Semaphore semaforo)
    {
        this.grandeVetor = grandeVetor;
        this.inicio = inicio;
        this.fim = fim;
        this.servidor = servidor;
        this.semaforo = semaforo;
        this.vetorOrdenadoParcial = null;
    }
    
    public byte[] getVetorOrdenadoParcial()
    {
        return this.vetorOrdenadoParcial;
    }
    
    public long getTempoThread()
    {
        return this.tempoThread;
    }
    
    @Override
    public void run()
    {
        long inicioThread = System.currentTimeMillis();
        byte[] minhaParte = null;
        
        try
        {
            // 1. Seção Crítica: Copiar fatia
            this.semaforo.acquire();
            minhaParte = Arrays.copyOfRange(this.grandeVetor, this.inicio, this.fim);
            this.semaforo.release();
            
            System.out.println(AMARELO + "[TrabalhadoraD] Thread " + Thread.currentThread().getName() + 
                             " enviando " + String.format("%,d", minhaParte.length) + " elementos" + RESET);
            
            // 2. Enviar Pedido (Vetor para ordenar)
            this.servidor.receba(new Pedido(minhaParte)); // Pedido agora só recebe o vetor
            
            // 3. Receber Resposta (Vetor Ordenado)
            Comunicado c = this.servidor.envie();
            
            if (c instanceof Resposta)
            {
                Resposta resposta = (Resposta) c;
                this.vetorOrdenadoParcial = resposta.getVetorOrdenado(); // Método novo na classe Resposta
                System.out.println(VERDE + "[TrabalhadoraD] Thread " + Thread.currentThread().getName() + 
                                 " recebeu vetor ordenado de tamanho: " + String.format("%,d", this.vetorOrdenadoParcial.length) + RESET);
            }
            else
            {
                System.err.println(VERMELHO + "[TrabalhadoraD] Recebeu comunicado inesperado!" + RESET);
            }
            
            minhaParte = null;
            System.gc();
        }
        catch (Exception e)
        {
            System.err.println(VERMELHO + "[TrabalhadoraD] Erro: " + e.getMessage() + RESET);
        }
        finally
        {
            if (this.semaforo.availablePermits() == 0) this.semaforo.release();
        }
        
        long fimThread = System.currentTimeMillis();
        this.tempoThread = fimThread - inicioThread;
        
        System.out.println(CIANO + "[TrabalhadoraD] Thread " + Thread.currentThread().getName() + 
                         " finalizada em " + this.tempoThread + "ms" + RESET);
    }
}