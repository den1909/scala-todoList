import java.time.LocalDate
import java.time.format.DateTimeFormatter

case class Task(
  id: Int,
  title: String,
  category: String,    // "work", "school", "private"
  status: String,      // "open", "in-work", "finished"
  priority: String,    // "low", "medium", "high", "urgent"
  deadline: Option[LocalDate],
  description: Option[String],
  createdAt: LocalDate
) {
  def isOverdue: Boolean = deadline.exists(_.isBefore(LocalDate.now()))

  def formattedDeadline: String = deadline match {
    case Some(date) => date.format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))
    case None => "No deadline"
  }

  def priorityWeight: Int = priority match {
    case "urgent" => 4
    case "high" => 3
    case "medium" => 2
    case "low" => 1
    case _ => 0
  }
}