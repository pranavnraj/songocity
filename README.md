# songbirds

## Install Packages
Clone repo and set up Maven first following this instruction: [Maven in 5 minutes](https://maven.apache.org/guides/getting-started/maven-in-five-minutes.html). If on Mac, `brew install maven` should work as well. Then run `mvn clean install` in this repo to compile and produce project build/JAR files.

## Launch React Website Locally
If you don't have npm yet, follow [this instruction](https://www.npmjs.com/get-npm) to install npm first. After npm is installed, go to `src/main/songbirds-frontend`. To install necessary packages, run `npm install`. To launch the website, run `npm start`(once this is done, you can leave this permanently running in terminal because updates to the frontend compile and build automatically)

## Launch Backend Locally
Run `./mvnw spring-boot:run`. If successful, go to localhost:3000 to view the website.
If you need access to cloud MongoDB(which you probably will), run `mvn spring-boot:run -Dspring-boot.run.jvmArguments="-Djdk.tls.client.protocols=TLSv1.2"` instead.


