import java.io.*;
import java.net.*;
import java.util.*;
import java.util.Arrays;

public class D
{
    // IPs dos servidores (hard-coded conforme enunciado)
    private static final String[] IPS_SERVIDORES = {"localhost", "localhost", "192.168.15.3"};
    private static final int[] PORTAS_SERVIDORES = {12345, 12346, 12347};
    
    // Cores para logs
    private static final String RESET = "\033[0m";
    private static final String VERDE = "\033[32m";
    private static final String AZUL = "\033[34m";
    private static final String AMARELO = "\033[33m";
    private static final String VERMELHO = "\033[31m";
    private static final String CIANO = "\033[36m";
    
    public static void main(String[] args)
    {
        System.out.println(CIANO + "[D] Iniciando distribuidor..." + RESET);
        
        // Conectar aos servidores usando Parceiro
        List<Parceiro> servidores = new ArrayList<>();
        
        System.out.println(AZUL + "[D] Conectando aos servidores..." + RESET);
        for (int i = 0; i < IPS_SERVIDORES.length; i++)
        {
            try
            {
                System.out.println(AMARELO + "[D] Tentando conectar com " + IPS_SERVIDORES[i] + ":" + PORTAS_SERVIDORES[i] + "..." + RESET);
                Socket conexao = new Socket(IPS_SERVIDORES[i], PORTAS_SERVIDORES[i]);
                ObjectOutputStream transmissor = new ObjectOutputStream(conexao.getOutputStream());
                ObjectInputStream receptor = new ObjectInputStream(conexao.getInputStream());
                Parceiro servidor = new Parceiro(conexao, receptor, transmissor);
                
                servidores.add(servidor);
                
                System.out.println(VERDE + "[D] ✓ Conectado ao servidor " + IPS_SERVIDORES[i] + ":" + PORTAS_SERVIDORES[i] + RESET);
            }
            catch (Exception e)
            {
                System.err.println(VERMELHO + "[D] ✗ Erro ao conectar com " + IPS_SERVIDORES[i] + ":" + PORTAS_SERVIDORES[i] + " - " + e.getMessage() + RESET);
            }
        }
        
        if (servidores.isEmpty())
        {
            System.err.println(VERMELHO + "[D] Nenhum servidor disponível!" + RESET);
            return;
        }
        
        System.out.println(VERDE + "[D] ✓ Conectado a " + servidores.size() + " servidores. Iniciando processamento automático..." + RESET);
        
        // Iniciar processamento automaticamente
        processarNovoVetor(servidores);
        
        // Encerrar após processamento
        encerrarConexoes(servidores);
        System.out.println(CIANO + "[D] Programa encerrado!" + RESET);
    }
    
    private static int calcularTamanhoMaximoVetor()
    {
        System.out.println(AZUL + "[D] Estimando o maior tamanho possível de vetor em Java..." + RESET);
        long inicio = System.currentTimeMillis();
        int tamanho = 1_000_000; // começa com 1 milhão
        int ultimoBemSucedido = 0;
        
        while (true)
        {
            try
            {
                byte[] vetor = new byte[tamanho];
                ultimoBemSucedido = tamanho;
                vetor = null; // libera
                System.gc();
                
                // aumenta o tamanho em 50% para a próxima tentativa
                if (tamanho > Integer.MAX_VALUE / 3 * 2) break;
                tamanho /= 2;
                tamanho *= 3;
                System.out.println(VERDE + "[D] Alocado com sucesso: " + String.format("%,d", ultimoBemSucedido) + " elementos" + RESET);
            }
            catch (OutOfMemoryError e)
            {
                System.out.println(VERMELHO + "[D] Falhou em " + String.format("%,d", tamanho) + " elementos" + RESET);
                break;
            }
        }
        
        long fim = System.currentTimeMillis();
        System.out.println(CIANO + "[D] Maior vetor que coube (aproximadamente): " + String.format("%,d", ultimoBemSucedido) + RESET);
        System.out.println(AMARELO + "[D] Memória estimada: " + String.format("%.2f MB", ultimoBemSucedido * 1.0 / (1024 * 1024)) + RESET);
        System.out.println(AMARELO + "[D] Tempo total: " + String.format("%.2f segundos", (fim - inicio) / 1000.0) + RESET);
        
        // Limitar tamanho para 2GB (2 bilhões de bytes)
        int tamanhoLimitado = Math.min(ultimoBemSucedido, 2_000_000_000); // Máximo 2GB (limite do int)
        if (tamanhoLimitado < ultimoBemSucedido)
        {
            System.out.println(AMARELO + "[D] ⚠️  Limitando vetor para " + String.format("%,d", tamanhoLimitado) + " elementos (3GB máximo)" + RESET);
        }
        
        return tamanhoLimitado;
    }
    
