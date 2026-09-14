# Règles R8 d'orinasa.

# Firestore relit les modèles par réflexion (toObject) : R8 ne doit ni renommer
# ni retirer leurs champs, accesseurs et constructeurs sans argument. Sans cette
# règle, le build release lit des objets vides ou plante à l'exécution.
-keep class com.orinasa.app.model.** { *; }

# Les annotations Firestore (@PropertyName…) et les types génériques doivent
# survivre à l'optimisation.
-keepattributes Signature, *Annotation*
