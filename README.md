# Frenemy

## About The Project

Frenemy is a web application for a role-playing game (RPG) built using **Java 17** and **Spring Boot**. 
It offers a comprehensive set of RESTful APIs to handle core game mechanics. Players can create and manage their characters, select unique races and classes, engage in combat, accept and turn in quests, and manage character inventories.

## Built With

* [Java 17]
* [Spring Boot 3.5.6]
* [PostgreSQL]
* [Gradle]

## Features Implemented

The application currently supports the following core features through its REST API:

* **Authentication & Authorization** (`/api/auth`)
  * User registration and secure login.

* **Character System** (`/api/character`)
  * Character retrieval and detailed stats tracking.
  * Inventory management (viewing items, equipping, and unequipping gear).

* **Classes and Races** (`/api/classes`, `/api/races`)
  * View available character classes and races for character creation.

* **Quests** (`/api/quests`)
  * Retrieve available quests.
  * Track and update quest progression, and handle quest completion.

* **Combat System** (`/api/combat`)
  * Initialize combat encounters.
  * Fetch current combat status.
  * Process turns and combat actions.
