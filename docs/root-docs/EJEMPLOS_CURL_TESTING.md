# 🧪 EJEMPLOS DE CURL PARA TESTING - SISTEMA DE TUTORÍAS

**Fecha:** 20 de Noviembre de 2024
**Propósito:** Guía rápida para probar endpoints con curl

---

## 🔧 CONFIGURACIÓN INICIAL

```bash
# Variables útiles
API_URL="http://localhost:8080"
USERNAME="coordinador"
PASSWORD="password123"

# Guardar tokens en variables
ACCESS_TOKEN=""
REFRESH_TOKEN=""
```

---

## 🔐 MÓDULO 1: AUTENTICACIÓN

### 1.1 Login
```bash
curl -X POST "${API_URL}/auth/login" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "coordinador",
    "password": "password123"
  }' \
  -v  # Muestra headers de respuesta (incluyendo cookies)
```

**Guardar tokens después del login:**
```bash
# Nota: Los tokens se guardan en cookies automáticamente
# El navegador los maneja, pero en curl hay que extraer manualmente
curl -X POST "${API_URL}/auth/login" \
  -H "Content-Type: application/json" \
  -c ./cookies.txt \
  -d '{
    "username": "coordinador",
    "password": "password123"
  }'

# Usar cookies en siguientes requests
curl -X GET "${API_URL}/auth/me" \
  -b ./cookies.txt
```

### 1.2 Obtener Info del Usuario
```bash
curl -X GET "${API_URL}/auth/me" \
  -b ./cookies.txt
```

### 1.3 Refresh Token
```bash
curl -X POST "${API_URL}/auth/refresh" \
  -H "Content-Type: application/json" \
  -b ./cookies.txt
```

### 1.4 Logout
```bash
curl -X POST "${API_URL}/auth/logout" \
  -H "Content-Type: application/json" \
  -b ./cookies.txt
```

### 1.5 Registrar Usuario (Requiere Admin)
```bash
curl -X POST "${API_URL}/auth/register" \
  -H "Content-Type: application/json" \
  -b ./cookies.txt \
  -d '{
    "username": "nuevo_coordinador",
    "password": "SecurePass123!",
    "nombre": "Juan García",
    "email": "juan@universidad.edu",
    "rol": "COORDINADOR_TUTORIAS"
  }'
```

---

## 👥 MÓDULO 2: ALUMNOS

### 2.1 Listar Alumnos del Semestre Actual
```bash
curl -X GET "${API_URL}/api/alumnos/semestre-actual?page=1&limit=20" \
  -b ./cookies.txt
```

### 2.2 Listar Todos los Alumnos con Filtros
```bash
# Solo activos
curl -X GET "${API_URL}/api/alumnos?page=1&limit=20&estado=ACTIVO&soloActivo=true" \
  -b ./cookies.txt

# Por carrera
curl -X GET "${API_URL}/api/alumnos?page=1&limit=20&carrera=ISC" \
  -b ./cookies.txt

# Semestre específico
curl -X GET "${API_URL}/api/alumnos?page=1&limit=20&semestreId=3" \
  -b ./cookies.txt
```

### 2.3 Obtener Alumno por ID
```bash
curl -X GET "${API_URL}/api/alumnos/15" \
  -b ./cookies.txt
```

### 2.4 Buscar por Matrícula
```bash
curl -X GET "${API_URL}/api/alumnos/by-matricula/202401001" \
  -b ./cookies.txt
```

### 2.5 Autocomplete de Alumnos
```bash
curl -X GET "${API_URL}/api/alumnos/autocomplete?q=car&estado=ACTIVO" \
  -b ./cookies.txt
```

### 2.6 Búsqueda Avanzada
```bash
curl -X GET "${API_URL}/api/alumnos/search?q=carlos&carrera=ISC&limit=10" \
  -b ./cookies.txt
```

