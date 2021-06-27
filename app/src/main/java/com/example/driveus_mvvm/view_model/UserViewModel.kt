package com.example.driveus_mvvm.view_model

import android.util.Log
import android.util.Patterns
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.driveus_mvvm.R
import com.example.driveus_mvvm.model.entities.User
import com.example.driveus_mvvm.model.repository.FirestoreRepository
import com.example.driveus_mvvm.ui.enums.SignUpFormEnum
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.firestore.DocumentReference
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class UserViewModel : ViewModel() {

    private val tag = "FIRESTORE_USER_VIEW_MODEL"

    private val userById: MutableLiveData<User> = MutableLiveData()
    private val formErrors = MutableLiveData<MutableMap<SignUpFormEnum, Int>>(mutableMapOf())
    private val redirect = MutableLiveData(false)

    private fun validateForm(textInputs: Map<SignUpFormEnum, String>, usernameInUse: Boolean): Boolean {
        val errorMap = mutableMapOf<SignUpFormEnum, Int>()
        var res = true

        //Name
        if (textInputs[SignUpFormEnum.NAME].isNullOrBlank()) {
            errorMap[SignUpFormEnum.NAME] = R.string.sign_up_form_error_not_empty
            res = false
        } else if (textInputs[SignUpFormEnum.NAME].toString().length > 50) {
            errorMap[SignUpFormEnum.NAME] = R.string.sign_up_form_error_max_50
            res = false
        }

        //Surname
        if (textInputs[SignUpFormEnum.SURNAME].isNullOrBlank()) {
            errorMap[SignUpFormEnum.SURNAME] = R.string.sign_up_form_error_not_empty
            res = false
        } else if (textInputs[SignUpFormEnum.SURNAME].toString().length > 50) {
            errorMap[SignUpFormEnum.SURNAME] = R.string.sign_up_form_error_max_50
            res = false
        }

        //Username
        if (textInputs[SignUpFormEnum.USERNAME].isNullOrBlank()) {
            errorMap[SignUpFormEnum.USERNAME] = R.string.sign_up_form_error_not_empty
            res = false
        } else if (textInputs[SignUpFormEnum.USERNAME].toString().length > 50) {
            errorMap[SignUpFormEnum.USERNAME] = R.string.sign_up_form_error_max_50
            res = false
        } else if (usernameInUse) {
            errorMap[SignUpFormEnum.USERNAME] = R.string.sign_up_form_error_username_in_use
            res = false
        }

        //Email
        if (textInputs[SignUpFormEnum.EMAIL].isNullOrBlank()) {
            errorMap[SignUpFormEnum.EMAIL] = R.string.sign_up_form_error_not_empty
            res = false
        } else if (Patterns.EMAIL_ADDRESS.matcher(textInputs[SignUpFormEnum.EMAIL].toString()).matches().not()) {
            errorMap[SignUpFormEnum.EMAIL] = R.string.sign_up_form_error_email_pattern
            res = false
        }

        //Password
        if (textInputs[SignUpFormEnum.PASSWORD].isNullOrBlank()) {
            errorMap[SignUpFormEnum.PASSWORD] = R.string.sign_up_form_error_not_empty
            res = false
        }

        //Confirm Password
        if (textInputs[SignUpFormEnum.CONFIRM_PASSWORD].isNullOrBlank()) {
            errorMap[SignUpFormEnum.CONFIRM_PASSWORD] = R.string.sign_up_form_error_not_empty
            res = false
        } else if (textInputs[SignUpFormEnum.CONFIRM_PASSWORD].toString() != textInputs[SignUpFormEnum.PASSWORD].toString()) {
            errorMap[SignUpFormEnum.CONFIRM_PASSWORD] = R.string.sign_up_form_error_pass_not_match
            res = false
        }

        formErrors.postValue(errorMap)

        return res
    }

    private fun getUserFromInputs(inputs: Map<SignUpFormEnum, String>, uid: String): User {
        return User(
            uid = uid,
            name = inputs[SignUpFormEnum.NAME],
            surname = inputs[SignUpFormEnum.SURNAME],
            username = inputs[SignUpFormEnum.USERNAME],
            email = inputs[SignUpFormEnum.EMAIL]
        )
    }

    private fun treatAuthExceptions(ex: Exception) {
        val errorMap = mutableMapOf<SignUpFormEnum, Int>()

        if (ex::class.java == FirebaseAuthUserCollisionException::class.java) {
            errorMap[SignUpFormEnum.EMAIL] = R.string.sign_up_form_error_email_in_use
        }

        formErrors.postValue(errorMap)
    }

    private fun validate(inputs: Map<SignUpFormEnum, String>, usernameInUse: Boolean) {
        //Validamos los inputs del formulario y si el nombre de usuario está en uso
        if (validateForm(inputs, usernameInUse) ) {

            //Una vez validados creamos la instancia del usuario en FirebaseAuth
            FirebaseAuth.getInstance().createUserWithEmailAndPassword(
                inputs[SignUpFormEnum.EMAIL].toString(),
                inputs[SignUpFormEnum.PASSWORD].toString()
            ).addOnCompleteListener {

                //Si es exitoso podemos crear el usuario en Firestore con la uid recien creada
                if (it.isSuccessful) {
                    val uid: String? = it.result?.user?.uid
                    viewModelScope.launch(Dispatchers.IO) {
                        uid?.let {
                            FirestoreRepository.createUser(getUserFromInputs(inputs, uid))
                        }
                    }

                    redirect.postValue(true)

                //Si no es exitoso tratamos las excepciones que nos devuelve FirebaseAuth
                } else {
                    it.exception?.let { ex ->
                        treatAuthExceptions(ex)
                    }
                }
            }
        }
    }

    fun getFormErrors(): LiveData<MutableMap<SignUpFormEnum, Int>> {
        return formErrors
    }

    fun getRedirect(): LiveData<Boolean> {
        return redirect
    }

    fun getUserById(userId: String): LiveData<User> {
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

    //Función para obtener la id del documento del usuario con la uid por parámetro.
    // Se utilizará para actualizar los datos de la sesión
    fun getDocumentIdFromUID(uid: String): String {
        var res: String = ""

        FirestoreRepository.getUserByUID(uid).get().addOnSuccessListener {
            if (it.documents.size == 1) {
                res = it.documents.first().id
            }
        }

        return res
    }

    fun updateUserName(userId: String, name: String) {
        viewModelScope.launch(Dispatchers.IO) {
            FirestoreRepository.updateUserName(userId, name)
        }
    }

    fun createNewUser(inputs: Map<SignUpFormEnum, String>) {
        FirestoreRepository.usernameInUse(inputs[SignUpFormEnum.USERNAME].toString()).get().addOnSuccessListener {
            if (it.documents.size > 0) {
                validate(inputs, true)
            } else {
                validate(inputs, false)
            }
        }
    }
}