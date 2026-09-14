# Orinasa

**Application Android de gestion des ressources humaines pour les PME** :
pointage, congés, fiches de paie, équipe et annonces, réunis dans une seule
application.

*Orinasa* signifie « entreprise » en malgache. La paie applique les règles de
Madagascar (CNAPS, OSTIE, IRSA).

![Kotlin](https://img.shields.io/badge/Kotlin-2.0-7F52FF?logo=kotlin&logoColor=white)
![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-Material%203-4285F4?logo=jetpackcompose&logoColor=white)
![Firebase](https://img.shields.io/badge/Firebase-Auth%20%C2%B7%20Firestore%20%C2%B7%20FCM-FFCA28?logo=firebase&logoColor=black)
![Android](https://img.shields.io/badge/Android-8.0%2B-3DDC84?logo=android&logoColor=white)
![Licence](https://img.shields.io/badge/licence-MIT-blue)

---

## Fonctionnalités

### Entreprise et comptes
- Création d'une entreprise par son administrateur
- Arrivée des employés par **code d'invitation**
- Deux profils : **administrateur** et **employé**

### Pointage
- Enregistrement de l'heure d'arrivée et de départ
- Historique personnel, et vue du pointage de toute l'équipe

### Congés
- Demande de congé par type, avec dates de début et de fin
- Validation par l'administrateur : *en attente*, *accepté*, *refusé*, *annulé*
- Historique des demandes

### Paie
- Génération des fiches de paie : salaire de base, heures supplémentaires
  majorées de 50 %, primes et indemnités
- Cotisations **CNAPS** (1 % salarié, 13 % employeur) et **OSTIE** (1 % / 5 %)
- Impôt **IRSA** calculé par tranches
- Consultation de ses fiches par chaque employé

> Les barèmes (2024) sont définis dans `utils/Constants.kt` et
> `utils/PayCalculator.kt` : à mettre à jour si la réglementation change.

### Équipe et communication
- Ajout d'employés et fiche détaillée de chacun
- Annonces à toute l'entreprise
- Notifications push
- Profil et paramètres

---

## Stack technique

| | |
|---|---|
| Langage | Kotlin 2.0 |
| Interface | Jetpack Compose, Material 3, Navigation Compose |
| Architecture | MVVM : écrans → ViewModels → repositories |
| Données | Firebase Authentication, Cloud Firestore |
| Notifications | Firebase Cloud Messaging |
| Médias | Cloudinary (envoi), Coil (affichage) |
| Préférences | DataStore |
| Cible | Android 8.0 et plus (minSdk 26), compileSdk 35 |

---

## Structure du projet

```
app/src/main/java/com/orinasa/app/
├── ui/           écrans : connexion, tableaux de bord, équipe, paie, annonces, profil
├── attendance/   écrans de pointage
├── leave/        écrans de congés
├── viewmodel/    un ViewModel par domaine
├── repository/   accès à Firestore
├── model/        User, Company, Attendance, LeaveRequest, Payslip…
└── utils/        calcul de paie, notifications, envoi Cloudinary, constantes
```

Collections Firestore utilisées : `companies`, `users`, `attendance`, `leaves`,
`payslips`, `departments`, `announcements`, `notifications`.

---

## Lancer le projet

**Prérequis** : Android Studio récent, JDK 17, un compte Firebase et un compte
Cloudinary.

1. **Cloner le dépôt**
   ```bash
   git clone https://github.com/rnandresy/orinasa.git
   ```
2. **Configurer Firebase**
   - créer un projet Firebase et y ajouter une application Android avec le
     nom de paquet `com.orinasa.app` ;
   - activer **Authentication** (e-mail / mot de passe), **Cloud Firestore** et
     **Cloud Messaging** ;
   - télécharger `google-services.json` et le placer dans `app/`.

   Ce fichier est propre à chaque projet Firebase : il est volontairement exclu
   du dépôt.
3. **Configurer Cloudinary** : renseigner ton `CLOUD_NAME` et un preset d'envoi
   non signé (`UPLOAD_PRESET`) dans `utils/CloudinaryUploader.kt`.
4. **Ouvrir le projet dans Android Studio**, laisser Gradle se synchroniser,
   puis lancer l'application sur un émulateur ou un téléphone.

> Les règles de sécurité Firestore ne sont pas fournies : définis-les dans la
> console Firebase avant toute utilisation réelle.

---

## Auteur

**Christiano** — [github.com/rnandresy](https://github.com/rnandresy)

## Licence

Distribué sous licence MIT. Voir [LICENSE](LICENSE).
