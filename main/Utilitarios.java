public class Utilitarios {
    // Intercala dois vetores ordenados A e B retornando um C ordenado
    public static byte[] intercalar(byte[] A, byte[] B) {
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