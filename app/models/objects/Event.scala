package models.objects

case class Event(id: Long, title: String, description: String, groupName: String, tags: List[String], eventType: String)

