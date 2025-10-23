#!/bin/bash

echo "=== SISTEMA DE TESTES LOCAIS ==="
echo "Este script executa o sistema distribuído localmente com 4 portas"
echo ""

# Compilar todas as classes de teste
echo "Compilando classes de teste..."
cd Testes
javac *.java
if [ $? -ne 0 ]; then
    echo "Erro na compilação!"
    exit 1
fi
echo "✓ Compilação concluída"
echo ""

# Função para limpar processos ao sair
cleanup() {
    echo ""
    echo "Encerrando processos..."
    pkill -f "java ReceptorLocal"
    pkill -f "java DistribuidorLocal"
    pkill -f "java ClienteServidorLocal"
    exit 0
}

# Capturar Ctrl+C
trap cleanup SIGINT

echo "Escolha uma opção:"
echo "1. Teste automático completo (ReceptorLocal + DistribuidorLocal)"
echo "2. Teste super rápido (ClienteServidorLocal)"
echo "3. Executar apenas ReceptorLocal (4 portas)"
echo "4. Executar apenas DistribuidorLocal"
echo "5. Estimar tamanho máximo do vetor"
echo ""
read -p "Opção: " opcao

case $opcao in
    1)
        echo ""
        echo "Iniciando ReceptorLocal em background..."
        java ReceptorLocal &
        sleep 3
        
        echo "Iniciando DistribuidorLocal..."
        java DistribuidorLocal
        ;;
    2)
        echo ""
        echo "Executando teste super rápido..."
        java ClienteServidorLocal
        ;;
    3)
        echo ""
        echo "Executando ReceptorLocal..."
        echo "Pressione Ctrl+C para parar"
        java ReceptorLocal
        ;;
    4)
        echo ""
        echo "Executando DistribuidorLocal..."
        echo "IMPORTANTE: Certifique-se de que ReceptorLocal está rodando!"
        java DistribuidorLocal
        ;;
    5)
        echo ""
        echo "Estimando tamanho máximo do vetor..."
        echo "Execute com: java -Xmx4G MaiorVetorAproximado"
        java MaiorVetorAproximado
        ;;
    *)
        echo "Opção inválida!"
        exit 1
        ;;
esac

# Aguardar processos em background
wait