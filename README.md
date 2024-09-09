# The Zerofiltre Platform Backend

## Contribution Guide

I'm pleased you are thinking about helping us build a platform that will change people's lives through quality learning
materials.

Get ready by following these simple steps.

## Tech Requirements

- Java 11+
- Maven 3+
- Docker

## Fork and Start Coding

I advise you to watch this [video](https://youtu.be/GSTbARM5ni4?si=2iQV5-g7S-vF-4bP) to get a glimpse of the code
structure.
It is in French and, unfortunately, the sound quality is not at its best.

To contribute, you must fork the project, then clone your fork to start working.
Once you are done hitting the keyboard, submit a pull request targeting our main branch.

### Local Setup

- Launch a local mysql and redis services using Docker-compose :
  ```shell
      docker compose up
  ```
- Build and run the app, then check the startup went successfully:
    ```shell
    cd blog/
    mvn clean package -DskipTests
    java -jar target/blog.jar
    ```
- Go to http://localhost/swagger-ui.html to explore our API Swagger documentation.

If the documentation does not load properly, check for any errors in the console to make sure you did not miss any of
the previous steps.

### Contact Us

While a discussion channel is being set up, you can still open an issue or reach us here: info@zerofiltre.tech
