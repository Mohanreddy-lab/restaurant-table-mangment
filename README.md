# Restaurant Table Management

A small project for keeping track of restaurant tables. You can add tables, seat customers, mark tables for cleaning, and check how many tables are available.

There is a Java command-line version and a browser version backed by a simple SQLite database.

## What it can do

- Add tables with a number and seating capacity
- Assign customers to an available table
- Mark a table as needing cleaning when customers leave
- Make a table available again after cleaning
- Change the capacity of an available table
- View table details and basic restaurant statistics

## Run the Java version

You’ll need a JDK installed. Open a terminal in this folder and run:

```bash
javac main.java
java main
```

Follow the numbered menu to manage tables.

## Run the browser version

Compile and start the Java server:

```bash
javac Backend.java
java -cp ".;sqlite-jdbc.jar;slf4j-api.jar" Backend
```

Open http://localhost:8080 in a browser. The browser version stores tables in `tables.db`.
