import java.util.*;

/**
 * Classe principal do Receptor (Servidor).
 * Inicia a thread 'AceitadoraDeConexaoR' e fornece uma interface de comandos para encerrar o servidor.
 */
public class R
{
    public static String PORTA_PADRAO = "12345";
    
    // Cores para logs
    private static final String RESET = "\033[0m";
    private static final String VERDE = "\033[32m";
    private static final String AZUL = "\033[34m";
    private static final String AMARELO = "\033[33m";
    private static final String VERMELHO = "\033[31m";
    private static final String CIANO = "\033[36m";
    
    public static void main (String[] args)
    {
        if (args.length>1)
        {
            System.err.println ("Uso esperado: java R [PORTA]\n");
            return;
        }

        String porta=R.PORTA_PADRAO;
        
        if (args.length==1)
            porta = args[0];

        ArrayList<Parceiro> usuarios =
        new ArrayList<Parceiro> ();

        AceitadoraDeConexaoR aceitadoraDeConexao=null;
        try
        {
            aceitadoraDeConexao =
            new AceitadoraDeConexaoR (porta, usuarios);
            aceitadoraDeConexao.start();
        }
        catch (Exception erro)
        {
            System.err.println ("Escolha uma porta apropriada e liberada para uso!\n");
            return;
        }

        System.out.println(CIANO + "[R] Servidor iniciado na porta " + porta + "!" + RESET);
        System.out.println(VERDE + "[R] AceitadoraDeConexaoR iniciada!" + RESET);
        System.out.println(AZUL + "[R] Aguardando conexões..." + RESET);

        for(;;)
        {
            System.out.println ("O servidor esta ativo! Para desativa-lo,");
            System.out.println ("use o comando \"desativar\"\n");
            System.out.print   ("> ");

            String comando=null;
            try
            {
                comando = Teclado.getUmString();
            }
            catch (Exception erro)
            {}

            if (comando.toLowerCase().equals("desativar"))
            {
                synchronized (usuarios)
                {
                    ComunicadoEncerramento comunicadoEncerramento =
                    new ComunicadoEncerramento ();
                    
                    for (Parceiro usuario:usuarios)
                    {
                        try
                        {
                            usuario.receba (comunicadoEncerramento);
                            usuario.adeus  ();
                        }
                        catch (Exception erro)
                        {}
                    }
                }

                System.out.println ("O servidor foi desativado!\n");
                System.exit(0);
            }
            else
                System.err.println ("Comando invalido!\n");
        }
    }
}
