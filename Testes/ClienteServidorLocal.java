import java.io.*;
import java.net.*;
import java.util.*;

public class ClienteServidorLocal {
	public static void main(String[] args) throws Exception {
		System.out.println("=== TESTE AUTOMÁTICO LOCAL ===");
		
		// Estima vetor máximo e usa metade
		int estimado = estimarTamanhoMaximoAproximado();
		int tamanho = Math.max(100000, estimado/2);
		System.out.println("[TESTE] Tamanho escolhido (metade): "+String.format("%,d", tamanho));
		
		// Sobe servidor em thread
		Thread servidor = new Thread(() -> {
			try { ReceptorLocal.main(new String[]{}); } catch (Exception e) { e.printStackTrace(); }
		});
		servidor.setDaemon(true);
		servidor.start();
		
		Thread.sleep(2000); // aguardar servidores iniciarem
		
		// Roda cliente distribuindo fatias para 4 instâncias locais
		byte[] vetor = gerarVetor(tamanho);
		int pos = (int)(Math.random()*vetor.length);
		int procurado = vetor[pos];
		System.out.println("[TESTE] Número escolhido do vetor: "+procurado+" (posição "+pos+")");
		
		int[] portas = {12345, 12346, 12347, 12348};
		int tamParte = (int)Math.ceil(vetor.length/(double)portas.length);
		List<Thread> ts = new ArrayList<>();
		List<Runner> rs = new ArrayList<>();
		for (int i=0;i<portas.length;i++) {
			int ini=i*tamParte, fim=Math.min(vetor.length, ini+tamParte);
			if (ini>=fim) break;
			byte[] fatia = Arrays.copyOfRange(vetor, ini, fim);
			Runner r = new Runner("localhost", portas[i], fatia, procurado);
			rs.add(r);
			Thread t = new Thread(r);
			t.start();
			ts.add(t);
		}
		for (Thread t:ts) t.join();
		int total=0; for (Runner r:rs) total+=r.total;
		System.out.println("[TESTE] Total contado: "+total);
		
		// Comparar com sequencial
		long inicioSeq = System.currentTimeMillis();
		int seq = contarSequencial(vetor, procurado);
		long fimSeq = System.currentTimeMillis();
		System.out.println("[TESTE] Sequencial: "+seq+" em "+(fimSeq-inicioSeq)+" ms");
		System.out.println("[TESTE] Verificação: "+(total==seq?"✓ CORRETO":"✗ ERRO"));
	}
	
	private static int estimarTamanhoMaximoAproximado() {
		int tamanho = 1_000_000, ultimo=1_000_000;
		try {
			while (true) {
				byte[] v = new byte[tamanho];
				ultimo = tamanho;
				v=null; System.gc();
				if (tamanho > Integer.MAX_VALUE / 3 * 2) break;
				tamanho = (tamanho/2)*3;
			}
		} catch (OutOfMemoryError e) {}
		return ultimo;
	}
	
	private static byte[] gerarVetor(int tamanho) {
		byte[] v = new byte[tamanho];
		Random r = new Random();
		for (int i=0;i<tamanho;i++) v[i]=(byte)(r.nextInt(201)-100);
		return v;
	}
	
	private static int contarSequencial(byte[] vetor, int procurado) {
		int contagem = 0;
		for (byte numero : vetor) {
			if (numero == procurado) contagem++;
		}
		return contagem;
	}
	
	static class Runner implements Runnable {
		final String ip; final int porta; final byte[] fatia; final int procurado; int total=0;
		Runner(String ip,int porta,byte[] fatia,int procurado){this.ip=ip;this.porta=porta;this.fatia=fatia;this.procurado=procurado;}
		public void run() {
			try (Socket s=new Socket(ip,porta);
				 ObjectOutputStream out=new ObjectOutputStream(s.getOutputStream());
				 ObjectInputStream in=new ObjectInputStream(s.getInputStream())){
				int[] nums=new int[fatia.length]; for(int i=0;i<fatia.length;i++) nums[i]=fatia[i];
				out.writeObject(new Pedido(nums, procurado)); out.flush();
				Object o=in.readObject();
				if (o instanceof Resposta) total=((Resposta)o).getContagem();
			} catch(Exception e){ System.err.println("[TESTE] Falha: "+e.getMessage());}
		}
	}
}
