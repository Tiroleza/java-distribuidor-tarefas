public class Pedido extends Comunicado {
	private static final long serialVersionUID = 1L;
	private final int[] numeros;
	private final int procurado;
	
	public Pedido(int[] numeros, int procurado) {
		this.numeros = numeros;
		this.procurado = procurado;
	}
	
	public int[] getNumeros() { return this.numeros; }
	public int getProcurado() { return this.procurado; }
	
	public int contar() {
		int cont = 0;
		for (int n : this.numeros)
			if (n == this.procurado) cont++;
		return cont;
	}
}
