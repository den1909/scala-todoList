import java.time.LocalDate

object Main {
  def main(args: Array[String]): Unit = {
    TaskManager.initialize()
    UIComponents.clearScreen()
    UIComponents.animateText("Loading Todo List Manager", 100)
    println()
    Thread.sleep(500)

    val config = DataPersistence.loadConfig()
    val tasks = TaskManager.getAllTasks
    println(UIComponents.drawNotification(s"Loaded ${tasks.length} task(s)", "info"))
    println(UIComponents.drawNotification(s"Theme: ${UITheme.getCurrentTheme.name}", "info"))

    // 🔔 einfache Erinnerung bei überfälligen Tasks
    val overdue = tasks.filter(t => t.deadline.exists(_.isBefore(LocalDate.now())))
    if (overdue.nonEmpty) {
      println(s"\n⚠️  ${overdue.size} Aufgabe(n) überfällig:")
      overdue.take(3).foreach(t => println(s"- ${t.title} (fällig: ${t.deadline.get})"))
      println()
    }

    if (config.autoSave)
      println(UIComponents.drawNotification("Auto-save is enabled", "info"))

    Thread.sleep(1000)
    Runtime.getRuntime.addShutdownHook(new Thread(() => TaskManager.saveTasks()))
    Menu.handleUserInput()
  }
}
