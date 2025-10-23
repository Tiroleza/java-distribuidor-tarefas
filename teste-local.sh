#!/bin/bash

echo "=== TESTE LOCAL COM MÚLTIPLAS INSTÂNCIAS ==="
echo "Este script executa múltiplas instâncias do Receptor para teste local"
echo ""

# Compilar primeiro
echo "Compilando classes..."
cd codigo-ref/Servidor
javac *.java
if [ $? -ne 0 ]; then
    echo "Erro na compilação!"
    exit 1
fi

cd ../Cliente
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
    pkill -f "java Receptor"
    pkill -f "java Distribuidor"
    exit 0
}

# Capturar Ctrl+C
trap cleanup SIGINT

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
echo "Iniciando DistribuidorLocalGrande..."
echo ""

# Executar distribuidor local com vetor grande
cd ../Cliente
java DistribuidorLocalGrande

# Aguardar
wait
