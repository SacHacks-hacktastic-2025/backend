# 빌드 단계
FROM openjdk:17-jdk-slim as builder

# 작업 디렉토리 설정
WORKDIR /app

# Gradle Wrapper와 프로젝트 관련 파일을 먼저 복사
COPY gradlew .
COPY gradle/ gradle/
COPY build.gradle .
COPY settings.gradle .

# Gradle 종속성 캐싱
RUN ./gradlew dependencies --no-daemon

# 애플리케이션 코드 복사 및 빌드
COPY . .
RUN ./gradlew bootJar

# 실행 단계
FROM openjdk:17-jdk-slim

# 작업 디렉토리 설정
WORKDIR /app

# 빌드된 JAR 파일 복사
COPY --from=builder /app/build/libs/*.jar app.jar

# 애플리케이션 실행 포트 노출
EXPOSE 8080

# 컨테이너 실행 시 JAR 파일 실행
CMD ["java", "-jar", "app.jar"]
