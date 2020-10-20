# Sauron

Distributed Systems 2019-2020 project.  
Includes the two parts of the project (tags: SD_P1 and SD_P2).  
Project statement in [statement folder](./statement/)  
Project demonstration in [demo folder](./demo/)  
Project report in [report folder](./report/)

## Authors

**Group T19**

### Team members

| Number | Name              | User                                                     | Email                                                                                   |
| ------ | ----------------- | -------------------------------------------------------- | --------------------------------------------------------------------------------------- |
| 90699  | Afonso Matos      | [github.com/afonsomatos](https://github.com/afonsomatos) | [afonsolfmatos@tecnico.ulisboa.pt](mailto:afonsolfmatos@tecnico.ulisboa.pt)             |
| 90775  | Ricardo Fernandes | [github.com/rickerp](https://github.com/rickerp)         | [ricardo.s.fernandes@tecnico.ulisboa.pt](mailto:ricardo.s.fernandes@tecnico.ulisboa.pt) |
| 90741  | João Tomás Lopes  | [github.com/tomlopes](https://github.com/tomlopes)       | [joaotomaslopes@tecnico.ulisboa.pt](mailto:joaotomaslopes@tecnico.ulisboa.pt)           |

### Task leaders

| Task set | To-Do                         | Leader              |
| -------- | ----------------------------- | ------------------- |
| core     | protocol buffers, silo-client | _(whole team)_      |
| T1       | cam_join, cam_info, eye       | _Afonso Matos_      |
| T2       | report, spotter               | _João Tomás Lopes_  |
| T3       | track, trackMatch, trace      | _Ricardo Fernandes_ |
| T4       | test T1                       | _Ricardo Fernandes_ |
| T5       | test T2                       | _Afonso Matos_      |
| T6       | test T3                       | _João Tomás Lopes_  |

## Getting Started

The overall system is composed of multiple modules.
The main server is the _silo_.
The clients are the _eye_ and _spotter_.

See the [project statement](./statement/README.md) for a full description of the domain and the system.  
See the [demonstration markdown](./demo/README.md) to know how to run the project

### Prerequisites

Java Developer Kit 11 is required running on Linux, Windows or Mac.
Maven 3 is also required.

To confirm that you have them installed, open a terminal and type:

```
javac -version

mvn -version
```

### Installing

To compile and install all modules:

```
mvn clean install -DskipTests
```

The integration tests are skipped because they require the servers to be running.

## Built With

- [Maven](https://maven.apache.org/) - Build Tool and Dependency Management
- [gRPC](https://grpc.io/) - RPC framework

## Versioning

We use [SemVer](http://semver.org/) for versioning.
