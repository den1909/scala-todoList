object UIComponents {

  // Box drawing characters
  private val boxChars = Map(
    "topLeft" -> "╔",
    "topRight" -> "╗",
    "bottomLeft" -> "╚",
    "bottomRight" -> "╝",
    "horizontal" -> "═",
    "vertical" -> "║",
    "cross" -> "╬",
    "teeDown" -> "╦",
    "teeUp" -> "╩",
    "teeRight" -> "╠",
    "teeLeft" -> "╣"
  )

  def clearScreen(): Unit = {
    print("\u001b[2J\u001b[H")
  }

  def moveCursor(row: Int, col: Int): Unit = {
    print(s"\u001b[${row};${col}H")
  }

  def hideCursor(): Unit = print("\u001b[?25l")
  def showCursor(): Unit = print("\u001b[?25h")

  def drawBox(width: Int, height: Int, title: String = ""): List[String] = {
    val theme = UITheme.getCurrentTheme
    val lines = scala.collection.mutable.ListBuffer[String]()

    // Top border
    val topBorder = if (title.nonEmpty) {
      val titlePadding = (width - title.length - 4) / 2
      val leftPadding = "═" * titlePadding
      val rightPadding = "═" * (width - title.length - 4 - titlePadding)
      s"${boxChars("topLeft")}$leftPadding ${UITheme.bold(UITheme.primary(title))} $rightPadding${boxChars("topRight")}"
    } else {
      s"${boxChars("topLeft")}${"═" * (width - 2)}${boxChars("topRight")}"
    }
    lines += UITheme.border(topBorder)

    // Middle lines
    for (_ <- 1 until height - 1) {
      lines += UITheme.border(s"${boxChars("vertical")}${" " * (width - 2)}${boxChars("vertical")}")
    }

    // Bottom border
    val bottomBorder = s"${boxChars("bottomLeft")}${"═" * (width - 2)}${boxChars("bottomRight")}"
    lines += UITheme.border(bottomBorder)

    lines.toList
  }

  def drawTitle(text: String, width: Int = 70): String = {
    val padding = (width - text.length) / 2
    val leftPad = " " * padding
    val rightPad = " " * (width - text.length - padding)
    UITheme.bold(UITheme.primary(s"$leftPad$text$rightPad"))
  }

  def drawSeparator(width: Int = 70, char: String = "═"): String = {
    UITheme.border(char * width)
  }

  def drawMenuItem(number: Int, icon: String, text: String, isSelected: Boolean = false): String = {
    val theme = UITheme.getCurrentTheme
    val prefix = if (isSelected) "►" else " "
    val numberStr = if (number < 10) s" $number" else s"$number"

    val menuText = s"$prefix $numberStr. $icon $text"

    if (isSelected) {
      s"${theme.background}${theme.accent}$menuText${UITheme.Colors.RESET}"
    } else {
      UITheme.text(menuText)
    }
  }

  def drawProgressIndicator(step: Int, totalSteps: Int): String = {
    val dots = "●" * step + "○" * (totalSteps - step)
    UITheme.accent(dots)
  }

  def drawStatusBadge(status: String): String = {
    status.toLowerCase match {
      case "open" => UITheme.warning("● OPEN")
      case "in-work" => UITheme.accent("● IN-WORK")
      case "finished" => UITheme.success("● FINISHED")
      case _ => UITheme.dimText(s"● ${status.toUpperCase}")
    }
  }

  def drawPriorityBadge(priority: String): String = {
    priority.toLowerCase match {
      case "urgent" => UITheme.error("🔴 URGENT")
      case "high" => UITheme.warning("🟠 HIGH")
      case "medium" => UITheme.accent("🟡 MEDIUM")
      case "low" => UITheme.success("🟢 LOW")
      case _ => UITheme.dimText(s"● ${priority.toUpperCase}")
    }
  }

  def drawCategoryBadge(category: String): String = {
    category.toLowerCase match {
      case "work" => UITheme.primary("💼 WORK")
      case "school" => UITheme.accent("📚 SCHOOL")
      case "private" => UITheme.success("🏠 PRIVATE")
      case _ => UITheme.dimText(s"📁 ${category.toUpperCase}")
    }
  }

  def drawNotification(message: String, notificationType: String = "info"): String = {
    val icon = notificationType match {
      case "success" => "✅"
      case "error" => "❌"
      case "warning" => "⚠️"
      case "info" => "ℹ️"
      case _ => "💬"
    }

    val coloredMessage = notificationType match {
      case "success" => UITheme.success(message)
      case "error" => UITheme.error(message)
      case "warning" => UITheme.warning(message)
      case "info" => UITheme.info(message)
      case _ => UITheme.text(message)
    }

    s"$icon $coloredMessage"
  }

  def drawLoadingSpinner(frame: Int): String = {
    val frames = Array("⠋", "⠙", "⠹", "⠸", "⠼", "⠴", "⠦", "⠧", "⠇", "⠏")
    val spinner = frames(frame % frames.length)
    UITheme.accent(s"$spinner Loading...")
  }

  def drawTaskCard(task: Task): String = {
    val theme = UITheme.getCurrentTheme
    val lines = scala.collection.mutable.ListBuffer[String]()

    // Header with ID and title
    val header = s"${UITheme.bold(UITheme.primary(s"#${task.id}"))} ${UITheme.bold(task.title)}"
    lines += header

    // Status, Priority, Category line
    val badges = List(
      drawStatusBadge(task.status),
      drawPriorityBadge(task.priority),
      drawCategoryBadge(task.category)
    ).mkString("  ")
    lines += badges

    // Deadline and creation date
    val deadlineText = if (task.isOverdue) {
      UITheme.error(s"⏰ ${task.formattedDeadline} [OVERDUE!]")
    } else {
      UITheme.info(s"⏰ ${task.formattedDeadline}")
    }

    val createdText = UITheme.dimText(s"📅 Created: ${task.createdAt.format(java.time.format.DateTimeFormatter.ofPattern("dd.MM.yyyy"))}")
    lines += s"$deadlineText  $createdText"

    // Description if present
    task.description.foreach { desc =>
      lines += UITheme.dimText(s"📝 $desc")
    }

    lines.mkString("\n")
  }

  def drawWelcomeScreen(): Unit = {
    clearScreen()
    println()
    println(drawTitle("📋 Todo List Manager", 70))
    println(UITheme.dimText(drawTitle("Manage your tasks efficiently", 70)))
    println()
    println(drawSeparator(70))
    println()
  }

  def animateText(text: String, delay: Int = 50): Unit = {
    text.foreach { char =>
      print(char)
      Thread.sleep(delay)
    }
  }

  def drawThemeSelector(): Unit = {
    println(UITheme.border("╔" + "═" * 68 + "╗"))
    println(UITheme.border("║") + drawTitle("🎨 THEME SELECTOR", 66) + UITheme.border("║"))
    println(UITheme.border("╚" + "═" * 68 + "╝"))
    println()

    UITheme.getAvailableThemes.zipWithIndex.foreach { case (theme, index) =>
      val isSelected = theme == UITheme.getCurrentTheme
      val marker = if (isSelected) "►" else " "
      val preview = s"$marker ${index + 1}. ${theme.name} Theme"

      if (isSelected) {
        println(UITheme.bold(UITheme.accent(preview)))
      } else {
        println(UITheme.text(preview))
      }
    }

    println()
    println(UITheme.dimText("Select a theme by entering its number..."))
  }
}