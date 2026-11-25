# ==========================================
# ETAPA 1: COMPILAR CON MAVEN Y JAVA 21
# ==========================================

FROM maven:3.9-eclipse-temurin-21 AS build

WORKDIR /app

COPY pom.xml .
COPY src ./src

RUN mvn -DskipTests package

# ==========================================
# ETAPA 2: EJECUTAR CON JAVA 21
# ==========================================

FROM eclipse-temurin:21-jre

WORKDIR /app

COPY --from=build /app/target/*.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]
