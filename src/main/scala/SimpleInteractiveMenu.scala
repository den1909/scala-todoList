import scala.io.StdIn
import scala.annotation.tailrec

object SimpleInteractiveMenu {

  case class MenuItem(key: String, icon: String, text: String, action: () => Unit)

  def displayInteractiveMenu(items: List[MenuItem]): Unit = {
    menuLoop(items)
  }

  @tailrec
  private def menuLoop(items: List[MenuItem]): Unit = {
    UIComponents.clearScreen()
    UIComponents.drawWelcomeScreen()
    println(UITheme.border("╔" + "═" * 68 + "╗"))
    println(UITheme.border("║") + UIComponents.drawTitle("🎮 INTERACTIVE MENU", 66) + UITheme.border("║"))
    println(UITheme.border("╠" + "═" * 68 + "╣"))

    drawMenuItems(items)

    println(UITheme.border("╚" + "═" * 68 + "╝"))
    println()
    val currentTheme = UITheme.getCurrentTheme
    println(UITheme.dimText(s"Current theme: ${currentTheme.name}"))
    println()
    println(UITheme.accent("🎮 Interactive Controls:"))
    println(UITheme.dimText("• Enter number + Enter to select"))
    println(UITheme.dimText("• Type 't' for quick theme change"))
    println(UITheme.dimText("• Type 's' for quick search"))
    println(UITheme.dimText("• Type 'a' for quick add task"))
    println(UITheme.dimText("• Type 'q' to quit"))
    println()

    print(UITheme.accent("🎯 Your choice: "))
    val inputRaw = StdIn.readLine()
    if (inputRaw == null) {
      UIComponents.clearScreen()
      println(UIComponents.drawNotification("Goodbye!", "info"))
      return // Exit recursion
    }

    val input = inputRaw.toLowerCase.trim

    input match {
      case "q" | "quit" | "exit" =>
        UIComponents.clearScreen()
        println(UIComponents.drawNotification("Thanks for using Todo List Manager!", "success"))
        println(UIComponents.drawNotification("Goodbye!", "info"))
        () // Exit recursion

      case "t" | "theme" =>
        handleQuickThemeChange()
        menuLoop(items)

      case "s" | "search" =>
        handleQuickSearch()
        menuLoop(items)

      case "a" | "add" =>
        UIComponents.clearScreen()
        Menu.handleAddTask()
        waitForEnter()
        menuLoop(items)

      case "h" | "help" =>
        showHelpScreen()
        menuLoop(items)

      case choice =>
        findMenuItemByKey(items, choice) match {
          case Some(item) =>
            UIComponents.clearScreen()
            item.action()
            waitForEnter()
            menuLoop(items)
          case None =>
            UIComponents.clearScreen()
            println(UIComponents.drawNotification(s"Unknown command: '$choice'. Type 'h' for help.", "error"))
            waitForEnter()
            menuLoop(items)
        }
    }
  }

  @tailrec
  private def drawMenuItems(items: List[MenuItem]): Unit = items match {
    case Nil => () // Base case
    case item :: tail =>
      val menuLine = s"  ${item.key}. ${item.icon} ${item.text}"
      val padding = " " * (66 - menuLine.length)
      println(UITheme.border("║ ") + UITheme.text(menuLine) + padding + UITheme.border("║"))
      drawMenuItems(tail) // Recursive call
  }

  @tailrec
  private def findMenuItemByKey(items: List[MenuItem], targetKey: String): Option[MenuItem] = items match {
    case Nil => None
    case item :: tail =>
      if (item.key == targetKey) Some(item)
      else findMenuItemByKey(tail, targetKey)
  }

