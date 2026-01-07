# Deploy en Railway (Backend)

## Root Directory
- `/backend`

## Build
- Si usas Dockerfile (recomendado): Railway lo detecta automáticamente en `/backend/Dockerfile`.
  - El Dockerfile ya es multi-stage y ejecuta la app con `-Dserver.port=${PORT:-8080}`.
- Alternativa sin Docker: Nixpacks (Railway) usando Maven
  - Build Command: `./mvnw -DskipTests package`
  - Start Command: `java -jar target/ProyectoTutoriasBackend-0.0.1-SNAPSHOT.jar`

## Variables de Entorno (Raw Editor)
Pega el siguiente bloque y ajusta valores:

```
SPRING_PROFILES_ACTIVE=prod

# MySQL (JawsDB)
DB_URL=jdbc:mysql://HOST:3306/DBNAME?useSSL=true&requireSSL=true&serverTimezone=UTC&allowPublicKeyRetrieval=true
DB_USER=your_user
DB_PASSWORD=your_password

# Security
JWT_SECRET=change-me-long-random
APP_CRYPTO_KEY=base64-256bit-key

# CORS
CORS_ALLOWED_ORIGINS=https://your-frontend.vercel.app

# Cookies (opcional, dejar vacío para host-only)
COOKIE_DOMAIN=

# Google OAuth (opcional)
GOOGLE_CLIENT_ID=
GOOGLE_CLIENT_SECRET=

# OAuth redirects (opcional)
OAUTH2_SUCCESS_REDIRECT=https://your-frontend.vercel.app/
OAUTH2_FAILURE_REDIRECT=https://your-frontend.vercel.app/login?error=google
```

## Construir DB_URL desde URL tipo `mysql://user:pass@host:port/db`
Dada `mysql://user:pass@host:3306/dbname` (JawsDB):
- DB_URL: `jdbc:mysql://host:3306/dbname?useSSL=true&requireSSL=true&serverTimezone=UTC&allowPublicKeyRetrieval=true`
- DB_USER: `user`
- DB_PASSWORD: `pass`

## Checklist de Verificación
- Despliegue: servicio arranca sin errores de esquema
  - Por defecto: `spring.jpa.hibernate.ddl-auto=validate`
  - Si falla por discrepancias de esquema en el demo, puedes usar env: `SPRING_JPA_HIBERNATE_DDL_AUTO=update`
- Health básico: `GET /api/dashboard/health` debe responder `status=UP` (nota: requiere auth según tu SecurityConfig; si necesitas health público, habilítalo explícitamente)
- Login: `POST /auth/login` crea cookies HttpOnly
- Logout: `POST /auth/logout` borra cookies
- CORS: desde tu frontend (`CORS_ALLOWED_ORIGINS`) las requests con `withCredentials: true` funcionan

## Notas
- No commitees secretos. Usa `.env.*.example` como referencia.
- Railway inyecta `PORT`. El runtime ya lo respeta.
- Si usas OAuth, ajusta `OAUTH2_*` y `COOKIE_DOMAIN` según tu dominio.

