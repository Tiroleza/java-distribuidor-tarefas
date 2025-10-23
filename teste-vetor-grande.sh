#!/bin/bash

echo "=== TESTE COM VETOR GRANDE ==="
echo "Este script executa o sistema com vetores grandes baseados no MaiorVetorAproximado"
echo ""

# Compilar primeiro
echo "Compilando classes..."
cd codigo-ref/Servidor
javac *.java
if [ $? -ne 0 ]; then
    echo "Erro na compilação do servidor!"
    exit 1
fi

cd ../Cliente
javac *.java
if [ $? -ne 0 ]; then
    echo "Erro na compilação do cliente!"
    exit 1
fi

echo "✓ Compilação concluída"
echo ""

# Função para limpar processos ao sair
cleanup() {
    echo ""
    echo "Encerrando processos..."
    pkill -f "java Receptor"
    pkill -f "java Distribuidor"
    exit 0
}

# Capturar Ctrl+C
trap cleanup SIGINT

echo "Escolha o tamanho do vetor:"
echo "1. 10 milhões (10MB)"
echo "2. 50 milhões (50MB)"
echo "3. 100 milhões (100MB)"
echo "4. 500 milhões (500MB)"
echo "5. 1 bilhão (1GB)"
echo "6. Personalizado"
echo ""
read -p "Opção: " opcao

case $opcao in
    1)
        tamanho=10000000
        ;;
    2)
        tamanho=50000000
        ;;
    3)
        tamanho=100000000
        ;;
    4)
        tamanho=500000000
        ;;
    5)
        tamanho=1000000000
        ;;
    6)
        read -p "Digite o tamanho do vetor: " tamanho
        ;;
    *)
        echo "Opção inválida! Usando tamanho padrão: 100 milhões"
        tamanho=100000000
        ;;
esac

echo ""
echo "Tamanho escolhido: $(printf "%'d" $tamanho) elementos"
echo "Memória estimada: $(echo "scale=2; $tamanho / 1024 / 1024" | bc) MB"
echo ""

# Verificar se há memória suficiente
memoria_disponivel=$(free -m | awk 'NR==2{printf "%.0f", $7}')
memoria_necessaria=$(echo "scale=0; $tamanho / 1024 / 1024 * 2" | bc)

if [ $memoria_necessaria -gt $memoria_disponivel ]; then
    echo "⚠️  AVISO: Pode não haver memória suficiente!"
    echo "Memória disponível: ${memoria_disponivel}MB"
    echo "Memória necessária: ${memoria_necessaria}MB"
    echo ""
    read -p "Continuar mesmo assim? (s/n): " continuar
    if [ "$continuar" != "s" ] && [ "$continuar" != "S" ]; then
        echo "Operação cancelada."
        exit 0
    fi
fi

echo "Iniciando múltiplas instâncias do Receptor..."
echo "Pressione Ctrl+C para parar todos os processos"
echo ""

# Iniciar receptores em portas diferentes
cd ../Servidor
java Receptor 12345 &
java Receptor 12346 &
java Receptor 12347 &
java Receptor 12348 &

echo "4 instâncias do Receptor iniciadas nas portas 12345-12348"
echo "Aguardando 3 segundos para estabilização..."
sleep 3

echo ""
echo "Iniciando DistribuidorLocalGrande com vetor de $(printf "%'d" $tamanho) elementos..."
echo ""

# Executar distribuidor local com tamanho personalizado
cd ../Cliente
java DistribuidorLocalGrande $tamanho

# Aguardar
wait
