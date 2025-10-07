---
marp: true
theme: default
paginate: true
backgroundColor: #fff
---

<!-- _class: lead -->

# Todo List Manager

## Scala 3

**Dennis Bacher & Dion Thaqi**

---

## Projekt-Übersicht

Eine interaktive **Kommandozeilen-Todo-Anwendung** in Scala 3

### Kernfunktionen

- Task-Management mit Kategorien (work, school, private)
- Prioritätsstufen (low, medium, high, urgent)
- Deadline-Verwaltung mit Überfälligkeits-Erkennung
- Suche
- Auto-Save

---

## Hauptziel: Rekursive Programmierung

Das Projekt demonstriert **5 rekursive Funktionen**:

1. **Tail-rekursive** Task-Anzeige (`@tailrec`)
2. **Tail-rekursive** Task-Suche nach ID
3. **Tail-rekursive** Hauptmenü-Schleife
4. **Echte rekursive** Suche mit Relevanz-Scoring
5. **Echte rekursive** Multi-Word-Suche

---

## Code-Highlight: Intelligente Suche

**Suche mit Relevanz-Scoring** (TaskManager.scala:186-193)
Die Funktion geht durch alle Tasks, berechnet für jeden eine Punktzahl zum Suchbegriff, und gibt nur die Tasks mit Punktzahl grösser als 0 zurück.

```scala
private def searchWithRelevanceScore(tasks: List[Task], query: String):
    List[(Task, Int)] = tasks match {

  case Nil => List.empty

  case task :: tail =>
    val score = calculateRelevanceScore(task, query, 0)
    val restResults = searchWithRelevanceScore(tail, query)

    if (score > 0) (task, score) :: restResults
    else restResults
}
```

---

## Demo-Ablauf

```bash
sbt run
```

---

<!-- _class: lead -->

# Vielen Dank!

## Fragen?

**Dennis Bacher & Dion Thaqi**
