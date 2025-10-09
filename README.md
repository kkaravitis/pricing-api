
export PRICING_DB_PASSWORD=mypass
mvn flyway:migrate
mvn generate-sources

docker compose up -d
set JAVA_HOME=C:\RedHat\java-21-openjdk-21.0.7.0.6-1
export JAVA_HOME=/c/RedHat/java-21-openjdk-21.0.7.0.6-1
export DB_URL=jdbc:postgresql://localhost:5432/pricing_db
export DB_USERNAME=pricing_user
export DB_PASSWORD=pricing_pass
mvn flyway:migrate
mvn jooq-codegen:generate -Ddb.username=pricing_user -Ddb.password=pricing_pass