### 2.7 Crear Alumno
```bash
curl -X POST "${API_URL}/api/alumnos" \
  -H "Content-Type: application/json" \
  -b ./cookies.txt \
  -d '{
    "matricula": "202401050",
    "nombre": "Roberto Sánchez",
    "carrera": "ISC",
    "semestre": 1,
    "estado": "ACTIVO",
    "semestreId": 2
  }'
```

### 2.8 Actualizar Alumno (PUT)
```bash
curl -X PUT "${API_URL}/api/alumnos/15" \
  -H "Content-Type: application/json" \
  -b ./cookies.txt \
  -d '{
    "matricula": "202401015",
    "nombre": "Patricia Elena Martínez García",
    "carrera": "ADM",
    "semestre": 4,
    "estado": "ACTIVO",
    "semestreId": 2
  }'
```

### 2.9 Actualizar Parcialmente (PATCH)
```bash
curl -X PATCH "${API_URL}/api/alumnos/15" \
  -H "Content-Type: application/json" \
  -b ./cookies.txt \
  -d '{
    "nombre": "Patricia Elena Martínez García",
    "semestre": 4
  }'
```

### 2.10 Eliminar Alumno
```bash
curl -X DELETE "${API_URL}/api/alumnos/15" \
  -b ./cookies.txt
```

---

## 👨‍🏫 MÓDULO 3: TUTORES

### 3.1 Listar Tutores
```bash
# Todos
curl -X GET "${API_URL}/api/tutores" \
  -b ./cookies.txt

# Solo disponibles
curl -X GET "${API_URL}/api/tutores?disponibles=true" \
  -b ./cookies.txt

# De una carrera
curl -X GET "${API_URL}/api/tutores?carrera=ISC" \
  -b ./cookies.txt

# Activos y disponibles
curl -X GET "${API_URL}/api/tutores?activos=true&disponibles=true" \
  -b ./cookies.txt
```

### 3.2 Obtener Tutor
```bash
curl -X GET "${API_URL}/api/tutores/5" \
  -b ./cookies.txt
```

### 3.3 Alumnos de un Tutor
```bash
curl -X GET "${API_URL}/api/tutores/5/alumnos?semestreAcademico=2024-04" \
  -b ./cookies.txt
```

### 3.4 Autocomplete Tutores
```bash
curl -X GET "${API_URL}/api/tutores/autocomplete?q=fran" \
  -b ./cookies.txt
```

### 3.5 Búsqueda Tutores
```bash
curl -X GET "${API_URL}/api/tutores/search?q=pérez&carrera=ISC" \
  -b ./cookies.txt
```

### 3.6 Crear Tutor
```bash
curl -X POST "${API_URL}/api/tutores" \
  -H "Content-Type: application/json" \
  -b ./cookies.txt \
  -d '{
    "nombre": "Dr. Juan Rodríguez",
    "carrera": "ADM",
    "capacidadMax": 25,
    "areaAtencion": "Finanzas",
    "letraEdificio": "C"
  }'
```

### 3.7 Actualizar Tutor
```bash
curl -X PUT "${API_URL}/api/tutores/25" \
  -H "Content-Type: application/json" \
  -b ./cookies.txt \
  -d '{
    "nombre": "Dr. Juan Carlos Rodríguez",
    "capacidadMax": 30,
    "areaAtencion": "Finanzas y Contabilidad",
    "activo": true
  }'
```

### 3.8 Eliminar Tutor
```bash
curl -X DELETE "${API_URL}/api/tutores/25" \
  -b ./cookies.txt
```

---

## 📚 MÓDULO 4: SEMESTRES

### 4.1 Listar Semestres
```bash
curl -X GET "${API_URL}/api/semestres" \
  -b ./cookies.txt
```

### 4.2 Últimos N Semestres
```bash
curl -X GET "${API_URL}/api/semestres/ultimos?cantidad=5" \
  -b ./cookies.txt
```

### 4.3 Semestre Activo
```bash
curl -X GET "${API_URL}/api/semestres/activo" \
  -b ./cookies.txt
```

