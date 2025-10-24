#!/bin/bash

# Cores para logs
VERDE='\033[32m'
AZUL='\033[34m'
AMARELO='\033[33m'
VERMELHO='\033[31m'
CIANO='\033[36m'
RESET='\033[0m'

echo -e "${CIANO}==========================================${RESET}"
echo -e "${CIANO}TESTE DE COMPARAÇÃO DE PERFORMANCE${RESET}"
echo -e "${CIANO}==========================================${RESET}"

# Compilar todos os arquivos
echo -e "${AZUL}Compilando arquivos Java...${RESET}"
javac *.java
if [ $? -ne 0 ]; then
    echo -e "${VERMELHO}Erro na compilação!${RESET}"
    exit 1
fi
echo -e "${VERDE}✓ Compilação concluída!${RESET}"

echo -e "\n${CIANO}INSTRUÇÕES PARA TESTE:${RESET}"
echo -e "${AMARELO}1. Abra 4 terminais diferentes${RESET}"
echo -e "${AMARELO}2. Execute os comandos abaixo em cada terminal${RESET}"

echo -e "\n${AZUL}TERMINAL 1 - Servidor 1:${RESET}"
echo -e "${VERDE}java -Xmx1G R 12345${RESET}"

echo -e "\n${AZUL}TERMINAL 2 - Servidor 2:${RESET}"
echo -e "${VERDE}java -Xmx1G R 12346${RESET}"

echo -e "\n${AZUL}TERMINAL 3 - Servidor 3:${RESET}"
echo -e "${VERDE}java -Xmx1G R 12347${RESET}"

echo -e "\n${AZUL}TERMINAL 4 - Cliente Distribuído:${RESET}"
echo -e "${VERDE}java -Xmx3G D${RESET}"
echo -e "${AMARELO}No menu, use:${RESET}"
echo -e "${AMARELO}  [A] - Gerar vetor máximo${RESET}"
echo -e "${AMARELO}  [C] - Contar número aleatório${RESET}"
echo -e "${AMARELO}  [T] - Terminar${RESET}"

echo -e "\n${AZUL}TERMINAL 5 - Contagem Sequencial:${RESET}"
echo -e "${VERDE}java -Xmx3G ContagemSequencial${RESET}"
echo -e "${AMARELO}No menu, use:${RESET}"
echo -e "${AMARELO}  [A] - Gerar vetor máximo${RESET}"
echo -e "${AMARELO}  [C] - Contar número aleatório${RESET}"
echo -e "${AMARELO}  [T] - Terminar${RESET}"

echo -e "\n${CIANO}COMPARAÇÃO:${RESET}"
echo -e "${AMARELO}• Anote os tempos de processamento de ambos os sistemas${RESET}"
echo -e "${AMARELO}• O sistema distribuído deve ser mais rápido para vetores grandes${RESET}"
echo -e "${AMARELO}• Compare: Tempo total vs Tempo de cada thread${RESET}"

echo -e "\n${VERDE}✓ Script de teste criado!${RESET}"
