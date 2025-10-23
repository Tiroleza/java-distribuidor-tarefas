import java.io.*;
import java.net.*;
import java.util.*;

public class DistribuidorLocalOtimizado {
	public static final int[] PORTAS = {12345, 12346, 12347, 12348};
	public static final int CHUNK_SIZE = 1_000_000; // Processar em chunks de 1M
	
	public static void main(String[] args) {
		Scanner sc = new Scanner(System.in);
		System.out.println("=== DISTRIBUIDOR LOCAL OTIMIZADO ===");
		System.out.println("Processadores disponíveis: " + Runtime.getRuntime().availableProcessors());
		
		// Usar tamanho controlado
		int tamanhoEstimado = estimarTamanhoMaximo();
		int tamanho = Math.min(5_000_000, Math.max(100000, tamanhoEstimado / 8)); // máximo 5M
		
		System.out.println("Tamanho estimado máximo: " + String.format("%,d", tamanhoEstimado));
		System.out.println("Tamanho do vetor (otimizado): " + String.format("%,d", tamanho));
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
		
		// Processar em chunks para evitar OutOfMemoryError
		int totalContagem = 0;
		int chunksProcessados = 0;
		
		for (int inicio = 0; inicio < vetor.length; inicio += CHUNK_SIZE) {
			int fim = Math.min(vetor.length, inicio + CHUNK_SIZE);
			byte[] chunk = Arrays.copyOfRange(vetor, inicio, fim);
			
			System.out.println("[D-LOCAL] Processando chunk " + (++chunksProcessados) + 
							 " (elementos " + String.format("%,d", inicio) + " a " + String.format("%,d", fim-1) + ")");
			
			int contagemChunk = processarChunk(chunk, procurado);
			totalContagem += contagemChunk;
			
			System.out.println("[D-LOCAL] Chunk " + chunksProcessados + " contou: " + contagemChunk);
		}
		
		System.out.println("[D-LOCAL] Contagem total: " + String.format("%,d", totalContagem));
		
		// Comparar com contagem sequencial
		System.out.println("\n=== COMPARAÇÃO COM CONTAGEM SEQUENCIAL ===");
		long inicioSeq = System.currentTimeMillis();
		int contagemSeq = contarSequencial(vetor, procurado);
		long fimSeq = System.currentTimeMillis();
		long tempoSeq = fimSeq - inicioSeq;
		
		System.out.println("Contagem sequencial: " + String.format("%,d", contagemSeq) + " ocorrências em " + tempoSeq + " ms");
		System.out.println("Verificação: " + (totalContagem == contagemSeq ? "✓ CORRETO" : "✗ ERRO"));
		
		sc.close();
	}
	
	private static int processarChunk(byte[] chunk, int procurado) {
		int partes = PORTAS.length;
		int tamanhoParte = (int)Math.ceil(chunk.length / (double)partes);
		
		List<Thread> threads = new ArrayList<>();
		List<ContadorChunk> contadores = new ArrayList<>();
		
		for (int i = 0; i < partes; i++) {
			int inicio = i * tamanhoParte;
			int fim = Math.min(chunk.length, inicio + tamanhoParte);
			if (inicio >= fim) break;
			
			byte[] fatia = Arrays.copyOfRange(chunk, inicio, fim);
			ContadorChunk c = new ContadorChunk("localhost", PORTAS[i], fatia, procurado);
			contadores.add(c);
			Thread t = new Thread(c);
			t.start();
			threads.add(t);
		}
		
		// Aguardar todas as threads
		for (Thread t : threads) {
			try { t.join(); } catch (InterruptedException e) {}
		}
		
		int total = 0;
		for (ContadorChunk c : contadores) {
			total += c.getContagem();
		}
		
		return total;
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
}

class ContadorChunk implements Runnable {
	private final String ip;
	private final int porta;
	private final byte[] fatia;
	private final int procurado;
	private int contagem = 0;
	
	ContadorChunk(String ip, int porta, byte[] fatia, int procurado) {
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
			
			out.writeObject(new Pedido(this.fatia, this.procurado));
			out.flush();
			
			Object obj = in.readObject();
			if (obj instanceof Resposta resposta) {
				this.contagem = resposta.getContagem();
			}
		} catch (Exception e) {
			System.err.println("[D-LOCAL] Falha ao contar na porta " + porta + ": " + e.getMessage());
		}
	}
}