    private static void processarNovoVetor(List<Parceiro> servidores)
    {
        try
        {
            // Calcular tamanho máximo do vetor usando o código fornecido
            System.out.println(AZUL + "[D] Calculando tamanho máximo do vetor..." + RESET);
            int tamanho = calcularTamanhoMaximoVetor();
            
            System.out.println(VERDE + "[D] ✓ Tamanho do vetor definido: " + String.format("%,d", tamanho) + " elementos" + RESET);
            
            // Calcular estimativa de memória
            long memoriaEstimada = tamanho * 1L; // 1 byte por elemento
            double memoriaMB = memoriaEstimada / (1024.0 * 1024.0);
            System.out.println(AMARELO + "[D] Memória estimada: " + String.format("%.2f MB", memoriaMB) + RESET);
            
            // Gerar vetor de bytes aleatórios entre -100 e 100
            long inicioGeracao = System.currentTimeMillis();
            System.out.println(AZUL + "[D] Gerando vetor de " + String.format("%,d", tamanho) + " elementos..." + RESET);
            byte[] vetor = new byte[tamanho];
            Random random = new Random();
            for (int i = 0; i < tamanho; i++)
            {
                vetor[i] = (byte)(random.nextInt(201) - 100); // -100 a 100
            }
            long fimGeracao = System.currentTimeMillis();
            System.out.println(VERDE + "[D] ✓ Vetor gerado em " + (fimGeracao - inicioGeracao) + "ms!" + RESET);
            
            // Escolher aleatoriamente um número do vetor para contar
            int posicaoAleatoria = random.nextInt(tamanho);
            int numeroProcurado = vetor[posicaoAleatoria];
            
            System.out.println(CIANO + "[D] Procurando pelo número: " + numeroProcurado + " (posição " + String.format("%,d", posicaoAleatoria) + ")" + RESET);
            
            // Dividir vetor entre servidores usando TrabalhadoraD
            int tamanhoParte = vetor.length / servidores.size();
            
            System.out.println(AZUL + "[D] Processando vetor em " + servidores.size() + " partes paralelas..." + RESET);
            
            long inicioProcessamento = System.currentTimeMillis();
            
            // Fase 1: Iniciar Todas as Tarefas
            System.out.println(CIANO + "[D] FASE 1: Iniciando todas as threads..." + RESET);
            TrabalhadoraD[] threads = new TrabalhadoraD[servidores.size()];
            
            for (int i = 0; i < servidores.size(); i++)
            {
                final int inicio = i * tamanhoParte;
                final int fim = (i == servidores.size() - 1) ? vetor.length : (i + 1) * tamanhoParte;
                
                // Criar TrabalhadoraD sem cópia do vetor (apenas referências)
                threads[i] = new TrabalhadoraD(vetor, inicio, fim, servidores.get(i), numeroProcurado);
                
                // Iniciar thread (Fase 1)
                threads[i].start();
                System.out.println(VERDE + "[D] ✓ Thread " + i + " iniciada (start())" + RESET);
            }
            
            // Fase 2: Sincronizar (Aguardar) Todas as Tarefas
            System.out.println(CIANO + "[D] FASE 2: Aguardando todas as threads..." + RESET);
            for (int i = 0; i < threads.length; i++)
            {
                try
                {
                    threads[i].join();
                    System.out.println(VERDE + "[D] ✓ Thread " + i + " finalizada (join())" + RESET);
                }
                catch (InterruptedException e)
                {
                    System.err.println(VERMELHO + "[D] ✗ Erro ao aguardar Thread " + i + ": " + e.getMessage() + RESET);
                }
            }
            
            System.out.println(VERDE + "[D] ✓ Todas as threads finalizadas!" + RESET);
            
            long fimProcessamento = System.currentTimeMillis();
            long tempoTotal = fimProcessamento - inicioProcessamento;
            
            // Somar resultados das TrabalhadoraD
            int contagemTotal = 0;
            for (int i = 0; i < threads.length; i++)
            {
                contagemTotal += threads[i].getContagemParcial();
            }
            
            // Mostrar métricas de tempo
            System.out.println(VERDE + "[D] ✓ Contagem final: " + contagemTotal + RESET);
            System.out.println(CIANO + "[D] 📊 MÉTRICAS DE TEMPO:" + RESET);
            System.out.println(AMARELO + "[D]   • Tempo total de processamento: " + tempoTotal + "ms" + RESET);
            System.out.println(AMARELO + "[D]   • Tempo de geração do vetor: " + (fimGeracao - inicioGeracao) + "ms" + RESET);
            
            for (int i = 0; i < threads.length; i++)
            {
                System.out.println(AMARELO + "[D]   • Thread " + i + ": " + threads[i].getTempoThread() + "ms" + RESET);
            }
            
            // Desconectar após calcular
            System.out.println(AMARELO + "[D] Tarefa concluída, desconectando dos servidores..." + RESET);
        }
        catch (Exception e)
        {
            System.err.println(VERMELHO + "[D] ✗ Erro ao processar novo vetor: " + e.getMessage() + RESET);
        }
    }
    
    
    