### 4.4 Obtener por ID
```bash
curl -X GET "${API_URL}/api/semestres/3" \
  -b ./cookies.txt
```

### 4.5 Obtener por Código
```bash
curl -X GET "${API_URL}/api/semestres/codigo/2024-04" \
  -b ./cookies.txt
```

### 4.6 Verificar si Existe
```bash
curl -X GET "${API_URL}/api/semestres/existe/codigo/2024-04" \
  -b ./cookies.txt
```

### 4.7 Estadísticas de Semestre
```bash
curl -X GET "${API_URL}/api/semestres/3/estadisticas" \
  -b ./cookies.txt
```

### 4.8 Crear Semestre
```bash
curl -X POST "${API_URL}/api/semestres" \
  -H "Content-Type: application/json" \
  -b ./cookies.txt \
  -d '{
    "codigo": "2025-01",
    "nombre": "Semestre Enero-Junio 2025",
    "fechaInicio": "2025-01-15",
    "fechaFin": "2025-06-30"
  }'
```

### 4.9 Actualizar Semestre
```bash
curl -X PUT "${API_URL}/api/semestres/5" \
  -H "Content-Type: application/json" \
  -b ./cookies.txt \
  -d '{
    "nombre": "Semestre Enero-Junio 2025 (Revisado)",
    "fechaFin": "2025-07-15"
  }'
```

### 4.10 Activar Semestre
```bash
curl -X POST "${API_URL}/api/semestres/5/activar" \
  -H "Content-Type: application/json" \
  -b ./cookies.txt
```

### 4.11 Desactivar Semestre
```bash
curl -X POST "${API_URL}/api/semestres/5/desactivar" \
  -H "Content-Type: application/json" \
  -b ./cookies.txt
```

### 4.12 Eliminar Semestre
```bash
curl -X DELETE "${API_URL}/api/semestres/5" \
  -b ./cookies.txt
```

---

## 📋 MÓDULO 5: ASIGNACIONES ⭐

### 5.1 Iniciar Proceso (Cargar Excel)
```bash
# Con archivo Excel real
curl -X POST "${API_URL}/api/asignaciones/iniciar" \
  -b ./cookies.txt \
  -F "archivo=@./Alumnos_2024-04.xlsx" \
  -F "semestreAcademico=2024-04" \
  -F "usuario=coordinador"
```

### 5.2 Validar Excel (Sin Ejecutar)
```bash
curl -X POST "${API_URL}/api/asignaciones/validar-excel" \
  -b ./cookies.txt \
  -F "archivo=@./Alumnos_2024-04.xlsx" \
  -F "semestreId=3"
```

### 5.3 Ejecutar Asignación
```bash
curl -X POST "${API_URL}/api/asignaciones/ejecutar" \
  -H "Content-Type: application/json" \
  -b ./cookies.txt \
  -d '{
    "semestreId": 3,
    "alumnosAAsignar": [
      {
        "matricula": "202401001",
        "nombre": "Carlos López",
        "carrera": "ISC",
        "semestre": 3,
        "tutorAsignado": null
      }
    ],
    "usuario": "coordinador"
  }'
```

### 5.4 Obtener Estado del Proceso
```bash
curl -X GET "${API_URL}/api/asignaciones/proceso/42" \
  -b ./cookies.txt
```

### 5.5 Obtener Alertas del Proceso
```bash
# Todas las alertas
curl -X GET "${API_URL}/api/asignaciones/proceso/42/alertas" \
  -b ./cookies.txt

# Solo ERRORS
curl -X GET "${API_URL}/api/asignaciones/proceso/42/alertas?severidad=ERROR" \
  -b ./cookies.txt

# No resueltas
curl -X GET "${API_URL}/api/asignaciones/proceso/42/alertas?resuelta=false" \
  -b ./cookies.txt
```

### 5.6 Listar Todos los Procesos
```bash
curl -X GET "${API_URL}/api/asignaciones/procesos" \
  -b ./cookies.txt
```

