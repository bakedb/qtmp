FROM eclipse-temurin:21-jdk AS build
WORKDIR /src
COPY QTMPServer.java .
RUN javac -d /out QTMPServer.java

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /out/ /app/
COPY server.properties /app/server.properties

EXPOSE 7867

RUN useradd -r appuser && chown -R appuser /app
USER appuser

CMD ["java", "-cp", "/app", "QTMPServer"]
