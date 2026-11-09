# Restaurant Table Management

A small project for keeping track of restaurant tables. You can add tables, seat customers, mark tables for cleaning, and check how many tables are available.

There are two ways to try it: a Java command-line version and a simple browser version.

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

Open `index.html` in a web browser. You can add and manage tables directly on the page; no server or extra packages are needed.

## A note about saving data

Tables are kept in memory, so they’re cleared when you close the Java program or refresh the browser page. This project doesn’t currently save table information between sessions.
