#!/bin/bash

echo "=== SISTEMA DISTRIBUÍDO DE CONTAGEM ==="
echo "Compilando todas as classes..."

# Compilar classes do Cliente
cd codigo-ref/Cliente
javac *.java
if [ $? -eq 0 ]; then
    echo "✓ Classes do Cliente compiladas com sucesso"
else
    echo "✗ Erro na compilação das classes do Cliente"
    exit 1
fi

# Compilar classes do Servidor
cd ../Servidor
javac *.java
if [ $? -eq 0 ]; then
    echo "✓ Classes do Servidor compiladas com sucesso"
else
    echo "✗ Erro na compilação das classes do Servidor"
    exit 1
fi

cd ../..

echo ""
echo "=== OPÇÕES DE EXECUÇÃO ==="
echo "1. Executar programa principal integrado"
echo "2. Executar Receptor (servidor)"
echo "3. Executar Distribuidor (cliente)"
echo "4. Executar contagem sequencial"
echo "5. Executar teste do sistema"
echo "6. Sair"
echo ""
read -p "Escolha uma opção: " opcao

case $opcao in
    1)
        echo "Executando programa principal..."
        cd codigo-ref/Cliente
        java SistemaContagem
        ;;
    2)
        echo "Executando Receptor..."
        echo "Digite a porta (ou pressione Enter para usar 12345):"
        read porta
        cd codigo-ref/Servidor
        if [ -z "$porta" ]; then
            java Receptor
        else
            java Receptor $porta
        fi
        ;;
    3)
        echo "Executando Distribuidor..."
        echo "IMPORTANTE: Certifique-se de que os servidores R estão rodando!"
        cd codigo-ref/Cliente
        java Distribuidor
        ;;
    4)
        echo "Executando contagem sequencial..."
        cd codigo-ref/Cliente
        java ContadorSequencial
        ;;
    5)
        echo "Executando teste do sistema..."
        cd codigo-ref/Cliente
        java TesteSistema
        ;;
    6)
        echo "Saindo..."
        exit 0
        ;;
    *)
        echo "Opção inválida!"
        exit 1
        ;;
esac
