public class Pedido extends Comunicado {
	private static final long serialVersionUID = 1L;
	private final byte[] numeros;  // Mudança: usar byte[] em vez de int[]
	private final int procurado;
	
	public Pedido(byte[] numeros, int procurado) {
		this.numeros = numeros;
		this.procurado = procurado;
	}
	
	public byte[] getNumeros() { return this.numeros; }
	public int getProcurado() { return this.procurado; }
	
	public int contar() {
		int cont = 0;
		for (byte n : this.numeros)
			if (n == this.procurado) cont++;
		return cont;
	}
}
