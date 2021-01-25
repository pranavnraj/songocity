# songbirds

## Install Packages
Set up Maven first following this instruction: [Maven in 5 minutes](https://maven.apache.org/guides/getting-started/maven-in-five-minutes.html). Then run `mvn clean install` in this repo to install relevant packages.

## Launch Authorization Flow Website Locally
Run `./mvnw spring-boot:run`. If successful, go to localhost:8888 to view the website.
If you need access to cloud MongoDB, run `mvn spring-boot:run -Dspring-boot.run.jvmArguments="-Djdk.tls.client.protocols=TLSv1.2"`

## Launch React Website Locally
If you don't have npm yet, follow [this instruction](https://www.npmjs.com/get-npm) to install npm first. After npm is installed, go to `src/main/songbirds-frontend`. To install necessary packages, run `npm install`. To launch the website, run `npm start`.