  private def handleQuickThemeChange(): Unit = {
    UIComponents.clearScreen()
    val themes = UITheme.getAvailableThemes

    println(UITheme.border("╔" + "═" * 68 + "╗"))
    println(UITheme.border("║") + UIComponents.drawTitle("🎨 QUICK THEME SELECTOR", 66) + UITheme.border("║"))
    println(UITheme.border("╚" + "═" * 68 + "╝"))
    println()

    displayThemes(themes, 0)

    println()
    print(UITheme.accent("🎨 Choose theme (1-4) or Enter to cancel: "))

    val choiceRaw = StdIn.readLine()
    if (choiceRaw == null) return
    val choice = choiceRaw.trim
    if (choice.nonEmpty && choice.matches("[1-4]")) {
      val themeIndex = choice.toInt - 1
      if (themeIndex >= 0 && themeIndex < themes.length) {
        UITheme.setTheme(themes(themeIndex))

        // Save theme preference
        val config = DataPersistence.loadConfig()
        val newConfig = config.copy(theme = themes(themeIndex).name)
        DataPersistence.saveConfig(newConfig)

        println(UIComponents.drawNotification(s"Theme changed to ${themes(themeIndex).name}!", "success"))
        Thread.sleep(1000)
      }
    }
  }

  private def handleQuickSearch(): Unit = {
    UIComponents.clearScreen()
    println(UITheme.border("╔" + "═" * 68 + "╗"))
    println(UITheme.border("║") + UIComponents.drawTitle("🔍 QUICK SEARCH", 66) + UITheme.border("║"))
    println(UITheme.border("╚" + "═" * 68 + "╝"))
    println()

    print(UITheme.accent("🔍 Search tasks (or Enter to cancel): "))
    val queryRaw = StdIn.readLine()
    if (queryRaw == null) return
    val query = queryRaw.trim

    if (query.nonEmpty) {
      val matchingTasks = TaskManager.searchTasks(query)

      if (matchingTasks.nonEmpty) {
        println()
        println(UITheme.success(s"Found ${matchingTasks.length} matching task(s):"))
        println()

        displayTasks(matchingTasks)
      } else {
        println(UIComponents.drawNotification(s"No tasks found matching '$query'", "info"))
      }
    }

    waitForEnter()
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

  private def showHelpScreen(): Unit = {
    UIComponents.clearScreen()
    println(UITheme.border("╔" + "═" * 68 + "╗"))
    println(UITheme.border("║") + UIComponents.drawTitle("📖 HELP & SHORTCUTS", 66) + UITheme.border("║"))
    println(UITheme.border("╚" + "═" * 68 + "╝"))
    println()

    println(UITheme.accent("🎮 Interactive Mode Commands:"))
    println(UITheme.text("  1-12    - Select menu option by number"))
    println(UITheme.text("  t       - Quick theme change"))
    println(UITheme.text("  s       - Quick search"))
    println(UITheme.text("  a       - Quick add task"))
    println(UITheme.text("  h       - Show this help"))
    println(UITheme.text("  q       - Quit application"))
    println()

    println(UITheme.accent("📋 Menu Options:"))
    println(UITheme.text("  1  - Show all tasks"))
    println(UITheme.text("  2  - Show tasks by category"))
    println(UITheme.text("  3  - Show tasks by priority"))
    println(UITheme.text("  4  - Show overdue tasks"))
    println(UITheme.text("  5  - Add new task"))
    println(UITheme.text("  6  - Change task status"))
    println(UITheme.text("  7  - Update task deadline"))
    println(UITheme.text("  8  - Update task priority"))
    println(UITheme.text("  9  - Update task description"))
    println(UITheme.text("  10 - Show task details"))
    println(UITheme.text("  11 - Change theme"))
    println(UITheme.text("  12 - Exit"))
    println()

    println(UITheme.accent("💡 Tips:"))
    println(UITheme.dimText("  • Tasks are automatically saved"))
    println(UITheme.dimText("  • Use priorities: low, medium, high, urgent"))
    println(UITheme.dimText("  • Date format for deadlines: DD.MM.YYYY"))
    println(UITheme.dimText("  • Categories: work, school, private"))

    waitForEnter()
  }

  private def waitForEnter(): Unit = {
    print(UITheme.dimText("\n Press Enter to continue..."))
    val input = StdIn.readLine()
    if (input == null) return
  }

  def createMainMenuItems(): List[MenuItem] = {
    List(
      MenuItem("1", "📋", "Show all tasks", () => TaskManager.showAllTasks()),
      MenuItem("2", "📁", "Show tasks by category", () => Menu.handleShowByCategory()),
      MenuItem("3", "⚡", "Show tasks by priority", () => Menu.handleShowByPriority()),
      MenuItem("4", "⚠️", "Show overdue tasks", () => TaskManager.showOverdueTasks()),
      MenuItem("5", "➕", "Add new task", () => Menu.handleAddTask()),
      MenuItem("6", "🔄", "Change task status", () => Menu.handleChangeStatus()),
      MenuItem("7", "📅", "Update task deadline", () => Menu.handleUpdateDeadline()),
      MenuItem("8", "🏆", "Update task priority", () => Menu.handleUpdatePriority()),
      MenuItem("9", "📝", "Update task description", () => Menu.handleUpdateDescription()),
      MenuItem("10", "🔍", "Show task details", () => Menu.handleShowTaskDetails()),
      MenuItem("11", "📊", "Show analytics", () => showAnalytics()),
      MenuItem("12", "🚪", "Exit", () => {
        UIComponents.clearScreen()
        println(UIComponents.drawNotification("Thanks for using Todo List Manager!", "success"))
        println(UIComponents.drawNotification("Goodbye!", "info"))
        System.exit(0)
      })
    )
  }

  private def showAnalytics(): Unit = {
    val allTasks = TaskManager.getAllTasks
    val totalTasks = allTasks.length

    if (totalTasks == 0) {
      println(UIComponents.drawNotification("No tasks available for analysis", "info"))
      return
    }

    println(UITheme.border("╔" + "═" * 68 + "╗"))
    println(UITheme.border("║") + UIComponents.drawTitle("📊 TASK ANALYTICS", 66) + UITheme.border("║"))
    println(UITheme.border("╚" + "═" * 68 + "╝"))
    println()

    // Status distribution
    val statusCounts = allTasks.groupBy(_.status).mapValues(_.length)
    val openTasks = statusCounts.getOrElse("open", 0)
    val inWorkTasks = statusCounts.getOrElse("in-work", 0)
    val finishedTasks = statusCounts.getOrElse("finished", 0)

    println(UITheme.info("📈 Status Distribution:"))
    println(s"   Open: ${UITheme.progressBar(openTasks, totalTasks)}")
    println(s"   In-Work: ${UITheme.progressBar(inWorkTasks, totalTasks)}")
    println(s"   Finished: ${UITheme.progressBar(finishedTasks, totalTasks)}")
    println()

    // Priority distribution
    val priorityCounts = allTasks.groupBy(_.priority).mapValues(_.length)
    println(UITheme.info("🏆 Priority Distribution:"))
    priorityCounts.foreach { case (priority, count) =>
      val badge = UIComponents.drawPriorityBadge(priority)
      val percentage = (count.toDouble / totalTasks * 100).toInt
      println(s"   $badge: $count tasks ($percentage%)")
    }
    println()

    // Category distribution
    val categoryCounts = allTasks.groupBy(_.category).mapValues(_.length)
    println(UITheme.info("📁 Category Distribution:"))
    categoryCounts.foreach { case (category, count) =>
      val badge = UIComponents.drawCategoryBadge(category)
      val percentage = (count.toDouble / totalTasks * 100).toInt
      println(s"   $badge: $count tasks ($percentage%)")
    }
    println()

    // Overdue analysis
    val overdueTasks = allTasks.filter(_.isOverdue)
    if (overdueTasks.nonEmpty) {
      println(UITheme.warning(s"⚠️  ${overdueTasks.length} task(s) are overdue!"))
    } else {
      println(UITheme.success("✅ No overdue tasks!"))
    }

    // Completion rate
    val completionRate = if (totalTasks > 0) (finishedTasks.toDouble / totalTasks * 100).toInt else 0
    println()
    println(UITheme.info(s"🎯 Overall Progress: ${UITheme.progressBar(finishedTasks, totalTasks)} - $completionRate% complete"))
  }

  @tailrec
  private def displayThemes(themes: List[UITheme.Theme], index: Int): Unit = themes match {
    case Nil => () // Base case
    case theme :: tail =>
      val current = if (theme == UITheme.getCurrentTheme) " (current)" else ""
      val preview = s"${index + 1}. ${theme.name}$current - ${theme.primary}Preview${UITheme.Colors.RESET}"
      println(s"  $preview")
      displayThemes(tail, index + 1)
  }
}
