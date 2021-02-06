FROM openjdk:11
VOLUME /tmp
COPY run.sh .
COPY target/*.jar app.jar
ENTRYPOINT ["./run.sh"]
