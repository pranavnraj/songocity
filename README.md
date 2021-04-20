# songbirds

## Install Packages
Clone repo and set up Maven first following this instruction: [Maven in 5 minutes](https://maven.apache.org/guides/getting-started/maven-in-five-minutes.html). If on Mac, `brew install maven` should work as well. Then run `mvn clean install` at the same root as pom.xml to compile and produce project build/JAR files. If the build fails, delete the folder `node_modules` in songbirds-frontend and try `mvn clean install` again.

## Launch App
Run `java -Dspring-boot.run.jvmArguments -Djdk.tls.client.protocols=TLSv1.2 -jar target/app-0.0.1-SNAPSHOT.jar` at the same root as pom.xml. If successful, go to localhost:5000 to view the website.


