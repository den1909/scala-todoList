import java.io.{File, PrintWriter, FileWriter}
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import scala.io.Source
import scala.util.{Try, Success, Failure}
import scala.collection.mutable.ListBuffer

object DataPersistence {

  private val DATA_FILE = "tasks.json"
  private val CONFIG_FILE = "config.json"
  private val BACKUP_DIR = "backups"

  case class ConfigData(
    theme: String,
    autoSave: Boolean,
    backupEnabled: Boolean,
    maxBackups: Int
  )
  private def taskToJson(task: Task): String = {
    val deadlineStr = task.deadline.map(_.format(DateTimeFormatter.ofPattern("dd.MM.yyyy")))
    val descStr = task.description.getOrElse("")
    val createdStr = task.createdAt.format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))

    s"""{
    "id": ${task.id},
    "title": "${escapeJson(task.title)}",
    "category": "${task.category}",
    "status": "${task.status}",
    "priority": "${task.priority}",
    "deadline": ${deadlineStr.map(d => s""""$d"""").getOrElse("null")},
    "description": ${if (descStr.nonEmpty) s""""${escapeJson(descStr)}"""" else "null"},
    "createdAt": "$createdStr"
  }"""
  }

  private def escapeJson(str: String): String = {
    str.replace("\\", "\\\\")
       .replace("\"", "\\\"")
       .replace("\n", "\\n")
       .replace("\r", "\\r")
       .replace("\t", "\\t")
  }

  private def unescapeJson(str: String): String = {
    str.replace("\\\\", "\\")
       .replace("\\\"", "\"")
       .replace("\\n", "\n")
       .replace("\\r", "\r")
       .replace("\\t", "\t")
  }
  private def parseTaskFromJson(json: String): Option[Task] = {
    try {
      val lines = json.split("\n").map(_.trim)

      def extractValue(key: String): Option[String] = {
        lines.find(_.startsWith(s""""$key":""")).map { line =>
          val colonIndex = line.indexOf(':')
          val value = line.substring(colonIndex + 1).trim
          if (value.startsWith("\"") && value.endsWith("\",")) {
            value.substring(1, value.length - 2)
          } else if (value.startsWith("\"") && value.endsWith("\"")) {
            value.substring(1, value.length - 1)
          } else if (value == "null," || value == "null") {
            null
          } else {
            value.replaceAll(",$", "")
          }
        }.filter(_ != null)
      }

      val id = extractValue("id").map(_.toInt).getOrElse(return None)
      val title = extractValue("title").map(unescapeJson).getOrElse(return None)
      val category = extractValue("category").getOrElse(return None)
      val status = extractValue("status").getOrElse(return None)
      val priority = extractValue("priority").getOrElse(return None)
      val deadlineStr = extractValue("deadline")
      val descriptionStr = extractValue("description").map(unescapeJson)
      val createdAtStr = extractValue("createdAt").getOrElse(return None)

      val deadline = deadlineStr.flatMap { dateStr =>
        Try(LocalDate.parse(dateStr, DateTimeFormatter.ofPattern("dd.MM.yyyy"))).toOption
      }

      val createdAt = Try(LocalDate.parse(createdAtStr, DateTimeFormatter.ofPattern("dd.MM.yyyy")))
        .getOrElse(LocalDate.now())

      Some(Task(id, title, category, status, priority, deadline, descriptionStr, createdAt))
    } catch {
      case _: Exception => None
    }
  }

  def saveTasks(tasks: List[Task]): Boolean = {
    try {
      if (loadConfig().backupEnabled && new File(DATA_FILE).exists()) {
        createBackup()
      }

      val file = new PrintWriter(new FileWriter(DATA_FILE))
      file.println("{")
      file.println("  \"tasks\": [")

      tasks.zipWithIndex.foreach { case (task, index) =>
        file.print("    " + taskToJson(task))
        if (index < tasks.length - 1) file.println(",")
        else file.println()
      }

      file.println("  ],")
      file.println(s"""  "saved_at": "${LocalDate.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))}\"""")
      file.println("}")
      file.close()

      true
    } catch {
      case e: Exception =>
        println(UIComponents.drawNotification(s"Failed to save tasks: ${e.getMessage}", "error"))
        false
    }
  }

  def loadTasks(): List[Task] = {
    try {
      if (!new File(DATA_FILE).exists()) {
        return List.empty
      }

      val source = Source.fromFile(DATA_FILE)
      val content = source.mkString
      source.close()

      val tasksStart = content.indexOf("\"tasks\": [")
      val tasksEnd = content.indexOf("],", tasksStart)

      if (tasksStart == -1 || tasksEnd == -1) {
        return List.empty
      }

      val tasksSection = content.substring(tasksStart + "\"tasks\": [".length, tasksEnd)

      val taskObjects = ListBuffer[String]()
      var currentObject = new StringBuilder
      var braceCount = 0
      var inObject = false

      tasksSection.foreach { char =>
        if (char == '{') {
          if (!inObject) {
            inObject = true
            currentObject.clear()
          }
          braceCount += 1
          currentObject.append(char)
        } else if (char == '}') {
          braceCount -= 1
          currentObject.append(char)
          if (braceCount == 0 && inObject) {
            taskObjects += currentObject.toString()
            inObject = false
          }
        } else if (inObject) {
          currentObject.append(char)
        }
      }

      taskObjects.flatMap(parseTaskFromJson).toList
    } catch {
      case e: Exception =>
        println(UIComponents.drawNotification(s"Failed to load tasks: ${e.getMessage}", "error"))
        List.empty
    }
  }

  private def createBackup(): Unit = {
    try {
      val backupDir = new File(BACKUP_DIR)
      if (!backupDir.exists()) {
        backupDir.mkdirs()
      }

      val timestamp = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"))
      val backupFile = new File(backupDir, s"tasks_backup_$timestamp.json")

      val source = Source.fromFile(DATA_FILE)
      val content = source.mkString
      source.close()

      val writer = new PrintWriter(backupFile)
      writer.write(content)
      writer.close()
      cleanOldBackups()
    } catch {
      case e: Exception =>
        println(UIComponents.drawNotification(s"Failed to create backup: ${e.getMessage}", "warning"))
    }
  }

  private def cleanOldBackups(): Unit = {
    try {
      val config = loadConfig()
      val backupDir = new File(BACKUP_DIR)

      if (backupDir.exists()) {
        val backupFiles = backupDir.listFiles()
          .filter(_.getName.startsWith("tasks_backup_"))
          .sortBy(_.lastModified())
          .reverse

        if (backupFiles.length > config.maxBackups) {
          backupFiles.drop(config.maxBackups).foreach(_.delete())
        }
      }
    } catch {
      case _: Exception =>
    }
  }

  def saveConfig(config: ConfigData): Boolean = {
    try {
      val file = new PrintWriter(new FileWriter(CONFIG_FILE))
      file.println("{")
      file.println(s"""  "theme": "${config.theme}",""")
      file.println(s"""  "autoSave": ${config.autoSave},""")
      file.println(s"""  "backupEnabled": ${config.backupEnabled},""")
      file.println(s"""  "maxBackups": ${config.maxBackups}""")
      file.println("}")
      file.close()
      true
    } catch {
      case e: Exception =>
        println(UIComponents.drawNotification(s"Failed to save config: ${e.getMessage}", "error"))
        false
    }
  }

  def loadConfig(): ConfigData = {
    try {
      if (!new File(CONFIG_FILE).exists()) {
        return ConfigData("Dark", autoSave = true, backupEnabled = true, maxBackups = 5)
      }

      val source = Source.fromFile(CONFIG_FILE)
      val content = source.mkString
      source.close()

      def extractConfigValue(key: String): Option[String] = {
        val pattern = s""""$key":\\s*([^,}]+)""".r
        pattern.findFirstMatchIn(content).map(_.group(1).trim.replaceAll("^\"", "").replaceAll("\"$", ""))
      }

      val theme = extractConfigValue("theme").getOrElse("Dark")
      val autoSave = extractConfigValue("autoSave").exists(_.toBoolean)
      val backupEnabled = extractConfigValue("backupEnabled").exists(_.toBoolean)
      val maxBackups = extractConfigValue("maxBackups").map(_.toInt).getOrElse(5)

      ConfigData(theme, autoSave, backupEnabled, maxBackups)
    } catch {
      case e: Exception =>
        println(UIComponents.drawNotification(s"Failed to load config: ${e.getMessage}", "warning"))
        ConfigData("Dark", autoSave = true, backupEnabled = true, maxBackups = 5)
    }
  }

  def exportTasks(tasks: List[Task], format: String): Boolean = {
    format.toLowerCase match {
      case "json" => exportAsJson(tasks)
      case "csv" => exportAsCsv(tasks)
      case "md" | "markdown" => exportAsMarkdown(tasks)
      case _ =>
        println(UIComponents.drawNotification("Unsupported export format. Use: json, csv, markdown", "error"))
        false
    }
  }

  private def exportAsJson(tasks: List[Task]): Boolean = {
    val timestamp = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"))
    val filename = s"tasks_export_$timestamp.json"

    try {
      val file = new PrintWriter(new FileWriter(filename))
      file.println("{")
      file.println("  \"tasks\": [")

      tasks.zipWithIndex.foreach { case (task, index) =>
        file.print("    " + taskToJson(task))
        if (index < tasks.length - 1) file.println(",")
        else file.println()
      }

      file.println("  ],")
      file.println(s"""  "exported_at": "${LocalDate.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))}\"""")
      file.println("}")
      file.close()

      println(UIComponents.drawNotification(s"Tasks exported to $filename", "success"))
      true
    } catch {
      case e: Exception =>
        println(UIComponents.drawNotification(s"Export failed: ${e.getMessage}", "error"))
        false
    }
  }

  private def exportAsCsv(tasks: List[Task]): Boolean = {
    val timestamp = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"))
    val filename = s"tasks_export_$timestamp.csv"

    try {
      val file = new PrintWriter(new FileWriter(filename))
      file.println("ID,Title,Category,Status,Priority,Deadline,Description,Created")

      tasks.foreach { task =>
        val deadline = task.deadline.map(_.format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))).getOrElse("")
        val description = task.description.getOrElse("").replace(",", ";")
        val created = task.createdAt.format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))

        file.println(s"""${task.id},"${task.title}",${task.category},${task.status},${task.priority},"$deadline","$description","$created"""")
      }

      file.close()

      println(UIComponents.drawNotification(s"Tasks exported to $filename", "success"))
      true
    } catch {
      case e: Exception =>
        println(UIComponents.drawNotification(s"Export failed: ${e.getMessage}", "error"))
        false
    }
  }

  private def exportAsMarkdown(tasks: List[Task]): Boolean = {
    val timestamp = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"))
    val filename = s"tasks_export_$timestamp.md"

    try {
      val file = new PrintWriter(new FileWriter(filename))
      file.println("# Todo List Export")
      file.println()
      file.println(s"Exported on: ${LocalDate.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))}")
      file.println()

      // Group by category
      val tasksByCategory = tasks.groupBy(_.category)

      tasksByCategory.foreach { case (category, categoryTasks) =>
        file.println(s"## ${category.capitalize} Tasks")
        file.println()

        categoryTasks.foreach { task =>
          val statusIcon = task.status match {
            case "finished" => "✅"
            case "in-work" => "🔄"
            case _ => "⭕"
          }

          val priorityIcon = task.priority match {
            case "urgent" => "🔴"
            case "high" => "🟠"
            case "medium" => "🟡"
            case "low" => "🟢"
            case _ => "⚪"
          }

          file.println(s"### $statusIcon $priorityIcon ${task.title}")
          file.println()
          file.println(s"- **Status**: ${task.status}")
          file.println(s"- **Priority**: ${task.priority}")
          task.deadline.foreach(d => file.println(s"- **Deadline**: ${d.format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))}"))
          file.println(s"- **Created**: ${task.createdAt.format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))}")
          task.description.foreach(desc => {
            file.println()
            file.println(desc)
          })
          file.println()
        }
      }

      file.close()

      println(UIComponents.drawNotification(s"Tasks exported to $filename", "success"))
      true
    } catch {
      case e: Exception =>
        println(UIComponents.drawNotification(s"Export failed: ${e.getMessage}", "error"))
        false
    }
  }

  def autoSave(tasks: List[Task]): Unit = {
    val config = loadConfig()
    if (config.autoSave) {
      saveTasks(tasks)
    }
  }
}
