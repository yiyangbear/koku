# Koku / 观子

A JavaFX Gomoku project built for coursework and software development practice.

## Overview

Koku is a desktop Gomoku game developed with Java and JavaFX.  
This project started as a course assignment and is being gradually improved into a more complete software project.

## Current Features

- Two-player Gomoku gameplay
- New match
- Undo
- Win detection
- Timer support
- Theme switching
- Language switching
- Coordinate display toggle
- Last move marker toggle
- Settings panel

## Tech Stack

- Java
- JavaFX
- Maven

## Project Structure

- `app` - application startup
- `config` - configuration and option models
- `domain` - core game data and rules
- `service` - game session and supporting services
- `ui` - user interface components
- `resources` - i18n and resource files

## Run

Use Maven to run the project:

```bash
mvn clean javafx:run