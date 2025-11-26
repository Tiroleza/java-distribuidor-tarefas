public class Resposta extends Comunicado {
    private byte[] vetorOrdenado;

    public Resposta(byte[] vetorOrdenado) {
        this.vetorOrdenado = vetorOrdenado;
    }

    public byte[] getVetorOrdenado() {
        return this.vetorOrdenado;
    }
}