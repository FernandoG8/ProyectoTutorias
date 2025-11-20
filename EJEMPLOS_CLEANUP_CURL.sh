#!/bin/bash

# =============================================================================
# EJEMPLOS DE USO - CLEANUP DE ASIGNACIONES CORRUPTAS
# =============================================================================
# Este script contiene ejemplos curl para usar los endpoints de limpieza
#
# REQUISITOS:
# - Backend corriendo en http://localhost:8080
# - Token de usuario con rol ADMIN
# - Reemplazar YOUR_ADMIN_TOKEN con token real
# =============================================================================

# Color codes para output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Variables
BASE_URL="http://localhost:8080"
TOKEN="YOUR_ADMIN_TOKEN"  # ← REEMPLAZAR CON TOKEN REAL
SEMESTRE_ID="5"           # ← REEMPLAZAR CON ID REAL

# =============================================================================
# TEST 1: Verificar salud del servicio
# =============================================================================
echo -e "${BLUE}=== TEST 1: Verificar Salud del Servicio ===${NC}"
echo "Endpoint: GET /api/cleanup/health"
echo ""

curl -X GET "${BASE_URL}/api/cleanup/health" \
  -H "Authorization: Bearer ${TOKEN}" \
  -H "Content-Type: application/json" | jq '.'

echo ""
echo -e "${GREEN}✓ Si ves 'status': 'OK', el servicio está activo${NC}"
echo ""

# =============================================================================
# TEST 2: Detectar corrupción en un semestre específico
# =============================================================================
echo -e "${BLUE}=== TEST 2: Detectar Corrupción en Semestre Específico ===${NC}"
echo "Endpoint: GET /api/cleanup/detectar/{semestreId}"
echo "Semestre ID: ${SEMESTRE_ID}"
echo "Acción: SOLO LECTURA - No modifica datos"
echo ""

curl -X GET "${BASE_URL}/api/cleanup/detectar/${SEMESTRE_ID}" \
  -H "Authorization: Bearer ${TOKEN}" \
  -H "Content-Type: application/json" | jq '.'

echo ""
echo -e "${YELLOW}⚠ Guardar el resultado para comparar después de limpiar${NC}"
echo ""

# =============================================================================
# TEST 3: Detectar corrupción global
# =============================================================================
echo -e "${BLUE}=== TEST 3: Detectar Corrupción Global ===${NC}"
echo "Endpoint: GET /api/cleanup/detectar-global"
echo "Acción: SOLO LECTURA - Analiza TODOS los semestres"
echo ""

curl -X GET "${BASE_URL}/api/cleanup/detectar-global" \
  -H "Authorization: Bearer ${TOKEN}" \
  -H "Content-Type: application/json" | jq '.'

echo ""
echo -e "${YELLOW}⚠ Identifica qué semestres tienen más corrupción${NC}"
echo ""

# =============================================================================
# TEST 4: LIMPIAR - Semestre específico (CON CONFIRMACIÓN)
# =============================================================================
echo -e "${RED}=== TEST 4: Limpiar Semestre Específico (DESTRUCTIVO) ===${NC}"
echo "Endpoint: POST /api/cleanup/limpiar/{semestreId}?confirmar=CONFIRMAR_LIMPIEZA"
echo "Semestre ID: ${SEMESTRE_ID}"
echo ""
echo -e "${RED}⚠️  ADVERTENCIA: Esta operación ELIMINA registros de la BD${NC}"
echo -e "${RED}⚠️  Asegúrate de hacer backup primero!${NC}"
echo ""
echo "Comando:"
echo ""

# Mostrar el comando sin ejecutarlo por seguridad
cat << EOF
curl -X POST "${BASE_URL}/api/cleanup/limpiar/${SEMESTRE_ID}?confirmar=CONFIRMAR_LIMPIEZA" \
  -H "Authorization: Bearer ${TOKEN}" \
  -H "Content-Type: application/json"
EOF

echo ""
echo -e "${YELLOW}Para ejecutar, descomenta la siguiente línea en este script:${NC}"
echo ""

# DESCOMENTA ESTA LÍNEA PARA EJECUTAR REALMENTE:
# curl -X POST "${BASE_URL}/api/cleanup/limpiar/${SEMESTRE_ID}?confirmar=CONFIRMAR_LIMPIEZA" \
#   -H "Authorization: Bearer ${TOKEN}" \
#   -H "Content-Type: application/json" | jq '.'

