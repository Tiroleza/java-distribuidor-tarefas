import java.util.Arrays;

public class Pedido extends Comunicado {
    private byte[] numeros;

    public Pedido(byte[] numeros) {
        this.numeros = numeros;
    }

    public byte[] getNumeros() {
        return this.numeros;
    }

    // Método principal que o Receptor vai chamar
    public void ordenar() {
        mergeSort(this.numeros, 0, this.numeros.length - 1);
    }

    // Implementação clássica do Merge Sort (Subsídio 2)
    private void mergeSort(byte[] vetor, int inicio, int fim) {
        if (inicio < fim) {
            int meio = (inicio + fim) / 2;
            mergeSort(vetor, inicio, meio);
            mergeSort(vetor, meio + 1, fim);
            merge(vetor, inicio, meio, fim);
        }
    }

    // A operação de intercalação (Subsídio 1)
    private void merge(byte[] vetor, int inicio, int meio, int fim) {
        byte[] left = Arrays.copyOfRange(vetor, inicio, meio + 1);
        byte[] right = Arrays.copyOfRange(vetor, meio + 1, fim + 1);

        int i = 0, j = 0, k = inicio;

        while (i < left.length && j < right.length) {
            if (left[i] <= right[j]) {
                vetor[k++] = left[i++];
            } else {
                vetor[k++] = right[j++];
            }
        }

        while (i < left.length) vetor[k++] = left[i++];
        while (j < right.length) vetor[k++] = right[j++];
    }
}