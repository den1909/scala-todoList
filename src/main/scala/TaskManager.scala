import java.time.LocalDate
import java.time.format.DateTimeFormatter
import scala.util.{Try, Success, Failure}
import scala.annotation.tailrec

object TaskManager {
  private var tasks = List[Task]()
  private var nextId = 1

  // Initialize by loading saved tasks
  def initialize(): Unit = {
    val savedTasks = DataPersistence.loadTasks()
    tasks = savedTasks
    nextId = if (savedTasks.nonEmpty) savedTasks.map(_.id).max + 1 else 1

    // Load and apply saved theme
    val config = DataPersistence.loadConfig()
    val theme = UITheme.getAvailableThemes.find(_.name == config.theme).getOrElse(UITheme.DarkTheme)
    UITheme.setTheme(theme)
  }

  def addTask(title: String, category: String, priority: String = "medium", deadline: Option[String] = None, description: Option[String] = None): Unit = {
    val validCategories = Set("work", "school", "private")
    if (!validCategories.contains(category.toLowerCase)) {
      println(UIComponents.drawNotification("Invalid category. Use: work, school, private", "error"))
      return
    }

    val validPriorities = Set("low", "medium", "high", "urgent")
    if (!validPriorities.contains(priority.toLowerCase)) {
      println(UIComponents.drawNotification("Invalid priority. Use: low, medium, high, urgent", "error"))
      return
    }

    val parsedDeadline = deadline.flatMap { dateStr =>
      Try {
        LocalDate.parse(dateStr, DateTimeFormatter.ofPattern("dd.MM.yyyy"))
      }.toOption
    }

    if (deadline.isDefined && parsedDeadline.isEmpty) {
      println(UIComponents.drawNotification("Invalid date format. Use DD.MM.YYYY", "error"))
      return
    }

    val task = Task(
      id = nextId,
      title = title,
      category = category.toLowerCase,
      status = "open",
      priority = priority.toLowerCase,
      deadline = parsedDeadline,
      description = description.filter(_.trim.nonEmpty),
      createdAt = LocalDate.now()
    )
    tasks = tasks :+ task
    nextId += 1

    // Auto-save if enabled
    DataPersistence.autoSave(tasks)

    println(UIComponents.drawNotification(s"Task created successfully! ID: ${task.id}", "success"))
    println()
    println(UITheme.dimText("Task preview:"))
    println(UIComponents.drawTaskCard(task))
  }

  def showAllTasks(): Unit = {
    if (tasks.isEmpty) {
      println(UIComponents.drawNotification("No tasks found. Add some tasks to get started!", "info"))
      return
    }

    println(UITheme.border("╔" + "═" * 68 + "╗"))
    println(UITheme.border("║") + UIComponents.drawTitle("📋 ALL TASKS", 66) + UITheme.border("║"))
    println(UITheme.border("╚" + "═" * 68 + "╝"))
    println()

    val sortedTasks = tasks.sortBy(task => (-task.priorityWeight, task.deadline.getOrElse(LocalDate.MAX)))
    val totalTasks = tasks.length
    val completedTasks = tasks.count(_.status == "finished")

    // Progress bar
    println(UITheme.info("Progress: ") + UITheme.progressBar(completedTasks, totalTasks))
    println()

    displayTasks(sortedTasks.toList)
  }

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

  @tailrec
  private def findTaskById(tasks: List[Task], targetId: Int): Option[Task] = tasks match {
    case Nil => None
    case task :: tail =>
      if (task.id == targetId) Some(task)
      else findTaskById(tail, targetId)
  }


  def showTasksByCategory(category: String): Unit = {
    val filteredTasks = tasks.filter(_.category == category.toLowerCase)

    if (filteredTasks.isEmpty) {
      println(UIComponents.drawNotification(s"No tasks found in category: $category", "info"))
      return
    }

    println(UITheme.border("╔" + "═" * 68 + "╗"))
    println(UITheme.border("║") + UIComponents.drawTitle(s"📁 ${category.toUpperCase} TASKS", 66) + UITheme.border("║"))
    println(UITheme.border("╚" + "═" * 68 + "╝"))
    println()

    val sortedTasks = filteredTasks.sortBy(task => (-task.priorityWeight, task.deadline.getOrElse(LocalDate.MAX)))

    displayTasks(sortedTasks.toList)
  }

