# ── Etapa 1: Build con Node ───────────────────────────────────
FROM node:22-alpine AS builder

WORKDIR /app

# Copiar package.json primero para cachear node_modules
COPY package*.json ./
RUN npm install 

# Copiar el código fuente y compilar
COPY . .
RUN npm run build

# ── Etapa 2: Servir con Nginx ─────────────────────────────────
FROM nginx:alpine

# Copiar el build de Angular al directorio de Nginx
COPY --from=builder /app/dist/project-manager-frontend/browser /usr/share/nginx/html

# Configuración de Nginx para Angular (manejo de rutas SPA)
COPY nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]
