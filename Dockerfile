# ==========================================================
# Step 1: 빌드 단계 (Build Stage)
# ==========================================================
FROM gradle:8.4.0-jdk21 AS build

WORKDIR /home/gradle/project
COPY --chown=gradle:gradle build.gradle settings.gradle /home/gradle/project/

RUN gradle dependencies --scan
COPY --chown=gradle:gradle . /home/gradle/project

RUN gradle build -x test

ARG JAR_FILE=build/libs/deartime-0.0.1-SNAPSHOT.jar

# ==========================================================
# Step 2: 실행 이미지 단계 (Run Stage)
# ==========================================================
FROM amazoncorretto:21

ENV TZ=Asia/Seoul
WORKDIR /app
COPY --from=build /home/gradle/project/build/libs/deartime-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-Duser.timezone=Asia/Seoul", "-jar", "app.jar"]