import scala.io.StdIn

object Menu {

  private def clearScreen(): Unit = {
    UIComponents.clearScreen()
  }

  private def waitForEnter(): Unit = {
    print(UITheme.dimText("\n Press Enter to continue..."))
    StdIn.readLine()
  }


  def handleUserInput(): Unit = {
    val menuItems = SimpleInteractiveMenu.createMainMenuItems()
    SimpleInteractiveMenu.displayInteractiveMenu(menuItems)
  }


  def handleShowByCategory(): Unit = {
    println(UITheme.border("╔" + "═" * 68 + "╗"))
    println(UITheme.border("║") + UIComponents.drawTitle("📁 SHOW BY CATEGORY", 66) + UITheme.border("║"))
    println(UITheme.border("╚" + "═" * 68 + "╝"))
    println()
    println(UITheme.info("📂 Available categories:"))
    println("  " + UIComponents.drawCategoryBadge("work"))
    println("  " + UIComponents.drawCategoryBadge("school"))
    println("  " + UIComponents.drawCategoryBadge("private"))
    println()
    print(UITheme.accent("🎯 Enter category: "))
    val category = StdIn.readLine()
    println()
    TaskManager.showTasksByCategory(category)
  }

  def handleAddTask(): Unit = {
    println(UITheme.border("╔" + "═" * 68 + "╗"))
    println(UITheme.border("║") + UIComponents.drawTitle("➕ ADD NEW TASK", 66) + UITheme.border("║"))
    println(UITheme.border("╚" + "═" * 68 + "╝"))
    println()

    print(UITheme.accent("📝 Title: "))
    val title = StdIn.readLine()

    if (title.trim.isEmpty) {
      println(UIComponents.drawNotification("Title cannot be empty!", "error"))
      return
    }

    print(UITheme.accent("📁 Category (work/school/private): "))
    val category = StdIn.readLine()

    print(UITheme.accent("🏆 Priority (low/medium/high/urgent) ") + UITheme.dimText("[default: medium]: "))
    val priorityInput = StdIn.readLine()
    val priority = if (priorityInput.trim.isEmpty) "medium" else priorityInput

    print(UITheme.accent("📅 Deadline (DD.MM.YYYY) ") + UITheme.dimText("[optional]: "))
    val deadlineInput = StdIn.readLine()
    val deadline = if (deadlineInput.trim.isEmpty) None else Some(deadlineInput)

    print(UITheme.accent("📝 Description ") + UITheme.dimText("[optional]: "))
    val descriptionInput = StdIn.readLine()
    val description = if (descriptionInput.trim.isEmpty) None else Some(descriptionInput)

    println()
    TaskManager.addTask(title, category, priority, deadline, description)
  }

  def handleShowByPriority(): Unit = {
    println(UITheme.border("╔" + "═" * 68 + "╗"))
    println(UITheme.border("║") + UIComponents.drawTitle("⚡ SHOW BY PRIORITY", 66) + UITheme.border("║"))
    println(UITheme.border("╚" + "═" * 68 + "╝"))
    println()
    println(UITheme.info("🏆 Available priorities:"))
    println("  " + UIComponents.drawPriorityBadge("low"))
    println("  " + UIComponents.drawPriorityBadge("medium"))
    println("  " + UIComponents.drawPriorityBadge("high"))
    println("  " + UIComponents.drawPriorityBadge("urgent"))
    println()
    print(UITheme.accent("🎯 Enter priority: "))
    val priority = StdIn.readLine()
    println()
    TaskManager.showTasksByPriority(priority)
  }

  def handleChangeStatus(): Unit = {
    println("╔══════════════════════════════════════════════════════════════╗")
    println("║                    🔄 CHANGE TASK STATUS                     ║")
    println("╚══════════════════════════════════════════════════════════════╝")
    println()
    TaskManager.showAllTasks()

    if (TaskManager.getAllTasks.nonEmpty) {
      println("\n📋 Select a task to update:")
      print("🎯 Task ID: ")

      try {
        val id = StdIn.readLine().toInt
        print("🔄 New status (open/in-work/finished): ")
        val status = StdIn.readLine()

        println()
        TaskManager.changeTaskStatus(id, status)
      } catch {
        case _: NumberFormatException => println("❌ Invalid ID. Please enter a number.")
      }
    }
  }

