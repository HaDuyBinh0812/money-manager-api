# Stage 1: Build app bằng Maven
FROM eclipse-temurin:21-jdk AS builder
WORKDIR /app

# copy toàn bộ source vào container
COPY . .

# chạy mvn package để build file jar (skip test cho nhanh)
RUN ./mvnw clean package -DskipTests

# Stage 2: Run app
FROM eclipse-temurin:21-jre
WORKDIR /app

# copy file jar đã build từ stage 1 sang
COPY --from=builder /app/target/moneymanager-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 9090
ENTRYPOINT ["java", "-jar", "app.jar"]