### 5.7 Cambio Manual de Tutor
```bash
curl -X POST "${API_URL}/api/asignaciones/cambio-tutor" \
  -H "Content-Type: application/json" \
  -b ./cookies.txt \
  -d '{
    "alumnoId": 15,
    "tutorOrigenId": 5,
    "tutorDestinoId": 7,
    "motivo": "Solicitud del alumno por horarios",
    "usuarioResponsable": "coordinador"
  }'
```

---

## 🚫 MÓDULO 6: ALUMNOS INACTIVOS

### 6.1 Listados
```bash
# Pendientes de motivo
curl -X GET "${API_URL}/api/alumnos-inactivos/pendientes" \
  -b ./cookies.txt

# Todos
curl -X GET "${API_URL}/api/alumnos-inactivos" \
  -b ./cookies.txt
```

### 6.2 Asignar Motivo
```bash
curl -X PUT "${API_URL}/api/alumnos-inactivos/1/motivo" \
  -H "Content-Type: application/json" \
  -b ./cookies.txt \
  -d '{
    "motivo": "BAJA_TEMPORAL",
    "descripcion": "Alumno se retira temporalmente por razones de salud"
  }'
```

### 6.3 Resolver Inactividad
```bash
curl -X PATCH "${API_URL}/api/alumnos-inactivos/1/resolver-motivo" \
  -H "Content-Type: application/json" \
  -b ./cookies.txt \
  -d '{
    "reactivar": true,
    "notas": "Alumno se recuperó y desea continuar"
  }'
```

---

## 🛠️ MÓDULO 7: MANTENIMIENTO

### 7.1 Diagnóstico
```bash
curl -X GET "${API_URL}/api/mantenimiento/diagnostico" \
  -b ./cookies.txt
```

### 7.2 Validar Integridad
```bash
curl -X GET "${API_URL}/api/mantenimiento/validar-integridad" \
  -b ./cookies.txt
```

### 7.3 Pendientes de Resolución
```bash
curl -X GET "${API_URL}/api/mantenimiento/pendientes-resolucion" \
  -b ./cookies.txt
```

### 7.4 Sincronizar Tutores
```bash
curl -X POST "${API_URL}/api/mantenimiento/sincronizar-tutores" \
  -H "Content-Type: application/json" \
  -b ./cookies.txt
```

### 7.5 Resolver Masivamente
```bash
curl -X POST "${API_URL}/api/mantenimiento/resolver-masivamente" \
  -H "Content-Type: application/json" \
  -b ./cookies.txt \
  -d '{
    "motivo": "EGRESADO",
    "estado_target": "INACTIVO",
    "semestreId": 1
  }'
```

### 7.6 Liberar Cupos
```bash
curl -X POST "${API_URL}/api/mantenimiento/liberar-cupos-seguro" \
  -H "Content-Type: application/json" \
  -b ./cookies.txt
```

---

## 📊 MÓDULO 8: DASHBOARD

### 8.1 Estadísticas Generales
```bash
curl -X GET "${API_URL}/api/dashboard/estadisticas" \
  -b ./cookies.txt
```

### 8.2 Distribución por Tutor
```bash
curl -X GET "${API_URL}/api/dashboard/distribucion-tutores" \
  -b ./cookies.txt
```

### 8.3 Procesos Recientes
```bash
curl -X GET "${API_URL}/api/dashboard/procesos-recientes" \
  -b ./cookies.txt
```

### 8.4 Semestre Activo
```bash
curl -X GET "${API_URL}/api/dashboard/semestre-activo" \
  -b ./cookies.txt
```

### 8.5 Health Check
```bash
curl -X GET "${API_URL}/api/dashboard/health"
```

---

## 📈 MÓDULO 9: REPORTES

### 9.1 Reporte por Carrera
```bash
curl -X GET "${API_URL}/api/reportes/por-carrera?semestreAcademico=2024-04&carrera=ISC" \
  -b ./cookies.txt
```

