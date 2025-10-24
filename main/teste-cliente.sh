#!/bin/bash

# Script de teste automático do cliente
echo "=========================================="
echo "  TESTE AUTOMÁTICO DO CLIENTE"
echo "=========================================="

PORTA=${1:-12345}
LOG_DIR="logs/$(ls -t logs/ | head -1)"

echo "Testando cliente na porta $PORTA"
echo "Logs em: $LOG_DIR"

# Teste 1: Novo vetor
echo ""
echo "Teste 1: Criando novo vetor..."
echo "N" | java D $PORTA > "$LOG_DIR/cliente_teste1.log" 2>&1 &
CLIENTE_PID=$!
sleep 5
kill $CLIENTE_PID 2>/dev/null

# Teste 2: Contar 111
echo ""
echo "Teste 2: Contando ocorrências de 111..."
echo -e "C\nS" | java D $PORTA > "$LOG_DIR/cliente_teste2.log" 2>&1 &
CLIENTE_PID=$!
sleep 5
kill $CLIENTE_PID 2>/dev/null

echo ""
echo "Testes concluídos! Verifique os logs:"
echo "  - $LOG_DIR/cliente_teste1.log"
echo "  - $LOG_DIR/cliente_teste2.log"
