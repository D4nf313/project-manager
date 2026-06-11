FROM maven:3.9-eclipse-temurin-21 AS backend-build
WORKDIR /app/backend
COPY project-manager-backend/pom.xml .
COPY project-manager-backend/src ./src
RUN mvn clean package -DskipTests

FROM node:18-alpine AS frontend-build
WORKDIR /app/frontend
COPY project-manager-frontend/package*.json ./
RUN npm install
COPY project-manager-frontend/ .
RUN npm run build --prod

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=backend-build /app/backend/target/*.jar app.jar
COPY --from=frontend-build /app/frontend/dist/ /app/static/
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]