### 9.2 Exportar Alumnos de Tutor
```bash
# Excel
curl -X GET "${API_URL}/api/reportes/tutores/5/alumnos/exportar?formato=EXCEL&periodo=2024-04" \
  -b ./cookies.txt \
  -o tutor_alumnos.xlsx

# PDF
curl -X GET "${API_URL}/api/reportes/tutores/5/alumnos/exportar?formato=PDF&periodo=2024-04" \
  -b ./cookies.txt \
  -o tutor_alumnos.pdf
```

### 9.3 Exportar Carrera
```bash
curl -X GET "${API_URL}/api/reportes/carreras/ISC/exportar?formato=EXCEL&periodo=2024-04" \
  -b ./cookies.txt \
  -o carrera_ISC.xlsx
```

### 9.4 Exportar Todos (ZIP)
```bash
curl -X GET "${API_URL}/api/reportes/carreras/exportar-todos?periodo=2024-04" \
  -b ./cookies.txt \
  -o todas_carreras.zip
```

---

## 🧹 MÓDULO 10: LIMPIEZA (ADMIN)

### 10.1 Detectar Corrupción
```bash
curl -X GET "${API_URL}/api/cleanup/detectar/3" \
  -b ./cookies.txt
```

### 10.2 Detectar Global
```bash
curl -X GET "${API_URL}/api/cleanup/detectar-global" \
  -b ./cookies.txt
```

### 10.3 Health Check
```bash
curl -X GET "${API_URL}/api/cleanup/health"
```

### 10.4 Limpiar Semestre
```bash
curl -X POST "${API_URL}/api/cleanup/limpiar/3" \
  -H "Content-Type: application/json" \
  -b ./cookies.txt \
  -d '{
    "confirmarLimpieza": true,
    "estrategia": "REASIGNAR_INTELLIGENTE"
  }'
```

### 10.5 Limpiar Global
```bash
curl -X POST "${API_URL}/api/cleanup/limpiar-global" \
  -H "Content-Type: application/json" \
  -b ./cookies.txt \
  -d '{
    "confirmarLimpieza": true,
    "estrategia": "REASIGNAR_INTELLIGENTE"
  }'
```

### 10.6 Recalcular Cargas
```bash
curl -X POST "${API_URL}/api/cleanup/recalcular" \
  -H "Content-Type: application/json" \
  -b ./cookies.txt
```

---

## 📚 UTILIDADES DE CURL

### Guardar/Usar Cookies
```bash
# Guardar cookies después del login
curl -X POST "${API_URL}/auth/login" \
  -H "Content-Type: application/json" \
  -c ./cookies.txt \
  -d '{"username": "coord", "password": "pass"}'

# Usar en siguientes requests
curl -X GET "${API_URL}/api/alumnos" \
  -b ./cookies.txt
```

### Guardar Respuesta en Archivo
```bash
curl -X GET "${API_URL}/api/alumnos" \
  -b ./cookies.txt \
  -o respuesta.json
```

### Ver Headers Completos
```bash
curl -X GET "${API_URL}/api/alumnos" \
  -b ./cookies.txt \
  -v
```

### Usar Variables de Entorno
```bash
# Guardar en variable
RESPONSE=$(curl -s -X GET "${API_URL}/api/alumnos" \
  -b ./cookies.txt)

# Usar jq para parsear
echo $RESPONSE | jq '.data.content[0]'
```

### Pretty Print JSON
```bash
curl -X GET "${API_URL}/api/alumnos" \
  -b ./cookies.txt | jq '.'
```

### Medir Tiempo de Respuesta
```bash
curl -X GET "${API_URL}/api/alumnos" \
  -b ./cookies.txt \
  -w "Tiempo: %{time_total}s\n"
```

### Enviar Headers Personalizados
```bash
curl -X GET "${API_URL}/api/alumnos" \
  -H "Authorization: Bearer TOKEN" \
  -H "Custom-Header: value" \
  -b ./cookies.txt
```

