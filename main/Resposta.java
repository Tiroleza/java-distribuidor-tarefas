/**
 * Classe serializável que encapsula o resultado de uma contagem.
 * Retorna o número total de ocorrências encontradas pelo servidor.
 */
public class Resposta extends Comunicado
{
    private Integer contagem;

    public Resposta (int contagem)
    {
        this.contagem = contagem;
    }

    public Integer getContagem ()
    {
        return this.contagem;
    }
}