echo ""

# =============================================================================
# TEST 5: LIMPIAR - Global (CON DOBLE CONFIRMACIÓN)
# =============================================================================
echo -e "${RED}=== TEST 5: Limpiar GLOBALMENTE (DESTRUCTIVO GLOBAL) ===${NC}"
echo "Endpoint: POST /api/cleanup/limpiar-global?confirmar=CONFIRMAR_LIMPIEZA_GLOBAL"
echo ""
echo -e "${RED}⚠️  ADVERTENCIA CRÍTICA: Esto LIMPIA TODO el sistema${NC}"
echo -e "${RED}⚠️  Solo ejecutar si sabes exactamente qué haces${NC}"
echo ""
echo "Comando:"
echo ""

cat << EOF
curl -X POST "${BASE_URL}/api/cleanup/limpiar-global?confirmar=CONFIRMAR_LIMPIEZA_GLOBAL" \
  -H "Authorization: Bearer ${TOKEN}" \
  -H "Content-Type: application/json"
EOF

echo ""
echo -e "${YELLOW}Para ejecutar, descomenta la siguiente línea en este script:${NC}"
echo ""

# DESCOMENTA ESTA LÍNEA PARA EJECUTAR REALMENTE:
# curl -X POST "${BASE_URL}/api/cleanup/limpiar-global?confirmar=CONFIRMAR_LIMPIEZA_GLOBAL" \
#   -H "Authorization: Bearer ${TOKEN}" \
#   -H "Content-Type: application/json" | jq '.'

echo ""

# =============================================================================
# TEST 6: Recalcular cargas después de limpiar
# =============================================================================
echo -e "${BLUE}=== TEST 6: Recalcular Cargas de Tutores ===${NC}"
echo "Endpoint: POST /api/cleanup/recalcular?semestreId={semestreId}"
echo "Semestre ID: ${SEMESTRE_ID} (opcional, omitir para todos)"
echo "Acción: Recalcula carga de tutores después de limpieza"
echo ""

curl -X POST "${BASE_URL}/api/cleanup/recalcular?semestreId=${SEMESTRE_ID}" \
  -H "Authorization: Bearer ${TOKEN}" \
  -H "Content-Type: application/json" | jq '.'

echo ""
echo -e "${GREEN}✓ Cargas sincronizadas${NC}"
echo ""

# =============================================================================
# TEST 7: Verificar después de limpiar
# =============================================================================
echo -e "${BLUE}=== TEST 7: Verificar Estado Después de Limpiar ===${NC}"
echo "Endpoint: GET /api/cleanup/detectar/{semestreId}"
echo "Semestre ID: ${SEMESTRE_ID}"
echo ""

curl -X GET "${BASE_URL}/api/cleanup/detectar/${SEMESTRE_ID}" \
  -H "Authorization: Bearer ${TOKEN}" \
  -H "Content-Type: application/json" | jq '.'

echo ""
echo -e "${GREEN}✓ Si ves 'estado': 'LIMPIO' y 'total_corrupcion': 0, la limpieza fue exitosa${NC}"
echo ""

# =============================================================================
# FLUJO COMPLETO PASO A PASO
# =============================================================================
echo -e "${BLUE}════════════════════════════════════════════════════════════════════${NC}"
echo -e "${BLUE}FLUJO COMPLETO PARA LIMPIAR UN SEMESTRE${NC}"
echo -e "${BLUE}════════════════════════════════════════════════════════════════════${NC}"
echo ""

cat << 'EOF'
PASO 1: Detectar Corrupción
  curl -X GET "http://localhost:8080/api/cleanup/detectar/5" \
    -H "Authorization: Bearer YOUR_TOKEN"

PASO 2: Revisar Resultado
  - Anotar total_corrupcion
  - Revisar qué tipo de corrupción hay

PASO 3: Hacer Backup
  mysqldump -u usuario -p base_datos > backup_antes_limpieza.sql

PASO 4: Limpiar Semestre
  curl -X POST "http://localhost:8080/api/cleanup/limpiar/5?confirmar=CONFIRMAR_LIMPIEZA" \
    -H "Authorization: Bearer YOUR_TOKEN"

