FROM openjdk:8

COPY build/libs/space.jar space.jar
COPY space-server.yaml space.yaml
COPY cert.p12 cert.p12
CMD [ "java", "-Xms128m", "-Xmx256m", "-Dfile.encoding=UTF-8", "-jar", "space.jar", "server", "space.yaml"]