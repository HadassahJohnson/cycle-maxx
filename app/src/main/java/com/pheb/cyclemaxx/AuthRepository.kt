package com.pheb.cyclemaxx

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore

class AuthRepository {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()

    fun signUp(
        email: String,
        password: String,
        userProfile: UserProfile,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val uid = auth.currentUser?.uid ?: ""
                    val profileWithUid = userProfile.copy(uid = uid)
                    
                    saveUserProfile(profileWithUid, onSuccess, onFailure)
                } else {
                    onFailure(task.exception?.message ?: "Authentication failed")
                }
            }
    }

    private fun saveUserProfile(
        userProfile: UserProfile,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        db.collection("users")
            .document(userProfile.uid)
            .set(userProfile)
            .addOnSuccessListener {
                onSuccess()
            }
            .addOnFailureListener { e ->
                onFailure(e.message ?: "Failed to save user profile")
            }
    }

    fun getCurrentUser(): FirebaseUser? = auth.currentUser
}