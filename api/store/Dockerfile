FROM openjdk:17
RUN mkdir -p /opt/bagtothefuture/config
COPY build/libs/store.jar /opt/bagtothefuture/store.jar
ENTRYPOINT ["java","-jar","/opt/bagtothefuture/store.jar"]