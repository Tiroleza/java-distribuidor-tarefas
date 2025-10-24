public class Pedido extends Comunicado
{
    private byte[] numeros;
    private int procurado;
    
    public Pedido (byte[] numeros, int procurado)
    {
        this.numeros = numeros;
        this.procurado = procurado;
    }
    
    public byte[] getNumeros ()
    {
        return this.numeros;
    }
    
    public int getProcurado ()
    {
        return this.procurado;
    }
    
    public int contar()
    {
        int contagem = 0;
        for (byte numero : this.numeros)
        {
            if (numero == this.procurado)
            {
                contagem++;
            }
        }
        return contagem;
    }
}

