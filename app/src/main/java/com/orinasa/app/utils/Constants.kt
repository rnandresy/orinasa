package com.orinasa.app.utils

// ── Collections Firestore ─────────────────────────────────────────────────────
const val COLLECTION_COMPANIES     = "companies"
const val COLLECTION_USERS         = "users"
const val COLLECTION_ATTENDANCE    = "attendance"
const val COLLECTION_LEAVES        = "leaves"
const val COLLECTION_PAYSLIPS      = "payslips"
const val COLLECTION_DEPARTMENTS   = "departments"
const val COLLECTION_ANNOUNCEMENTS = "announcements"
const val COLLECTION_NOTIFICATIONS = "notifications"

// ── Cotisations malgaches (2024) ──────────────────────────────────────────────
const val CNAPS_EMPLOYEE_RATE          = 0.01
const val CNAPS_EMPLOYER_RATE          = 0.13
const val OSTIE_EMPLOYEE_RATE          = 0.01
const val OSTIE_EMPLOYER_RATE          = 0.05
const val SMIG_SECTEUR_NON_AGRICOLE    = 325_000.0

// ── App ───────────────────────────────────────────────────────────────────────
const val INVITE_CODE_LENGTH = 6
const val MAX_PHOTO_SIZE_MB  = 5

// ── Tranche IRSA ──────────────────────────────────────────────────────────────
val IRSA_BRACKETS = listOf(
    Triple(0.0,       350_000.0,        0.00),
    Triple(350_001.0, 400_000.0,        0.05),
    Triple(400_001.0, 500_000.0,        0.10),
    Triple(500_001.0, 600_000.0,        0.15),
    Triple(600_001.0, Double.MAX_VALUE, 0.20)
)

// ── Secteurs d'activité ───────────────────────────────────────────────────────
val COMPANY_SECTORS = listOf(
    "Commerce & Distribution",
    "BTP & Construction",
    "Agriculture & Élevage",
    "Transport & Logistique",
    "Industrie & Production",
    "Services & Conseil",
    "ONG & Associations",
    "Éducation & Formation",
    "Santé & Pharmacie",
    "Hôtellerie & Restauration",
    "Technologie & Informatique",
    "Finance & Assurance",
    "Autre"
)

// ── Types de contrat ──────────────────────────────────────────────────────────
val CONTRACT_TYPES = listOf("CDI", "CDD", "Stage", "Consultant", "Freelance")