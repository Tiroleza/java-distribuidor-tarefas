import java.io.*;
import java.net.*;

public class ReceptorLocal {
	public static final int[] PORTAS = {12345, 12346, 12347, 12348};
	
	public static void main(String[] args) {
		System.out.println("[R-LOCAL] Iniciando receptores locais nas portas: " + java.util.Arrays.toString(PORTAS));
		
		// Criar threads para cada porta
		for (int porta : PORTAS) {
			Thread thread = new Thread(() -> executarReceptor(porta));
			thread.setDaemon(true);
			thread.start();
		}
		
		System.out.println("[R-LOCAL] Todos os receptores iniciados. Pressione Ctrl+C para parar.");
		
		// Manter o programa rodando
		try {
			Thread.currentThread().join();
		} catch (InterruptedException e) {
			System.out.println("[R-LOCAL] Encerrando receptores...");
		}
	}
	
	private static void executarReceptor(int porta) {
		try (ServerSocket servidor = new ServerSocket(porta)) {
			System.out.println("[R-LOCAL] Receptor ativo na porta " + porta);
			
			for(;;) {
				System.out.println("[R-LOCAL] Porta " + porta + " aguardando conexão...");
				Socket conexao = servidor.accept();
				System.out.println("[R-LOCAL] Porta " + porta + " - Conexão de " + conexao.getInetAddress().getHostAddress());
				
				try (ObjectOutputStream out = new ObjectOutputStream(conexao.getOutputStream());
					 ObjectInputStream  in  = new ObjectInputStream(conexao.getInputStream())) {
					
					for(;;) {
						Object obj = in.readObject();
						if (obj instanceof Pedido) {
							Pedido p = (Pedido)obj;
							int c = p.contar();
							System.out.println("[R-LOCAL] Porta " + porta + " - Pedido recebido. Contagem=" + c);
							out.writeObject(new Resposta(c));
							out.flush();
						}
						else if (obj instanceof ComunicadoEncerramento) {
							System.out.println("[R-LOCAL] Porta " + porta + " - Comunicado de encerramento recebido.");
							break;
						}
					}
				} catch (EOFException e) {
					System.out.println("[R-LOCAL] Porta " + porta + " - Cliente encerrou a conexão.");
				} catch (Exception e) {
					System.err.println("[R-LOCAL] Porta " + porta + " - Erro na conexão: " + e.getMessage());
				}
			}
		} catch (IOException e) {
			System.err.println("[R-LOCAL] Erro ao abrir porta " + porta + ": " + e.getMessage());
		}
	}
}
