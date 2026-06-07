package com.orinasa.app.model

data class Department(
    val id: String = "",
    val companyId: String = "",
    val name: String = "",
    val managerId: String = "",
    val managerName: String = "",
    val employeeCount: Int = 0,
    val createdAt: Long = 0L
)