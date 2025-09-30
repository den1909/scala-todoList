object UITheme {

  // ANSI Color Codes
  object Colors {
    val RESET = "\u001B[0m"
    val BLACK = "\u001B[30m"
    val RED = "\u001B[31m"
    val GREEN = "\u001B[32m"
    val YELLOW = "\u001B[33m"
    val BLUE = "\u001B[34m"
    val PURPLE = "\u001B[35m"
    val CYAN = "\u001B[36m"
    val WHITE = "\u001B[37m"

    // Bright colors
    val BRIGHT_BLACK = "\u001B[90m"
    val BRIGHT_RED = "\u001B[91m"
    val BRIGHT_GREEN = "\u001B[92m"
    val BRIGHT_YELLOW = "\u001B[93m"
    val BRIGHT_BLUE = "\u001B[94m"
    val BRIGHT_PURPLE = "\u001B[95m"
    val BRIGHT_CYAN = "\u001B[96m"
    val BRIGHT_WHITE = "\u001B[97m"

    // Background colors
    val BG_BLACK = "\u001B[40m"
    val BG_RED = "\u001B[41m"
    val BG_GREEN = "\u001B[42m"
    val BG_YELLOW = "\u001B[43m"
    val BG_BLUE = "\u001B[44m"
    val BG_PURPLE = "\u001B[45m"
    val BG_CYAN = "\u001B[46m"
    val BG_WHITE = "\u001B[47m"

    // Text formatting
    val BOLD = "\u001B[1m"
    val DIM = "\u001B[2m"
    val ITALIC = "\u001B[3m"
    val UNDERLINE = "\u001B[4m"
    val BLINK = "\u001B[5m"
    val REVERSE = "\u001B[7m"
    val STRIKETHROUGH = "\u001B[9m"
  }

  // Theme definitions
  sealed trait Theme {
    def primary: String
    def secondary: String
    def accent: String
    def success: String
    def warning: String
    def error: String
    def info: String
    def border: String
    def text: String
    def dimText: String
    def background: String
    def name: String
  }

  object DarkTheme extends Theme {
    val name = "Dark"
    val primary = Colors.BRIGHT_BLUE
    val secondary = Colors.BRIGHT_PURPLE
    val accent = Colors.BRIGHT_CYAN
    val success = Colors.BRIGHT_GREEN
    val warning = Colors.BRIGHT_YELLOW
    val error = Colors.BRIGHT_RED
    val info = Colors.BRIGHT_WHITE
    val border = Colors.BRIGHT_BLACK
    val text = Colors.WHITE
    val dimText = Colors.BRIGHT_BLACK
    val background = Colors.BLACK
  }

  object LightTheme extends Theme {
    val name = "Light"
    val primary = Colors.BLUE
    val secondary = Colors.PURPLE
    val accent = Colors.CYAN
    val success = Colors.GREEN
    val warning = Colors.YELLOW
    val error = Colors.RED
    val info = Colors.BLACK
    val border = Colors.BLACK
    val text = Colors.BLACK
    val dimText = Colors.BRIGHT_BLACK
    val background = Colors.WHITE
  }

  object NeonTheme extends Theme {
    val name = "Neon"
    val primary = Colors.BRIGHT_PURPLE
    val secondary = Colors.BRIGHT_CYAN
    val accent = Colors.BRIGHT_GREEN
    val success = Colors.BRIGHT_GREEN
    val warning = Colors.BRIGHT_YELLOW
    val error = Colors.BRIGHT_RED
    val info = Colors.BRIGHT_WHITE
    val border = Colors.BRIGHT_PURPLE
    val text = Colors.BRIGHT_WHITE
    val dimText = Colors.PURPLE
    val background = Colors.BLACK
  }

  object OceanTheme extends Theme {
    val name = "Ocean"
    val primary = Colors.BRIGHT_BLUE
    val secondary = Colors.BRIGHT_CYAN
    val accent = Colors.BLUE
    val success = Colors.BRIGHT_GREEN
    val warning = Colors.BRIGHT_YELLOW
    val error = Colors.BRIGHT_RED
    val info = Colors.BRIGHT_WHITE
    val border = Colors.BRIGHT_BLUE
    val text = Colors.BRIGHT_WHITE
    val dimText = Colors.BLUE
    val background = Colors.BLACK
  }

  // Current theme state
  private var currentTheme: Theme = DarkTheme

  def getCurrentTheme: Theme = currentTheme
  def setTheme(theme: Theme): Unit = { currentTheme = theme }

  def getAvailableThemes: List[Theme] = List(DarkTheme, LightTheme, NeonTheme, OceanTheme)

  // Utility methods for styled text
  def styled(text: String, color: String): String = s"$color$text${Colors.RESET}"
  def bold(text: String): String = s"${Colors.BOLD}$text${Colors.RESET}"
  def dim(text: String): String = s"${Colors.DIM}$text${Colors.RESET}"
  def underline(text: String): String = s"${Colors.UNDERLINE}$text${Colors.RESET}"

  // Quick access to current theme colors
  def primary(text: String): String = styled(text, currentTheme.primary)
  def secondary(text: String): String = styled(text, currentTheme.secondary)
  def accent(text: String): String = styled(text, currentTheme.accent)
  def success(text: String): String = styled(text, currentTheme.success)
  def warning(text: String): String = styled(text, currentTheme.warning)
  def error(text: String): String = styled(text, currentTheme.error)
  def info(text: String): String = styled(text, currentTheme.info)
  def border(text: String): String = styled(text, currentTheme.border)
  def text(text: String): String = styled(text, currentTheme.text)
  def dimText(text: String): String = styled(text, currentTheme.dimText)

  // Animation effects
  def rainbow(text: String): String = {
    val colors = List(Colors.RED, Colors.YELLOW, Colors.GREEN, Colors.CYAN, Colors.BLUE, Colors.PURPLE)
    text.zipWithIndex.map { case (char, index) =>
      val color = colors(index % colors.length)
      s"$color$char"
    }.mkString + Colors.RESET
  }

  def blink(text: String): String = s"${Colors.BLINK}$text${Colors.RESET}"

  // Progress bar
  def progressBar(current: Int, total: Int, width: Int = 20): String = {
    val percentage = if (total > 0) (current.toDouble / total * 100).toInt else 0
    val filled = (current.toDouble / total * width).toInt
    val empty = width - filled

    val bar = "█" * filled + "░" * empty
    val coloredBar = if (percentage < 50) warning(bar)
                    else if (percentage < 80) accent(bar)
                    else success(bar)

    s"$coloredBar ${primary(s"$percentage%")} ${dimText(s"($current/$total)")}"
  }
}