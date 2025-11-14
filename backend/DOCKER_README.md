# Docker Setup - Sistema de Gestión de Tutorías Backend

This guide explains how to run the backend using Docker and Docker Compose.

## Prerequisites

- **Docker**: Version 20.10+ ([Install Docker](https://docs.docker.com/get-docker/))
- **Docker Compose**: Version 2.0+ ([Install Docker Compose](https://docs.docker.com/compose/install/))

## Quick Start

### 1. Build and Run with Docker Compose

From the `backend/` directory, run:

```bash
docker-compose up -d
```

This command:
- Builds the Spring Boot backend Docker image
- Starts MySQL 8.0 container
- Starts the backend application
- Creates a bridge network connecting both services

### 2. Verify Services are Running

```bash
# Check running containers
docker-compose ps

# Expected output:
# NAME                        STATUS
# gestion-tutorias-mysql      Up (healthy)
# gestion-tutorias-backend    Up (healthy)
```

### 3. Access the Application

- **API**: http://localhost:8080
- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **API Docs**: http://localhost:8080/api-docs
- **Dashboard Health**: http://localhost:8080/api/dashboard/health

### 4. Test a Sample Request

```bash
curl http://localhost:8080/api/dashboard/health

# Expected response:
# {
#   "status": "success",
#   "data": {
#     "status": "UP",
#     "semestre_activo_existe": false,
#     "timestamp": "2025-11-14T10:30:00"
#   }
# }
```

## Docker Services

### MySQL Service

- **Container Name**: `gestion-tutorias-mysql`
- **Image**: `mysql:8.0`
- **Port**: 3306
- **Database**: `gestion_tutorias`
- **User**: `tutorias_user`
- **Password**: `tutorias_password`
- **Root Password**: `root`

### Backend Service

- **Container Name**: `gestion-tutorias-backend`
- **Port**: 8080
- **Depends On**: MySQL (waits for health check)
- **Auto-migration**: Flyway migrations run on startup

## Configuration

### Environment Variables

The `docker-compose.yml` file sets environment variables for the backend. To customize:

1. **Edit `docker-compose.yml`** directly, OR
2. **Use `.env.docker`** and modify values as needed

Key configurations:
```yaml
SPRING_DATASOURCE_URL: jdbc:mysql://mysql:3306/gestion_tutorias?useSSL=false&serverTimezone=UTC
SPRING_JPA_HIBERNATE_DDL_AUTO: update  # Options: validate, update, create, create-drop
SECURITY_JWT_SECRET: your-secret-key-change-this-in-production-environment
```

### Change MySQL Credentials

To change credentials, edit `docker-compose.yml`:

```yaml
mysql:
  environment:
    MYSQL_USER: your_new_user
    MYSQL_PASSWORD: your_new_password
    # Also update backend service:
    SPRING_DATASOURCE_USERNAME: your_new_user
    SPRING_DATASOURCE_PASSWORD: your_new_password
```

## Common Commands

### Start Services

```bash
# Start in foreground (see logs)
docker-compose up

# Start in background
docker-compose up -d

# Start with rebuild
docker-compose up -d --build
```

### Stop Services

```bash
# Stop containers (data persists)
docker-compose stop

# Stop and remove containers (data persists in volume)
docker-compose down

# Stop, remove containers AND delete data
docker-compose down -v
```

### View Logs

```bash
# All services
docker-compose logs -f

# Specific service
docker-compose logs -f backend
docker-compose logs -f mysql

# Last 100 lines
docker-compose logs --tail 100
```

### Access MySQL Shell

```bash
docker-compose exec mysql mysql -u tutorias_user -p gestion_tutorias
# Enter password: tutorias_password
```

### Rebuild Backend Only

```bash
docker-compose build --no-cache backend
docker-compose up -d backend
```

### Check Service Health

```bash
# Health of all services
docker-compose ps

# Detailed backend health
curl http://localhost:8080/api/dashboard/health

# Detailed MySQL health
docker-compose exec mysql mysqladmin ping -u tutorias_user -p
```

## Troubleshooting

### 1. Backend fails to connect to MySQL

**Error**: `Communications link failure`

**Solution**:
```bash
# Check if MySQL is running
docker-compose ps

# Check MySQL logs
docker-compose logs mysql

# Restart MySQL
docker-compose restart mysql

# Wait for health check and restart backend
docker-compose restart backend
```

### 2. Port already in use

**Error**: `Error response from daemon: Ports are not available`

**Solution**:
```bash
# Check what's using the port
lsof -i :8080
lsof -i :3306

# Either kill the process or change ports in docker-compose.yml
# Edit docker-compose.yml and change:
# ports:
#   - "8081:8080"  (for different external port)
```

### 3. Rebuild from scratch

```bash
# Complete cleanup
docker-compose down -v

# Remove dangling images
docker image prune -a

# Rebuild and start
docker-compose up -d --build
```

### 4. Check database migration status

```bash
docker-compose logs backend | grep -i flyway
```

### 5. Clear all Docker data and start fresh

```bash
# Warning: This removes all images, containers, and volumes
docker system prune -a --volumes

# Then rebuild
docker-compose up -d --build
```

## Performance Tips

1. **Use `.dockerignore`** to exclude unnecessary files from build context
2. **Multi-stage builds** reduce final image size (Dockerfile uses this)
3. **Volume mounts** allow live code changes during development (if configured)

## Production Considerations

Before deploying to production:

1. **Change JWT secret** in `docker-compose.yml` or `.env`
2. **Use secure MySQL passwords** (not `tutorias_password`)
3. **Change `DDL_AUTO` from `update` to `validate`**
4. **Use managed database service** instead of container (AWS RDS, Azure Database, etc.)
5. **Enable HTTPS** and change CORS origins
6. **Use container registry** (Docker Hub, ECR, etc.)
7. **Set resource limits** in docker-compose.yml:
   ```yaml
   backend:
     deploy:
       resources:
         limits:
           cpus: '2'
           memory: 2G
   ```

## Docker Image Details

### Build Image

```bash
# Build locally
docker build -t tutorias-backend:latest .

# Run locally built image
docker run -e SPRING_DATASOURCE_URL=jdbc:mysql://host.docker.internal:3306/gestion_tutorias \
           -e SPRING_DATASOURCE_USERNAME=tutorias_user \
           -e SPRING_DATASOURCE_PASSWORD=tutorias_password \
           -p 8080:8080 \
           tutorias-backend:latest
```

### Image Size

The multi-stage build creates a minimal image:
- **Stage 1 (Builder)**: maven:3.9-eclipse-temurin-21 (~500MB, discarded)
- **Stage 2 (Runtime)**: eclipse-temurin:21-jre (~200MB) + JAR (~50MB)
- **Final Image Size**: ~250MB

## Volumes

Data persistence:
```yaml
mysql_data:
  driver: local
```

This volume stores MySQL data. When you `docker-compose down`, data is preserved. Use `docker-compose down -v` to delete data.

## Networks

Services communicate via Docker bridge network `tutorias-network`:
- Backend can reach MySQL at hostname `mysql` (not localhost)
- Both services expose ports on localhost for external access

---

**Created**: November 14, 2025
**Status**: Ready for development and testing
