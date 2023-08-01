FROM openjdk:17-jdk-slim
EXPOSE 8080
ADD target/zilch-cards-1.0.0.jar zilch-cards.jar
COPY ${JAR_FILE} zilchcards.jar
ENTRYPOINT ["java", "-jar", "/zilch-cards.jar"]