  def handleUpdateDeadline(): Unit = {
    println("╔══════════════════════════════════════════════════════════════╗")
    println("║                   📅 UPDATE TASK DEADLINE                    ║")
    println("╚══════════════════════════════════════════════════════════════╝")
    println()
    TaskManager.showAllTasks()

    if (TaskManager.getAllTasks.nonEmpty) {
      println("\n📋 Select a task to update:")
      print("🎯 Task ID: ")

      try {
        val id = StdIn.readLine().toInt
        print("📅 New deadline (DD.MM.YYYY): ")
        val deadline = StdIn.readLine()

        println()
        TaskManager.updateTaskDeadline(id, deadline)
      } catch {
        case _: NumberFormatException => println("❌ Invalid ID. Please enter a number.")
      }
    }
  }

  def handleUpdatePriority(): Unit = {
    println("╔══════════════════════════════════════════════════════════════╗")
    println("║                   🏆 UPDATE TASK PRIORITY                    ║")
    println("╚══════════════════════════════════════════════════════════════╝")
    println()
    TaskManager.showAllTasks()

    if (TaskManager.getAllTasks.nonEmpty) {
      println("\n📋 Select a task to update:")
      print("🎯 Task ID: ")

      try {
        val id = StdIn.readLine().toInt
        print("🏆 New priority (🟢low/🟡medium/🟠high/🔴urgent): ")
        val priority = StdIn.readLine()

        println()
        TaskManager.updateTaskPriority(id, priority)
      } catch {
        case _: NumberFormatException => println("❌ Invalid ID. Please enter a number.")
      }
    }
  }

  def handleUpdateDescription(): Unit = {
    println("╔══════════════════════════════════════════════════════════════╗")
    println("║                 📝 UPDATE TASK DESCRIPTION                   ║")
    println("╚══════════════════════════════════════════════════════════════╝")
    println()
    TaskManager.showAllTasks()

    if (TaskManager.getAllTasks.nonEmpty) {
      println("\n📋 Select a task to update:")
      print("🎯 Task ID: ")

      try {
        val id = StdIn.readLine().toInt
        print("📝 New description: ")
        val description = StdIn.readLine()

        println()
        TaskManager.updateTaskDescription(id, description)
      } catch {
        case _: NumberFormatException => println("❌ Invalid ID. Please enter a number.")
      }
    }
  }

  def handleShowTaskDetails(): Unit = {
    println(UITheme.border("╔" + "═" * 68 + "╗"))
    println(UITheme.border("║") + UIComponents.drawTitle("🔍 SHOW TASK DETAILS", 66) + UITheme.border("║"))
    println(UITheme.border("╚" + "═" * 68 + "╝"))
    println()
    TaskManager.showAllTasks()

    if (TaskManager.getAllTasks.nonEmpty) {
      println()
      println(UITheme.info("📋 Select a task to view details:"))
      print(UITheme.accent("🎯 Task ID: "))

      try {
        val id = StdIn.readLine().toInt
        println()
        TaskManager.showTaskDetails(id)
      } catch {
        case _: NumberFormatException => println(UIComponents.drawNotification("Invalid ID. Please enter a number.", "error"))
      }
    }
  }

  def handleThemeSelection(): Unit = {
    UIComponents.drawThemeSelector()
    print(UITheme.accent("🎯 Choose theme (1-4): "))

    try {
      val choice = StdIn.readLine().toInt
      val themes = UITheme.getAvailableThemes

      if (choice >= 1 && choice <= themes.length) {
        val selectedTheme = themes(choice - 1)
        UITheme.setTheme(selectedTheme)
        println(UIComponents.drawNotification(s"Theme changed to ${selectedTheme.name}!", "success"))
      } else {
        println(UIComponents.drawNotification("Invalid theme selection.", "error"))
      }
    } catch {
      case _: NumberFormatException => println(UIComponents.drawNotification("Invalid input. Please enter a number.", "error"))
    }
  }
}
