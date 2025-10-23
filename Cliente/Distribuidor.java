import java.io.*;
import java.net.*;
import java.util.*;

public class Distribuidor {
	public static final int PORTA_PADRAO = 12345;
	private static final String[] IPS = {
		"localhost", // ajuste para IPs reais nas máquinas R
		"localhost",
		"localhost",
		"localhost"
	};
	
	public static void main(String[] args) {
		Scanner sc = new Scanner(System.in);
		System.out.println("=== DISTRIBUIDOR ===");
		System.out.print("Tamanho do vetor (ex.: 1000000): ");
		int tamanho;
		try { tamanho = sc.nextInt(); } catch(Exception e) { tamanho = 1000000; }
		
		System.out.print("Deseja exibir o vetor? (s/n): ");
		boolean exibir = false;
		try { exibir = sc.next().trim().toLowerCase().startsWith("s"); } catch(Exception e) {}
		
		byte[] vetor = gerarVetor(tamanho);
		if (exibir) exibirVetor(vetor);
		
		// Escolher número aleatório do próprio vetor (posição aleatória)
		int pos = (int)(Math.random() * vetor.length);
		int procurado = vetor[pos];
		System.out.println("[D] Número a contar (tomado do vetor na posição "+pos+"): "+procurado);
		
		// Dividir em partes semelhantes
		int partes = IPS.length;
		int tamanhoParte = (int)Math.ceil(vetor.length / (double)partes);
		
		List<Thread> threads = new ArrayList<>();
		List<ContadorRemoto> contadores = new ArrayList<>();
		
		for (int i=0;i<partes;i++) {
			int inicio = i*tamanhoParte;
			int fim = Math.min(vetor.length, inicio+tamanhoParte);
			if (inicio>=fim) break;
			byte[] fatia = Arrays.copyOfRange(vetor, inicio, fim);
			ContadorRemoto c = new ContadorRemoto(IPS[i], PORTA_PADRAO, fatia, procurado);
			contadores.add(c);
			Thread t = new Thread(c);
			t.start();
			threads.add(t);
		}
		
		for (Thread t:threads) try { t.join(); } catch (InterruptedException e) {}
		
		int total = 0;
		for (ContadorRemoto c:contadores) total += c.getContagem();
		
		System.out.println("[D] Contagem total: "+total);
		
		// Encerrar conexões nos R
		encerrarReceptores();
	}
	
	private static byte[] gerarVetor(int tamanho) {
		byte[] v = new byte[tamanho];
		Random r = new Random();
		for (int i=0;i<tamanho;i++) v[i] = (byte)(r.nextInt(201)-100);
		return v;
	}
	
	private static void exibirVetor(byte[] v) {
		for (int i=0;i<Math.min(v.length,50);i++) {
			System.out.print(v[i]+" ");
			if ((i+1)%10==0) System.out.println();
		}
		if (v.length>50) System.out.println("... (primeiros 50 elementos)");
	}
	
	private static void encerrarReceptores() {
		for (String ip:IPS) {
			try(Socket s = new Socket(ip, PORTA_PADRAO);
				ObjectOutputStream out = new ObjectOutputStream(s.getOutputStream())){
				out.writeObject(new ComunicadoEncerramento());
				out.flush();
			} catch(Exception e) {
				System.err.println("[D] Falha ao encerrar receptor "+ip+": "+e.getMessage());
			}
		}
	}
}

class ContadorRemoto implements Runnable {
	private final String ip;
	private final int porta;
	private final byte[] fatia;
	private final int procurado;
	private int contagem = 0;
	
	ContadorRemoto(String ip, int porta, byte[] fatia, int procurado) {
		this.ip = ip;
		this.porta = porta;
		this.fatia = fatia;
		this.procurado = procurado;
	}
	
	public int getContagem() { return this.contagem; }
	
	@Override public void run() {
		try (Socket s = new Socket(this.ip,this.porta);
			 ObjectOutputStream out = new ObjectOutputStream(s.getOutputStream());
			 ObjectInputStream  in  = new ObjectInputStream(s.getInputStream())) {
			// converter byte[] -> int[] para Pedido
			int[] nums = new int[this.fatia.length];
			for (int i=0;i<this.fatia.length;i++) nums[i] = this.fatia[i];
			out.writeObject(new Pedido(nums, this.procurado));
			out.flush();
			Object obj = in.readObject();
			if (obj instanceof Resposta) this.contagem = ((Resposta)obj).getContagem();
		} catch (Exception e) {
			System.err.println("[D] Falha ao contar em "+ip+": "+e.getMessage());
		}
	}
}
