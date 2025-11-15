# Ejemplos de uso de autenticación

## 1. Iniciar sesión

```bash
curl -X POST "http://localhost:8080/auth/login" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "coord_tutorias",
    "password": "123456"
  }'
```

## 2. Registrar un nuevo usuario (requiere token del coordinador)

```bash
curl -X POST "http://localhost:8080/auth/register" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <TOKEN_DE_COORDINADOR>" \
  -d '{
    "username": "secretario1",
    "password": "123456",
    "role": "ROLE_SECRETARIO_ACADEMICO"
  }'
```

## 3. Consultar endpoint protegido

```bash
curl -X GET "http://localhost:8080/api/alumnos" \
  -H "Authorization: Bearer <TOKEN_DE_ACCESO>"
```

## 4. Renovar token de acceso

```bash
curl -X POST "http://localhost:8080/auth/refresh" \
  -H "Content-Type: application/json" \
  -d '{
    "refresh_token": "<TOKEN_DE_REFRESH>"
  }'
```
