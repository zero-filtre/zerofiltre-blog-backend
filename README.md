# zerofiltre-blog-backend

The zerofiltre blog backend

## Local Setup

- Java 11+
- Maven 3+

## Install

Define env vars :  
SPRING_DATASOURCE_PASSWORD to the appropriate db password SPRING_MAIL_PASSWORD to the appropriate smtp server password

- Clone the project and cd into it:  
  `cd blog/`
- Build and run  
  `mvn clean package -DskipTests`

  `java -jar target\blog.jar`