---

## 🔄 SCRIPTING AVANZADO

### Script: Crear Alumno y Obtener ID
```bash
#!/bin/bash

# Login
curl -X POST "http://localhost:8080/auth/login" \
  -H "Content-Type: application/json" \
  -c ./cookies.txt \
  -d '{"username": "coordinador", "password": "password123"}' > /dev/null

# Crear alumno
RESPONSE=$(curl -s -X POST "http://localhost:8080/api/alumnos" \
  -H "Content-Type: application/json" \
  -b ./cookies.txt \
  -d '{
    "matricula": "202401999",
    "nombre": "Test Alumno",
    "carrera": "ISC",
    "semestre": 1,
    "estado": "ACTIVO",
    "semestreId": 2
  }')

# Extraer ID
ALUMNO_ID=$(echo $RESPONSE | jq '.data.id')
echo "Alumno creado con ID: $ALUMNO_ID"

# Obtener alumno
curl -s -X GET "http://localhost:8080/api/alumnos/$ALUMNO_ID" \
  -b ./cookies.txt | jq '.'
```

### Script: Procesar Lotes
```bash
#!/bin/bash

# Leer archivo CSV y crear alumnos
while IFS=',' read -r matricula nombre carrera semestre
do
  curl -X POST "http://localhost:8080/api/alumnos" \
    -H "Content-Type: application/json" \
    -b ./cookies.txt \
    -d "{
      \"matricula\": \"$matricula\",
      \"nombre\": \"$nombre\",
      \"carrera\": \"$carrera\",
      \"semestre\": $semestre,
      \"estado\": \"ACTIVO\",
      \"semestreId\": 2
    }"

  sleep 1  # Esperar 1 segundo entre requests
done < alumnos.csv
```

---

## ✅ TESTING CHECKLIST

```bash
# 1. Probar autenticación
curl -X POST "http://localhost:8080/auth/login" \
  -H "Content-Type: application/json" \
  -c ./cookies.txt \
  -d '{"username": "coordinador", "password": "password123"}'

# 2. Probar endpoint simple
curl -X GET "http://localhost:8080/api/semestres/activo" \
  -b ./cookies.txt

# 3. Probar CRUD
# CREATE
curl -X POST "http://localhost:8080/api/alumnos" -H "Content-Type: application/json" -b ./cookies.txt -d '{...}'

# READ
curl -X GET "http://localhost:8080/api/alumnos/1" -b ./cookies.txt

# UPDATE
curl -X PUT "http://localhost:8080/api/alumnos/1" -H "Content-Type: application/json" -b ./cookies.txt -d '{...}'

# DELETE
curl -X DELETE "http://localhost:8080/api/alumnos/1" -b ./cookies.txt

# 4. Probar paginación
curl -X GET "http://localhost:8080/api/alumnos?page=1&limit=10" -b ./cookies.txt

# 5. Probar filtros
curl -X GET "http://localhost:8080/api/alumnos?estado=ACTIVO&carrera=ISC" -b ./cookies.txt

# 6. Probar búsqueda
curl -X GET "http://localhost:8080/api/alumnos/autocomplete?q=car" -b ./cookies.txt

# 7. Probar file upload
curl -X POST "http://localhost:8080/api/asignaciones/validar-excel" \
  -b ./cookies.txt \
  -F "archivo=@./test.xlsx" \
  -F "semestreId=2"

# 8. Probar error handling (credenciales malas)
curl -X POST "http://localhost:8080/auth/login" \
  -H "Content-Type: application/json" \
  -d '{"username": "bad", "password": "wrong"}'

# 9. Probar 404
curl -X GET "http://localhost:8080/api/alumnos/999999" -b ./cookies.txt

# 10. Probar health
curl -X GET "http://localhost:8080/api/dashboard/health"
```

---

**Última Actualización:** 20 de Noviembre de 2024
**Versión:** 1.0
**Estado:** ✅ LISTO

