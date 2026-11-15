# ---------- Etapa 1: build (compila o projeto com Maven) ----------
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

# Baixa as dependências primeiro (fica em cache entre builds)
COPY pom.xml .
RUN mvn -B -q dependency:go-offline

# Copia o código e gera o .jar
COPY src ./src
RUN mvn -B -q clean package -DskipTests

# ---------- Etapa 2: execução (imagem leve, só com o Java) ----------
FROM eclipse-temurin:21-jre
WORKDIR /app

# Fuso de São Paulo para as regras de horário (RN03, RN05)
ENV TZ=America/Sao_Paulo

COPY --from=build /app/target/fila-zero-api.jar app.jar

EXPOSE 8080

# DB_URL, DB_USER e DB_PASSWORD vêm das variáveis de ambiente do Render
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75", "-Duser.timezone=America/Sao_Paulo", "-Doracle.jdbc.timezoneAsRegion=false", "-jar", "app.jar"]
