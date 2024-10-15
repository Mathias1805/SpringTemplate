FROM openjdk:22
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} app.jar
ENTRYPOINT ["java","-jar","/app.jar"]
ENV  DBURL=jdbc:postgresql://localhost:5432/template
ENV DBUSER=guest
ENV DBPASS=guest