  def changeTaskStatus(id: Int, newStatus: String): Unit = {
    val validStatuses = Set("open", "in-work", "finished")
    if (!validStatuses.contains(newStatus.toLowerCase)) {
      println(UIComponents.drawNotification("Invalid status. Use: open, in-work, finished", "error"))
      return
    }

    findTaskById(tasks, id) match {
      case Some(task) =>
        val updatedTask = task.copy(status = newStatus.toLowerCase)
        tasks = tasks.map(t => if (t.id == id) updatedTask else t)

        // Auto-save if enabled
        DataPersistence.autoSave(tasks)

        println(UIComponents.drawNotification(s"Status updated to ${UIComponents.drawStatusBadge(newStatus)}!", "success"))
        println()
        println(UITheme.dimText("Updated task:"))
        println(UIComponents.drawTaskCard(updatedTask))
      case None =>
        println(UIComponents.drawNotification(s"Task with ID $id not found.", "error"))
    }
  }

  def getTaskById(id: Int): Option[Task] = {
    findTaskById(tasks, id)
  }

  def getAllTasks: List[Task] = tasks

  def saveTasks(): Boolean = {
    DataPersistence.saveTasks(tasks)
  }

  def exportTasks(format: String): Boolean = {
    DataPersistence.exportTasks(tasks, format)
  }

  def deleteTask(id: Int): Unit = {
    findTaskById(tasks, id) match {
      case Some(task) =>
        tasks = tasks.filterNot(_.id == id)

        // Auto-save if enabled
        DataPersistence.autoSave(tasks)

        println(UIComponents.drawNotification(s"Task #$id deleted successfully!", "success"))
      case None =>
        println(UIComponents.drawNotification(s"Task with ID $id not found.", "error"))
    }
  }

  def searchTasks(query: String): List[Task] = {
    val lowerQuery = query.toLowerCase
    val searchResults = searchWithRelevanceScore(tasks, lowerQuery)
    searchResults.sortBy(-_._2).map(_._1)  // Sort by relevance score descending
  }

  private def searchWithRelevanceScore(tasks: List[Task], query: String): List[(Task, Int)] = tasks match {
    case Nil => List.empty
    case task :: tail =>
      val score = calculateRelevanceScore(task, query, 0)
      val restResults = searchWithRelevanceScore(tail, query)
      if (score > 0) (task, score) :: restResults
      else restResults
  }

  private def calculateRelevanceScore(task: Task, query: String, baseScore: Int): Int = {
    val titleScore = if (task.title.toLowerCase.contains(query)) 10 else 0
    val exactTitleScore = if (task.title.toLowerCase == query) 20 else 0
    val descScore = task.description.map(desc =>
      if (desc.toLowerCase.contains(query)) 5 else 0
    ).getOrElse(0)
    val categoryScore = if (task.category.toLowerCase.contains(query)) 3 else 0
    val statusScore = if (task.status.toLowerCase.contains(query)) 2 else 0
    val priorityScore = if (task.priority.toLowerCase.contains(query)) 2 else 0

    // Recursive bonus calculation based on query word count
    val wordBonus = calculateWordMatchBonus(query.split("\\s+").toList, task)

    baseScore + titleScore + exactTitleScore + descScore + categoryScore + statusScore + priorityScore + wordBonus
  }

  private def calculateWordMatchBonus(words: List[String], task: Task): Int = words match {
    case Nil => 0
    case word :: tail =>
      val wordScore = if (task.title.toLowerCase.contains(word) ||
                         task.description.exists(_.toLowerCase.contains(word))) 1 else 0
      wordScore + calculateWordMatchBonus(tail, task)
  }

  def showTasksByPriority(priority: String): Unit = {
    val filteredTasks = tasks.filter(_.priority == priority.toLowerCase)

    if (filteredTasks.isEmpty) {
      println(UIComponents.drawNotification(s"No tasks found with priority: $priority", "info"))
      return
    }

    println(UITheme.border("╔" + "═" * 68 + "╗"))
    println(UITheme.border("║") + UIComponents.drawTitle(s"⚡ ${priority.toUpperCase} PRIORITY TASKS", 66) + UITheme.border("║"))
    println(UITheme.border("╚" + "═" * 68 + "╝"))
    println()

    displayTasks(filteredTasks.toList)
  }

