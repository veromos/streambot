# Streambot

This project is an server api for streamers, it allows creation and gestion users, tips, giveaways and surveys.

### Prerequisites

- IntelliJ
- Scala
- SBT


### Running the server

First run the class HelloSlick to populate the database

```
sbt runMain PopulateDatabase
```

And then launch the web server

```
sbt runMain services.HttpService
```

Now you can start requesting the api at http://localhost:8080


### Running the tests

In coming
