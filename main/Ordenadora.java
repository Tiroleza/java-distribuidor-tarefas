
/**
 * Thread trabalhadora do Servidor.
 * Recebe uma fatia do vetor e a ordena usando a lógica do Merge Sort (via Pedido).
 */
public class Ordenadora extends Thread {
    private byte[] vetor;
    private int id; // Identificador da thread para logs

    // CORREÇÃO AQUI: O construtor agora aceita (byte[] vetor, int id)
    public Ordenadora(byte[] vetor, int id) {
        this.vetor = vetor;
        this.id = id;
    }

    public byte[] getVetorOrdenado() {
        return this.vetor;
    }

    @Override
    public void run() {
        // Usa a lógica do Pedido para ordenar o vetor localmente
        // (Certifique-se que Pedido.java tem o método ordenar())
        new Pedido(this.vetor).ordenar();
        
        // Opcional: Log para depuração
        // System.out.println("Thread Ordenadora " + this.id + " terminou.");
    }
}