# =========================
# 1️⃣ Build stage
# =========================
FROM maven:3.9.9-eclipse-temurin-21 AS build

WORKDIR /build

# Copy pom.xml first (better caching)
COPY pom.xml .
RUN mvn dependency:go-offline

# Copy source
COPY src ./src

# Build JAR
RUN mvn clean package -DskipTests


# =========================
# 2️⃣ Runtime stage
# =========================
FROM eclipse-temurin:21-jre

WORKDIR /app

# Copy only the built jar
COPY --from=build /build/target/*.jar app.jar

EXPOSE 5173

ENTRYPOINT ["java","-jar","app.jar"]
