# Developing the rdf-toolkit

## Development

The primary development IDE used for this project is [IntelliJ IDEA](http://www.jetbrains.com/idea/).


## Building

This project is being build and packaged on the EDM Council Jenkins Server by [this job](https://jenkins.edmcouncil.org/job/rdf-toolkit-build/).

The current Jenkins build status is: 
[![Build Status](https://jenkins.edmcouncil.org/buildStatus/icon?job=rdf-toolkit-build)](https://jenkins.edmcouncil.org/job/rdf-toolkit-build/)


## Testing

You can run the rdf-toolkit without first packaging it as a jar (see "package" below) by launching it via Maven:

```
mvn exec:java --help
```

All the unit tests can be executed by this command:

```
mvn test
```

## Package

### Normal packaging

Normal packaging as a jar is done with the following command:

```
mvn package
```

This creates a jar file like:

```
./target/original-rdf-toolkit-<version>.jar
```

### Packaging as "uber jar"

The RDF Toolkit is packaged as one "fat jar" or "uber jar" which can be downloaded 
from the EDM Council Jenkins server:

- https://jenkins.edmcouncil.org/job/rdf-toolkit-build/lastSuccessfulBuild/artifact/target/scala-2.11/rdf-toolkit.jar

You can create this uber jar from the command line yourself as well:

```
mvn package
```

The created jar can be found here:

```
./target/rdf-toolkit-<version>.jar
```