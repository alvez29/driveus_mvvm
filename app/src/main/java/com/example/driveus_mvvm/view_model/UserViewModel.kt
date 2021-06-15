package com.example.driveus_mvvm.view_model

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.driveus_mvvm.model.entities.User
import com.example.driveus_mvvm.model.repository.FirestoreRepository
import com.google.firebase.firestore.EventListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class UserViewModel : ViewModel() {

    private val tag = "FIRESTORE_USER_VIEW_MODEL"

    private val userById : MutableLiveData<User> = MutableLiveData()

    fun getUserById(userId: String) : LiveData<User> {
        FirestoreRepository.getUserById(userId)
            .addSnapshotListener { value, error ->
                if (error != null) {
                    Log.w(tag, "Listen failed.", error)
                    userById.value = null
                }

                val user: User? = value?.toObject(User::class.java)
                userById.postValue(user)

            }

        return userById
    }

    fun updateUserName(userId: String, name: String ) {
        viewModelScope.launch(Dispatchers.IO) {
            FirestoreRepository.updateUserName(userId, name)
        }
    }

}