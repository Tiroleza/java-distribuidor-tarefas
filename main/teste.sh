#!/bin/bash

# Script de teste simples
echo "=========================================="
echo "  SISTEMA DE CONTAGEM DISTRIBUÍDA"
echo "=========================================="

# Compilar
echo "Compilando sistema..."
javac *.java

if [ $? -ne 0 ]; then
    echo "ERRO: Falha na compilação!"
    exit 1
fi

echo "Compilação concluída com sucesso!"
echo ""
echo "⚠️  IMPORTANTE: Para evitar LinkageError, SEMPRE use:"
echo "   1. Compilar: javac *.java"
echo "   2. Executar: java -Xmx3G D (NÃO use java D.java)"
echo ""
echo "Para usar 3GB de memória, execute com:"
echo "   java -Xmx3G D"
echo "   java -Xmx1G R 12345"
echo "   java -Xmx1G R 12346"
echo "   java -Xmx1G R 12347"
echo ""
echo "Para testar o sistema:"
echo "1. Em terminais separados, execute:"
echo "   java R 12345"
echo "   java R 12346" 
echo "   java R 12347"
echo ""
echo "2. Em outro terminal, execute:"
echo "   java D"
echo ""
echo "3. Teste as opções:"
echo "   - N: Novo vetor (gera vetor aleatório entre -100 e 100)"
echo "   - M: Mostrar vetor"
echo "   - S: Sair"
echo ""
echo "=========================================="
