/**
 * Thread mergeadora que faz merge de dois vetores ordenados.
 * Usada tanto no Receptor (R) quanto no Distribuidor (D).
 */
public class Mergeadora extends Thread {
    private byte[] vetorA;
    private byte[] vetorB;
    private byte[] resultado;
    private int id;
    
    // Cores para logs
    private static final String RESET = "\033[0m";
    private static final String CIANO = "\033[36m";
    
    public Mergeadora(byte[] vetorA, byte[] vetorB, int id) {
        this.vetorA = vetorA;
        this.vetorB = vetorB;
        this.id = id;
        this.resultado = null;
    }
    
    public byte[] getResultado() {
        return this.resultado;
    }
    
    @Override
    public void run() {
        // Faz o merge dos dois vetores ordenados
        this.resultado = intercalar(this.vetorA, this.vetorB);
    }
    
    /**
     * Método utilitário para intercalar (merge) dois vetores ordenados.
     */
    private byte[] intercalar(byte[] A, byte[] B) {
        byte[] C = new byte[A.length + B.length];
        int i = 0, j = 0, k = 0;
        
        while (i < A.length && j < B.length) {
            if (A[i] <= B[j]) {
                C[k++] = A[i++];
            } else {
                C[k++] = B[j++];
            }
        }
        
        while (i < A.length) C[k++] = A[i++];
        while (j < B.length) C[k++] = B[j++];
        
        return C;
    }
}

