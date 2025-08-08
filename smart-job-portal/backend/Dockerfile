FROM eclipse-temurin:17-jdk-alpine as build
WORKDIR /workspace/app

# Copy maven executable and pom.xml from backend directory
COPY smart-job-portal/backend/mvnw .
COPY smart-job-portal/backend/.mvn .mvn
COPY smart-job-portal/backend/pom.xml .

# Make mvnw executable
RUN chmod +x ./mvnw

# Build all dependencies but exclude the actual application to leverage Docker cache
RUN ./mvnw dependency:go-offline -B

# Copy source code from backend directory
COPY smart-job-portal/backend/src src

# Package the application
RUN ./mvnw package -DskipTests
RUN mkdir -p target/dependency && (cd target/dependency; jar -xf ../*.jar)

# Runtime stage
FROM eclipse-temurin:17-jre-alpine
VOLUME /tmp
ARG DEPENDENCY=/workspace/app/target/dependency
COPY --from=build ${DEPENDENCY}/BOOT-INF/lib /app/lib
COPY --from=build ${DEPENDENCY}/META-INF /app/META-INF
COPY --from=build ${DEPENDENCY}/BOOT-INF/classes /app
ENTRYPOINT ["java","-cp","app:app/lib/*","com.soprasteria.smartjobportal.SmartJobPortalApplication"]
