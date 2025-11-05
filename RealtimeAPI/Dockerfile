# Multi-stage build pour optimiser l'image finale
FROM maven:3.9.8-eclipse-temurin-21 AS build

# Copier les fichiers de configuration Maven
COPY pom.xml /app/
WORKDIR /app

# Télécharger les dépendances (cache layer)
RUN mvn dependency:go-offline -B

# Copier le code source
COPY src /app/src

# Builder l'application
RUN mvn clean package -DskipTests

# Image finale plus légère
FROM eclipse-temurin:21-jre

# Créer un utilisateur non-root pour la sécurité
RUN groupadd -r spring && useradd -r -g spring spring

# Copier le JAR depuis l'étape de build
COPY --from=build /app/target/*.jar /app/app.jar

# Changer les permissions
RUN chown spring:spring /app/app.jar

# Utiliser l'utilisateur non-root
USER spring

# Port de l'application
EXPOSE 8080

# Point d'entrée
ENTRYPOINT ["java", "-jar", "/app/app.jar"]