PASO 5: Recalcular Cargas
  curl -X POST "http://localhost:8080/api/cleanup/recalcular?semestreId=5" \
    -H "Authorization: Bearer YOUR_TOKEN"

PASO 6: Verificar
  curl -X GET "http://localhost:8080/api/cleanup/detectar/5" \
    -H "Authorization: Bearer YOUR_TOKEN"

  Esperado:
  - "estado": "LIMPIO"
  - "total_corrupcion": 0

EOF

echo ""

# =============================================================================
# ALTERNATIVA: LIMPIAR GLOBALMENTE
# =============================================================================
echo -e "${BLUE}════════════════════════════════════════════════════════════════════${NC}"
echo -e "${RED}FLUJO COMPLETO PARA LIMPIAR GLOBALMENTE (TODO EL SISTEMA)${NC}"
echo -e "${BLUE}════════════════════════════════════════════════════════════════════${NC}"
echo ""

cat << 'EOF'
PASO 1: Detectar Corrupción Global
  curl -X GET "http://localhost:8080/api/cleanup/detectar-global" \
    -H "Authorization: Bearer YOUR_TOKEN"

PASO 2: Revisar Resultado
  - Anotar total_corrupcion
  - Revisar detalles_por_semestre

PASO 3: Hacer Backup COMPLETO
  mysqldump -u usuario -p base_datos > backup_antes_limpieza_global.sql

PASO 4: Limpiar TODO
  curl -X POST "http://localhost:8080/api/cleanup/limpiar-global?confirmar=CONFIRMAR_LIMPIEZA_GLOBAL" \
    -H "Authorization: Bearer YOUR_TOKEN"

PASO 5: Recalcular TODO
  curl -X POST "http://localhost:8080/api/cleanup/recalcular" \
    -H "Authorization: Bearer YOUR_TOKEN"

PASO 6: Verificar TODO
  curl -X GET "http://localhost:8080/api/cleanup/detectar-global" \
    -H "Authorization: Bearer YOUR_TOKEN"

  Esperado:
  - "total_corrupcion": 0

EOF

echo ""

# =============================================================================
# NOTAS IMPORTANTES
# =============================================================================
echo -e "${BLUE}════════════════════════════════════════════════════════════════════${NC}"
echo -e "${YELLOW}NOTAS IMPORTANTES${NC}"
echo -e "${BLUE}════════════════════════════════════════════════════════════════════${NC}"
echo ""

cat << 'EOF'
1. REEMPLAZAR VARIABLES:
   - YOUR_ADMIN_TOKEN: Tu token JWT real
   - Semestre ID: El ID real del semestre en la BD

2. SEGURIDAD:
   - Estos endpoints requieren rol ADMIN
   - El parámetro 'confirmar' es obligatorio
   - Las operaciones son transaccionales (rollback en error)

3. AUDITORÍA:
   - Todas las eliminaciones se registran en logs_auditoria
   - Campo 'usuario' marcará como 'CLEANUP_SYSTEM'
   - Campo 'tipo_accion' será 'ELIMINACION_ASIGNACION'

4. BACKUP:
   - SIEMPRE hacer backup antes de limpiar
   - Guardar con timestamp: backup_2024-01-20_antes_limpieza.sql

5. VERIFICACIÓN:
   - Después de limpiar, SIEMPRE ejecutar detectar
   - Confirmar que 'estado' = 'LIMPIO'
   - Confirmar que 'total_corrupcion' = 0

6. RECALCULO:
   - Es MUY IMPORTANTE ejecutar recalcular después
   - Sino la carga de tutores queda desincronizada

7. LOGS:
   - Ver logs_auditoria para historial completo:
     SELECT * FROM logs_auditoria
     WHERE tipo_accion = 'ELIMINACION_ASIGNACION'
     ORDER BY fecha_registro DESC;

EOF

echo ""
echo -e "${GREEN}════════════════════════════════════════════════════════════════════${NC}"
echo -e "${GREEN}Ejemplos completos - Ver GUIA_CLEANUP_ASIGNACIONES.md para más${NC}"
echo -e "${GREEN}════════════════════════════════════════════════════════════════════${NC}"
echo ""