  def showOverdueTasks(): Unit = {
    val overdueTasks = tasks.filter(_.isOverdue)

    if (overdueTasks.isEmpty) {
      println(UIComponents.drawNotification("No overdue tasks! Great job staying on track!", "success"))
      return
    }

    println(UITheme.border("╔" + "═" * 68 + "╗"))
    println(UITheme.border("║") + UIComponents.drawTitle("⚠️ OVERDUE TASKS", 66) + UITheme.border("║"))
    println(UITheme.border("╚" + "═" * 68 + "╝"))
    println()

    println(UITheme.warning(s"⚠️  You have ${overdueTasks.length} overdue task(s). Consider prioritizing these!"))
    println()

    val sortedTasks = overdueTasks.sortBy(task => (-task.priorityWeight, task.deadline.get))

    displayTasks(sortedTasks.toList)
  }

  def updateTaskDeadline(id: Int, newDeadline: String): Unit = {
    val parsedDeadline = Try {
      LocalDate.parse(newDeadline, DateTimeFormatter.ofPattern("dd.MM.yyyy"))
    }

    parsedDeadline match {
      case Success(date) =>
        findTaskById(tasks, id) match {
          case Some(task) =>
            val updatedTask = task.copy(deadline = Some(date))
            tasks = tasks.map(t => if (t.id == id) updatedTask else t)

            // Auto-save if enabled
            DataPersistence.autoSave(tasks)

            println(UIComponents.drawNotification(s"Deadline updated to ${date.format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))}!", "success"))
          case None =>
            println(UIComponents.drawNotification(s"Task with ID $id not found.", "error"))
        }
      case Failure(_) =>
        println(UIComponents.drawNotification("Invalid date format. Use DD.MM.YYYY", "error"))
    }
  }

  def updateTaskPriority(id: Int, newPriority: String): Unit = {
    val validPriorities = Set("low", "medium", "high", "urgent")
    if (!validPriorities.contains(newPriority.toLowerCase)) {
      println(UIComponents.drawNotification("Invalid priority. Use: low, medium, high, urgent", "error"))
      return
    }

    findTaskById(tasks, id) match {
      case Some(task) =>
        val updatedTask = task.copy(priority = newPriority.toLowerCase)
        tasks = tasks.map(t => if (t.id == id) updatedTask else t)

        // Auto-save if enabled
        DataPersistence.autoSave(tasks)

        println(UIComponents.drawNotification(s"Priority updated to ${UIComponents.drawPriorityBadge(newPriority)}!", "success"))
      case None =>
        println(UIComponents.drawNotification(s"Task with ID $id not found.", "error"))
    }
  }

  def updateTaskDescription(id: Int, newDescription: String): Unit = {
    findTaskById(tasks, id) match {
      case Some(task) =>
        val desc = if (newDescription.trim.isEmpty) None else Some(newDescription.trim)
        val updatedTask = task.copy(description = desc)
        tasks = tasks.map(t => if (t.id == id) updatedTask else t)

        // Auto-save if enabled
        DataPersistence.autoSave(tasks)

        val message = if (desc.isDefined) "Description updated successfully!" else "Description removed successfully!"
        println(UIComponents.drawNotification(message, "success"))
      case None =>
        println(UIComponents.drawNotification(s"Task with ID $id not found.", "error"))
    }
  }

  def showTaskDetails(id: Int): Unit = {
    findTaskById(tasks, id) match {
      case Some(task) =>
        println(UITheme.border("╔" + "═" * 68 + "╗"))
        println(UITheme.border("║") + UIComponents.drawTitle(s"🔍 TASK #${task.id} DETAILS", 66) + UITheme.border("║"))
        println(UITheme.border("╚" + "═" * 68 + "╝"))
        println()
        println(UIComponents.drawTaskCard(task))

        if (task.isOverdue) {
          println()
          println(UITheme.error("⚠️  This task is OVERDUE! Consider updating the deadline or marking as completed."))
        }
      case None =>
        println(UIComponents.drawNotification(s"Task with ID $id not found.", "error"))
    }
  }
}