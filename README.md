# songocity

## Intro
Hello, 

This is the source code for Songocity, a Spotify music recommendation app that I wrote as a personal project. Songocity allows users to befriend other people who may have similar music tastes and uses our own machine learning model to create playlists curated to you based on your new friends' tastes. The playlists are automatically created in your own Spotify account, where you can immediately start listening to the recommended songs. You may visit the site at **songocity.com** (if still up).

## Walkthrough Demo of Site

Here is an ordered set of pictures showing a walkthrough of the site:

#### Main Site Page
![Screen Shot 2021-06-13 at 12 58 43 AM](https://user-images.githubusercontent.com/35354762/121799805-fc3f8200-cbe2-11eb-8c7e-da0cf4a74968.png)

#### Profile Page
![Screen Shot 2021-06-13 at 12 58 31 AM](https://user-images.githubusercontent.com/35354762/121799815-0eb9bb80-cbe3-11eb-8712-e4d60d708437.png)

#### View, Add, and Remove friends on the Friends Page
![Screen Shot 2021-06-13 at 12 57 56 AM](https://user-images.githubusercontent.com/35354762/121799806-fea1dc00-cbe2-11eb-81f8-0eaf7771cac2.png)

#### Create playlists based on the friends you specify on the Recommender Page
![Screen Shot 2021-06-13 at 12 58 04 AM](https://user-images.githubusercontent.com/35354762/121799811-09f50780-cbe3-11eb-8a21-ecf4eaa130f0.png)

#### View the created playlists on the New Playlists page
![Screen Shot 2021-06-13 at 1 01 02 AM](https://user-images.githubusercontent.com/35354762/121799832-2002c800-cbe3-11eb-8bce-8bdbd2ac30d7.png)

#### Listen to the created playlists on the Spotify App
<img width="919" alt="Screen Shot 2021-06-13 at 1 03 52 AM" src="https://user-images.githubusercontent.com/35354762/121799846-3e68c380-cbe3-11eb-8258-5fcb88f7d99d.png">

## Design and Tech Stack
I used the SpringBoot Java Web Framework to host the main site itself and a WSGI Python server to handle the Machine Learning training and recommendation. For the frontend, I relied on ReactJS and used a MongoDB as the main database. In addition to these main components, I also used AWS S3 to store the machine learning models for each person and song data. Finally, I used AWS to host the site live once it was deployed. AWS Elastic Beanstalk was perfect to host the Java and Python web servers, which seamlessly hooked up with Route53 for the site domain: songocity.com. 

Here is a simple diagram to show the basic architecture:

![SpotifyWebApp (3)](https://user-images.githubusercontent.com/35354762/121799467-2728d680-cbe1-11eb-882f-bef599b8e72d.png)

## Conclusion
I hope you enjoy the site! This project was fun to create. If you have any questions, please reach me at pranavnraj@gmail.com. Thanks!






## Running the site locally(Only for Developers)
### Install Packages
Clone repo and set up Maven first following this instruction: [Maven in 5 minutes](https://maven.apache.org/guides/getting-started/maven-in-five-minutes.html). If on Mac, `brew install maven` should work as well. Then run `mvn clean install` at the same root as pom.xml to compile and produce project build/JAR files. If the build fails, delete the folder `node_modules` in songbirds-frontend and try `mvn clean install` again.

### Launch App
Run `java -Dspring-boot.run.jvmArguments -Djdk.tls.client.protocols=TLSv1.2 -jar target/app-0.0.1-SNAPSHOT.jar` at the same root as pom.xml. If successful, go to localhost:5000 to view the website.


