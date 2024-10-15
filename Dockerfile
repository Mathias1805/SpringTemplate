FROM openjdk:22
ARG JAR_FILE=target/*.jar
COPY ./target/DswRalfCald-0.0.1-SNAPSHOT.jar app.jar
ENTRYPOINT ["java","-jar","/app.jar"]
ENV  DBURL=jdbc:postgresql://localhost:5432/template
ENV DBUSER=
ENV DBPASS=
