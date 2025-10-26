/**
 * Thread 'worker' interna do servidor R.
 * Processa a contagem de um sub-vetor de forma isolada e retorna o resultado.
 */
public class Contadora extends Thread
{
    private final byte[] numeros;
    private final int inicio;
    private final int fim;
    private final int procurado;
    private int contagemParcial;
    private long tempoThread;
    private final int indice;
    
    // Cores para logs
    private static final String RESET = "\033[0m";
    private static final String MAGENTA = "\033[35m";
    
    public Contadora(byte[] numeros, int inicio, int fim, int procurado, int indice)
    {
        this.numeros = numeros;
        this.inicio = inicio;
        this.fim = fim;
        this.procurado = procurado;
        this.indice = indice;
        this.contagemParcial = 0;
    }
    
    /**
     * @return A contagem parcial de ocorrências encontradas por esta thread
     */
    public int getContagemParcial()
    {
        return this.contagemParcial;
    }
    
    /**
     * @return O tempo de execução desta thread em milissegundos
     */
    public long getTempoThread()
    {
        return this.tempoThread;
    }
    
    @Override
    public void run()
    {
        long inicioThread = System.currentTimeMillis();
        
        for (int j = this.inicio; j < this.fim; j++)
        {
            if (this.numeros[j] == this.procurado)
            {
                this.contagemParcial++;
            }
        }
        
        long fimThread = System.currentTimeMillis();
        this.tempoThread = fimThread - inicioThread;
        
        System.out.println(MAGENTA + "[R] Thread " + this.indice + " processou " + 
                         String.format("%,d", this.fim - this.inicio) + " elementos, encontrou " + 
                         this.contagemParcial + " ocorrências (tempo: " + this.tempoThread + "ms)" + RESET);
    }
}

