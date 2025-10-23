import java.io.*;
import java.net.*;

public class Receptor {
	public static final int PORTA_PADRAO = 12345;
	
	public static void main(String[] args) {
		int porta = PORTA_PADRAO;
		if (args.length > 0) {
			try { porta = Integer.parseInt(args[0]); } catch (Exception e) { porta = PORTA_PADRAO; }
		}
		
		System.out.println("[R] Iniciando receptor na porta " + porta);
		try (ServerSocket servidor = new ServerSocket(porta)) {
			for(;;) {
				System.out.println("[R] Aguardando conexão...");
				Socket conexao = servidor.accept();
				System.out.println("[R] Conexão de " + conexao.getInetAddress().getHostAddress());
				
				try (ObjectOutputStream out = new ObjectOutputStream(conexao.getOutputStream());
					 ObjectInputStream  in  = new ObjectInputStream(conexao.getInputStream())) {
					for(;;) {
						Object obj = in.readObject();
						if (obj instanceof Pedido) {
							Pedido p = (Pedido)obj;
							int c = p.contar();
							System.out.println("[R] Pedido recebido. Contagem="+c);
							out.writeObject(new Resposta(c));
							out.flush();
						}
						else if (obj instanceof ComunicadoEncerramento) {
							System.out.println("[R] Comunicado de encerramento recebido. Fechando conexão.");
							break;
						}
					}
				} catch (EOFException e) {
					System.out.println("[R] Cliente encerrou a conexão.");
				} catch (Exception e) {
					System.err.println("[R] Erro na conexão: "+e.getMessage());
				}
			}
		} catch (IOException e) {
			System.err.println("[R] Erro ao abrir porta: "+e.getMessage());
		}
	}
}
