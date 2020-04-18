# Project-G API

## Web
http://www.project-g.xyz/ 

https://api.project-g.xyz/

## Development

### Requirements
* [Java 8 JDK](https://adoptopenjdk.net/)
* [Maven](https://maven.apache.org/)
* [IntelliJ IDEA](https://www.jetbrains.com/idea/)

### Checking out the project
```bash
git clone https://github.com/KaralabeZeller/Project-G-api.git
```

### Running unit and integration tests
```bash
mvn verify
```

### Running the application
```bash
mvn spring-boot:run
```

http://localhost:8080/

### Project structure

* [Backend API](https://github.com/KaralabeZeller/Project-G-api/tree/master/src/main/java/com/nter/projectg)
  * [Integration tests](https://github.com/KaralabeZeller/Project-G-api/tree/master/src/test/java/com/nter)
* [Frontend UI](https://github.com/KaralabeZeller/Project-G-api/tree/master/src/main/resources/static)
  * TODO Integration tests

## Deployment

### Deploying the application to Google Cloud
* [GitHub Actions](https://github.com/KaralabeZeller/Project-G-api/actions)
  * [CI](https://github.com/KaralabeZeller/Project-G-api/blob/master/.github/workflows/build.yaml)
  * [CI - Pull Request](https://github.com/KaralabeZeller/Project-G-api/blob/master/.github/workflows/pull-request.yaml)

![CI](https://github.com/KaralabeZeller/Project-G-api/workflows/CI/badge.svg)
![CI - Pull Request](https://github.com/KaralabeZeller/Project-G-api/workflows/CI%20-%20Pull%20Request/badge.svg)

## See also

### Other repositories
* [nTer](https://github.com/KaralabeZeller/nter)  
* [Project-G](https://github.com/KaralabeZeller/Project-G)  
* [Project-G-web](https://github.com/KaralabeZeller/Project-G-web)
