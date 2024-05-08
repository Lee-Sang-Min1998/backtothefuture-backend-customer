FROM openjdk:17
RUN mkdir -p /opt/bagtothefuture/config
COPY build/libs/store.jar /opt/bagtothefuture/store.jar
COPY ../../core/infra/src/main/resources/firebase-secret.json /firebase-secret.json
#COPY ../../core/infra/src/main/resources/firebase-secret.json /opt/bagtothefuture/firebase-secret.json
ENTRYPOINT ["java","-jar","/opt/bagtothefuture/store.jar"]