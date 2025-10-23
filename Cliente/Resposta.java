public class Resposta extends Comunicado {
	private static final long serialVersionUID = 1L;
	private final Integer contagem;
	
	public Resposta(int contagem) {
		this.contagem = contagem;
	}
	
	public Integer getContagem() { return this.contagem; }
}
