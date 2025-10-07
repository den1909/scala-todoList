import java.time.LocalDate

object Main {
  def main(args: Array[String]): Unit = {
    TaskManager.initialize()
    UIComponents.clearScreen()
    UIComponents.animateText("Loading Todo List Manager", 100)
    println()
    Thread.sleep(500)
    val config = DataPersistence.loadConfig()
    val taskCount = TaskManager.getAllTasks.length
    println(UIComponents.drawNotification(s"Loaded $taskCount task(s) from storage", "info"))
    println(UIComponents.drawNotification(s"Theme: ${UITheme.getCurrentTheme.name}", "info"))
    if (config.autoSave) {
      println(UIComponents.drawNotification("Auto-save is enabled", "info"))
    }
    println()
    Thread.sleep(1000)
    Runtime.getRuntime.addShutdownHook(new Thread(new Runnable {
      def run(): Unit = {
        TaskManager.saveTasks()
      }
    }))
    Menu.handleUserInput()
  }
}
