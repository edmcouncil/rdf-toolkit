# Developing the rdf-toolkit

## develop

The primary development IDE used for this project is [IntelliJ IDEA (http://www.jetbrains.com/idea/).
Also install their [Scala plugin](https://confluence.jetbrains.com/display/SCA/Scala+Plugin+for+IntelliJ+IDEA).

## build

This project is being build and packaged on the EDM Council Jenkins Server by [this job](https://jenkins.edmcouncil.org/job/rdf-toolkit-build/).

The current Jenkins build status is: 
[![Build Status](https://jenkins.edmcouncil.org/buildStatus/icon?job=rdf-toolkit-build)](https://jenkins.edmcouncil.org/job/rdf-toolkit-build/)

## test

You can run the rdf-toolkit without first packaging it as a jar (see "package" below) by launching it via sbt:

```
sbt "run --help"
```

All the unit tests can be executed by this command:

```
sbt test
```

## package

### normal packaging

Normal packaging as a jar is done with the following command:

```
sbt package
```

This creates a jar file like:

```
./target/scala-2.11/rdf-toolkit_2.11-<version>.jar
```

### packaging as "uber jar"

The RDF Toolkit is packaged as one "fat jar" or "uber jar" which can be downloaded 
from the EDM Council Jenkins server:

- https://jenkins.edmcouncil.org/job/rdf-toolkit-build/lastSuccessfulBuild/artifact/target/scala-2.11/rdf-toolkit.jar

You can create this uber jar from the command line yourself as well:

```
sbt assembly
```
