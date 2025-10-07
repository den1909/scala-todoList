# Todo List Manager - Scala

Eine interaktive Kommandozeilen-Todo-Anwendung geschrieben in Scala 3.

Dennis Bacher & Dion Thaqi

## Features

- 📋 Task-Management mit Kategorien (work, school, private)
- 🏆 Prioritätsstufen (low, medium, high, urgent)
- 📅 Deadline-Verwaltung mit Überfälligkeits-Erkennung
- 🔍 Intelligente Suche mit Relevanz-Scoring
- 🎨 4 verschiedene UI-Themes (Dark, Light, Neon, Ocean)
- 💾 Automatisches Speichern und Backup-System
- 📊 Task-Analytics und Fortschritts-Tracking

## Rekursive Funktionen

Das Projekt demonstriert verschiedene rekursive Implementierungen:

### Tail-rekursive Funktionen (`@tailrec`)

**1. Task-Anzeige** (TaskManager.scala:90-99)

```scala
@tailrec
private def displayTasks(tasks: List[Task]): Unit = tasks match {
  case Nil => () // Base case
  case task :: Nil =>
    println(UIComponents.drawTaskCard(task))
  case task :: tail =>
    println(UIComponents.drawTaskCard(task))
    println(UITheme.dimText("─" * 50))
    displayTasks(tail)
}
```

**2. Task-Suche nach ID** (TaskManager.scala:101-107)

```scala
@tailrec
private def findTaskById(tasks: List[Task], targetId: Int): Option[Task] = tasks match {
  case Nil => None
  case task :: tail =>
    if (task.id == targetId) Some(task)
    else findTaskById(tail, targetId)
}
```

**3. Hauptmenü-Schleife** (SimpleInteractiveMenu.scala:12-84)

```scala
@tailrec
private def menuLoop(items: List[MenuItem]): Unit = {
  // Menü anzeigen
  UIComponents.drawWelcomeScreen()
  // User Input verarbeiten
  val input = StdIn.readLine()
  input match {
    case "q" => () // Exit
    case choice =>
      executeAction(choice)
      menuLoop(items) // Rekursiver Aufruf
  }
}
```

### Echte rekursive Funktionen

**4. Intelligente Suche mit Relevanz-Scoring** (TaskManager.scala:186-193)

```scala
private def searchWithRelevanceScore(tasks: List[Task], query: String): List[(Task, Int)] = tasks match {
  case Nil => List.empty
  case task :: tail =>
    val score = calculateRelevanceScore(task, query, 0)
    val restResults = searchWithRelevanceScore(tail, query) // Rekursion
    if (score > 0) (task, score) :: restResults
    else restResults
}
```

**5. Multi-Word-Suche mit Bonus-Punkten** (TaskManager.scala:211-217)

```scala
private def calculateWordMatchBonus(words: List[String], task: Task): Int = words match {
  case Nil => 0
  case word :: tail =>
    val wordScore = if (task.title.toLowerCase.contains(word) ||
                       task.description.exists(_.toLowerCase.contains(word))) 1 else 0
    wordScore + calculateWordMatchBonus(tail, task) // Rekursion
}
```

## Dateistruktur

### Core-Komponenten

- **Main.scala** - Einstiegspunkt der Applikation

  - Initialisiert TaskManager und UI
  - Lädt gespeicherte Tasks und Konfiguration
  - Registriert Shutdown-Hook für Auto-Save

- **TaskManager.scala** - Zentrale Task-Verwaltung

  - CRUD-Operationen für Tasks
  - Rekursive Such- und Filter-Funktionen
  - Intelligente Suche mit Relevanz-Scoring
  - Sortierung nach Priorität und Deadline

- **Task.scala** - Task-Datenmodell
  - Case Class mit Feldern: id, title, category, status, priority, deadline, description
  - Helper-Methoden für Prioritäts-Gewichtung und Überfälligkeits-Check

### UI-Komponenten

- **Menu.scala** - Menü-Handler

  - Handler-Funktionen für alle Menüoptionen
  - User-Input-Validierung
  - Interaktive Dialoge für Task-Operationen

- **SimpleInteractiveMenu.scala** - Interaktives Menüsystem

  - Tail-rekursive Menü-Schleife
  - Keyboard-Shortcuts (t, s, a, q, h)
  - Quick-Access-Funktionen für häufige Operationen
  - Analytics-Dashboard

- **UIComponents.scala** - UI-Rendering

  - Task-Karten mit Farb-Coding
  - Badges für Status, Priorität, Kategorie
  - Fortschrittsbalken und Notifications
  - ASCII-Art Welcome Screen

- **UITheme.scala** - Theme-System
  - 4 vordefinierte Themes (Dark, Light, Neon, Ocean)
  - ANSI-Farbcodes für Terminal
  - Dynamischer Theme-Wechsel

### Datenpersistenz

- **DataPersistence.scala** - Speicher-Management
  - JSON-basierte Task-Speicherung
  - Konfigurationsverwaltung
  - Automatisches Backup-System
  - Export-Funktionen

## Schnellstart

```bash
# Kompilieren
sbt compile

# Ausführen
sbt run
```

## Bedienung

- **Zahlen 1-12**: Menüoptionen auswählen
- **t**: Schneller Theme-Wechsel
- **s**: Schnellsuche
- **a**: Schnell Task hinzufügen
- **q**: Beenden

## Datenspeicherung

- Tasks: `tasks.json`
- Konfiguration: `config.json`
- Backups: `backups/` Ordner

## Systemanforderungen

- Scala 3.3.3
- sbt 1.11.6
- Java 17+
