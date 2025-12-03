public class Resposta extends Comunicado {
    private byte[] vetorOrdenado;

    public Resposta(byte[] vetorOrdenado) {
        this.vetorOrdenado = vetorOrdenado;
    }

    public byte[] getVetor() {
        return this.vetorOrdenado;
    }
    
    // Método alternativo para compatibilidade (se necessário)
    public byte[] getVetorOrdenado() {
        return this.vetorOrdenado;
    }
}