    private static void encerrarConexoes(List<Parceiro> servidores)
    {
        System.out.println(AMARELO + "[D] Encerrando conexões..." + RESET);
        
        // Enviar comunicado de encerramento para todos os servidores
        for (int i = 0; i < servidores.size(); i++)
        {
            try
            {
                servidores.get(i).receba(new ComunicadoEncerramento());
                servidores.get(i).adeus();
                System.out.println(VERDE + "[D] ✓ Servidor " + i + " encerrado." + RESET);
            }
            catch (Exception e)
            {
                System.err.println(VERMELHO + "[D] ✗ Erro ao encerrar conexão com servidor " + i + ": " + e.getMessage() + RESET);
            }
        }
        
        System.out.println(CIANO + "[D] Conexões encerradas!" + RESET);
    }
    
    // Classe interna TrabalhadoraD
    private static class TrabalhadoraD extends Thread
    {
        private byte[] grandeVetor;
        private int inicio;
        private int fim;
        private Parceiro servidor;
        private int procurado;
        private int contagemParcial;
        private long tempoThread;
        
        public TrabalhadoraD(byte[] grandeVetor, int inicio, int fim, Parceiro servidor, int procurado)
        {
            this.grandeVetor = grandeVetor;
            this.inicio = inicio;
            this.fim = fim;
            this.servidor = servidor;
            this.procurado = procurado;
            this.contagemParcial = 0;
        }
        
        public int getContagemParcial()
        {
            return this.contagemParcial;
        }
        
        public long getTempoThread()
        {
            return this.tempoThread;
        }
        
        @Override
        public void run()
        {
            long inicioThread = System.currentTimeMillis();
            
            try
            {
                // Criar segmento do vetor dentro do run() para serializar alocação
                byte[] minhaParte = Arrays.copyOfRange(this.grandeVetor, this.inicio, this.fim);
                
                System.out.println(AMARELO + "[TrabalhadoraD] Thread " + Thread.currentThread().getName() + 
                                 " processando " + String.format("%,d", minhaParte.length) + " elementos" + RESET);
                
                // Usar Parceiro exatamente como Cliente.java para transação simples
                // Enviar pedido
                this.servidor.receba(new Pedido(minhaParte, this.procurado));
                
                // Aguardar resposta (bloqueando)
                Comunicado c = this.servidor.envie();
                
                // Verificar se é Resposta
                if (c instanceof Resposta)
                {
                    Resposta resposta = (Resposta) c;
                    this.contagemParcial = resposta.getContagem();
                    System.out.println(VERDE + "[TrabalhadoraD] Thread " + Thread.currentThread().getName() + 
                                     " recebeu resposta: " + this.contagemParcial + RESET);
                }
                else
                {
                    System.err.println(VERMELHO + "[TrabalhadoraD] Thread " + Thread.currentThread().getName() + 
                                     " recebeu comunicado inesperado: " + c.getClass().getSimpleName() + RESET);
                    this.contagemParcial = 0;
                }
                
                // Liberar memória do segmento
                minhaParte = null;
                System.gc();
            }
            catch (Exception e)
            {
                System.err.println(VERMELHO + "[TrabalhadoraD] Erro na thread " + Thread.currentThread().getName() + 
                                 ": " + e.getMessage() + RESET);
                this.contagemParcial = 0;
            }
            
            long fimThread = System.currentTimeMillis();
            this.tempoThread = fimThread - inicioThread;
            
            System.out.println(CIANO + "[TrabalhadoraD] Thread " + Thread.currentThread().getName() + 
                             " finalizada em " + this.tempoThread + "ms" + RESET);
        }
    }
}