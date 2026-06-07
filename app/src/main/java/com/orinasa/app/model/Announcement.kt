package com.orinasa.app.model

data class Announcement(
    val id: String = "",
    val companyId: String = "",
    val title: String = "",
    val content: String = "",
    val authorId: String = "",
    val authorName: String = "",
    val priority: AnnouncementPriority = AnnouncementPriority.NORMAL,
    val targetDepartmentId: String = "",  // "" = tous
    val targetDepartmentName: String = "",
    val readBy: List<String> = emptyList(),
    val timestamp: Long = 0L
) {
    fun isReadBy(userId: String) = userId in readBy
}

enum class AnnouncementPriority { NORMAL, IMPORTANT, URGENT }