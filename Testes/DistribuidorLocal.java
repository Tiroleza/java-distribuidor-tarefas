import java.io.*;
import java.net.*;
import java.util.*;

public class DistribuidorLocal {
	public static final int[] PORTAS = {12345, 12346, 12347, 12348};
	
	public static void main(String[] args) {
		try (Scanner sc = new Scanner(System.in)) {
			System.out.println("=== DISTRIBUIDOR LOCAL (TESTES) ===");
			System.out.println("Processadores disponíveis: " + Runtime.getRuntime().availableProcessors());
		
		// Usar metade do tamanho estimado do MaiorVetorAproximado
		int tamanhoEstimado = estimarTamanhoMaximo();
		int tamanho = Math.max(100000, tamanhoEstimado / 2); // mínimo 100k, máximo metade do estimado
		
		System.out.println("Tamanho estimado máximo: " + String.format("%,d", tamanhoEstimado));
		System.out.println("Tamanho do vetor (metade): " + String.format("%,d", tamanho));
		System.out.printf("Memória estimada: %.2f MB%n", tamanho * 1.0 / (1024 * 1024));
		
		System.out.print("Deseja exibir o vetor? (s/n): ");
		boolean exibir = false;
		try { exibir = sc.next().trim().toLowerCase().startsWith("s"); } catch(Exception e) {}
		
		byte[] vetor = gerarVetor(tamanho);
		if (exibir) exibirVetor(vetor);
		
		// Escolher número aleatório do próprio vetor
		int pos = (int)(Math.random() * vetor.length);
		int procurado = vetor[pos];
		System.out.println("[D-LOCAL] Número a contar (posição " + pos + "): " + procurado);
		
		// Dividir em partes para as 4 portas
		int partes = PORTAS.length;
		int tamanhoParte = (int)Math.ceil(vetor.length / (double)partes);
		
		List<Thread> threads = new ArrayList<>();
		List<ContadorLocal> contadores = new ArrayList<>();
		
		for (int i = 0; i < partes; i++) {
			int inicio = i * tamanhoParte;
			int fim = Math.min(vetor.length, inicio + tamanhoParte);
			if (inicio >= fim) break;
			
			byte[] fatia = Arrays.copyOfRange(vetor, inicio, fim);
			ContadorLocal c = new ContadorLocal("localhost", PORTAS[i], fatia, procurado);
			contadores.add(c);
			Thread t = new Thread(c);
			t.start();
			threads.add(t);
			
			System.out.println("[D-LOCAL] Thread " + (i+1) + " para porta " + PORTAS[i] + 
							 " (elementos " + String.format("%,d", inicio) + " a " + String.format("%,d", fim-1) + ")");
		}
		
		// Aguardar todas as threads
		for (Thread t : threads) {
			try { t.join(); } catch (InterruptedException e) {}
		}
		
		int total = 0;
		for (ContadorLocal c : contadores) {
			total += c.getContagem();
		}
		
		System.out.println("[D-LOCAL] Contagem total: " + String.format("%,d", total));
		
		// Encerrar conexões
		encerrarReceptores();
		
		// Comparar com contagem sequencial
		System.out.println("\n=== COMPARAÇÃO COM CONTAGEM SEQUENCIAL ===");
		long inicioSeq = System.currentTimeMillis();
		int contagemSeq = contarSequencial(vetor, procurado);
		long fimSeq = System.currentTimeMillis();
		long tempoSeq = fimSeq - inicioSeq;
		
		System.out.println("Contagem sequencial: " + String.format("%,d", contagemSeq) + " ocorrências em " + tempoSeq + " ms");
		System.out.println("Verificação: " + (total == contagemSeq ? "✓ CORRETO" : "✗ ERRO"));
    		}
	}
	
	private static int estimarTamanhoMaximo() {
		int tamanho = 1_000_000;
		int ultimo = 1_000_000;
		try {
			while (true) {
				byte[] v = new byte[tamanho];
				ultimo = tamanho;
				v = null;
				System.gc();
				if (tamanho > Integer.MAX_VALUE / 3 * 2) break;
				tamanho = (tamanho / 2) * 3;
			}
		} catch (OutOfMemoryError e) {
			// usar último tamanho válido
		}
		return ultimo;
	}
	
	private static byte[] gerarVetor(int tamanho) {
		System.out.println("[D-LOCAL] Gerando vetor de " + String.format("%,d", tamanho) + " elementos...");
		long inicio = System.currentTimeMillis();
		
		byte[] v = new byte[tamanho];
		Random r = new Random();
		for (int i = 0; i < tamanho; i++) {
			v[i] = (byte)(r.nextInt(201) - 100);
		}
		
		long fim = System.currentTimeMillis();
		System.out.println("[D-LOCAL] Vetor gerado em " + (fim - inicio) + " ms");
		return v;
	}
	
	private static void exibirVetor(byte[] v) {
		System.out.println("\n=== VETOR GERADO ===");
		for (int i = 0; i < Math.min(v.length, 50); i++) {
			System.out.print(v[i] + " ");
			if ((i + 1) % 10 == 0) System.out.println();
		}
		if (v.length > 50) System.out.println("... (primeiros 50 elementos)");
		System.out.println();
	}
	
	private static int contarSequencial(byte[] vetor, int procurado) {
		int contagem = 0;
		for (byte numero : vetor) {
			if (numero == procurado) contagem++;
		}
		return contagem;
	}
	
	private static void encerrarReceptores() {
		System.out.println("[D-LOCAL] Encerrando receptores...");
		for (int porta : PORTAS) {
			try (Socket s = new Socket("localhost", porta);
				 ObjectOutputStream out = new ObjectOutputStream(s.getOutputStream())) {
				out.writeObject(new ComunicadoEncerramento());
				out.flush();
			} catch (Exception e) {
				System.err.println("[D-LOCAL] Falha ao encerrar receptor porta " + porta + ": " + e.getMessage());
			}
		}
	}
}

class ContadorLocal implements Runnable {
	private final String ip;
	private final int porta;
	private final byte[] fatia;
	private final int procurado;
	private int contagem = 0;
	
	ContadorLocal(String ip, int porta, byte[] fatia, int procurado) {
		this.ip = ip;
		this.porta = porta;
		this.fatia = fatia;
		this.procurado = procurado;
	}
	
	public int getContagem() { return this.contagem; }
	
	@Override
	public void run() {
		try (Socket s = new Socket(this.ip, this.porta);
			 ObjectOutputStream out = new ObjectOutputStream(s.getOutputStream());
			 ObjectInputStream  in  = new ObjectInputStream(s.getInputStream())) {
			
			// Converter byte[] para int[]
			int[] nums = new int[this.fatia.length];
			for (int i = 0; i < this.fatia.length; i++) {
				nums[i] = this.fatia[i];
			}
			
			out.writeObject(new Pedido(nums, this.procurado));
			out.flush();
			
			Object obj = in.readObject();
			if (obj instanceof Resposta resposta) {
				this.contagem = resposta.getContagem();
				System.out.println("[D-LOCAL] Resposta da porta " + porta + ": " + contagem + " ocorrências");
			}
		} catch (Exception e) {
			System.err.println("[D-LOCAL] Falha ao contar na porta " + porta + ": " + e.getMessage());
		}
	}
}
