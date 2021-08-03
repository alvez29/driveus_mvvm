package com.example.driveus_mvvm.view_model

import android.net.Uri
import android.util.Log
import android.util.Patterns
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.driveus_mvvm.R
import com.example.driveus_mvvm.model.entities.User
import com.example.driveus_mvvm.model.entities.Vehicle
import com.example.driveus_mvvm.model.repository.FirestoreRepository
import com.example.driveus_mvvm.ui.enums.VehicleFormEnum
import com.example.driveus_mvvm.ui.enums.SignUpFormEnum
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class UserViewModel : ViewModel() {

    private val tag = "FIRESTORE_USER_VIEW_MODEL"

    private val userDocumentById: MutableLiveData<DocumentSnapshot> = MutableLiveData()
    private val formErrors = MutableLiveData<MutableMap<SignUpFormEnum, Int>>(mutableMapOf())
    private val redirect = MutableLiveData(Pair(false, ""))
    private val imageTrigger =  MutableLiveData(false)
    private val isLoading = MutableLiveData<Boolean>(false)

    private val vehiclesByUserId: MutableLiveData<Map<String, Vehicle>> = MutableLiveData()
    private val vehicleFormError = MutableLiveData<MutableMap<VehicleFormEnum, Int>>(mutableMapOf())
    private val redirectVehicle = MutableLiveData(false)
    private val hasAnySuscription = MutableLiveData(false)
    private val vehicleById: MutableLiveData<Vehicle> = MutableLiveData(null)
    private val isDriver = MutableLiveData(false)

    private val firebaseAuth: FirebaseAuth = FirestoreRepository.getFirebaseAuthInstance()
    private val firebaseStorage: FirebaseStorage = FirestoreRepository.getFirebaseStorageInstance()


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
            firebaseAuth.createUserWithEmailAndPassword(
                inputs[SignUpFormEnum.EMAIL].toString(),
                inputs[SignUpFormEnum.PASSWORD].toString()
            ).addOnCompleteListener {
                //Si es exitoso podemos crear el usuario en Firestore con la uid recien creada
                if (it.isSuccessful) {
                    val uid: String? = it.result?.user?.uid
                    viewModelScope.launch(Dispatchers.IO) {
                        uid?.let {
                            val userReference = FirestoreRepository.createUser(getUserFromInputs(inputs, uid)).await()
                            redirect.postValue(Pair(true, userReference.id))
                            isLoading.postValue(false)
                        }
                    }

                //Si no es exitoso tratamos las excepciones que nos devuelve FirebaseAuth
                } else {
                    it.exception?.let { ex ->
                        treatAuthExceptions(ex)
                    }
                    isLoading.postValue(false)
                }
            }
        } else {
            isLoading.postValue(false)
        }
    }

    private fun validateEditNameAndSurnameForm(inputs: MutableMap<SignUpFormEnum, String>): Boolean {
        val errorMap = mutableMapOf<SignUpFormEnum, Int>()
        var res = true

        //Color
        if (inputs[SignUpFormEnum.NAME].isNullOrBlank()){
            errorMap[SignUpFormEnum.NAME] = R.string.sign_up_form_error_not_empty
            res = false
        }

        //Seats
        if (inputs[SignUpFormEnum.SURNAME].isNullOrBlank()){
            errorMap[SignUpFormEnum.SURNAME] = R.string.sign_up_form_error_not_empty
            res = false
        }

        formErrors.postValue(errorMap)
        return res
    }

    fun getFormErrors(): LiveData<MutableMap<SignUpFormEnum, Int>> {
        return formErrors
    }

    fun getRedirect(): LiveData<Pair<Boolean, String>> {
        return redirect
    }

    fun getImageTrigger(): LiveData<Boolean> {
        return imageTrigger
    }

    fun getUserByUid(uid: String): Query {
        return FirestoreRepository.getUserByUID(uid)
    }

    fun getIsLoading(): LiveData<Boolean> {
        return isLoading
    }

    fun isDriver(userId: String): LiveData<Boolean> {
        FirestoreRepository.getUserById(userId).addSnapshotListener { value, error ->
            if (error != null) {
                Log.w(tag, "Listen failed.", error)
                isDriver.postValue(false)
            }
            val user: User? = value?.toObject(User::class.java)

            isDriver.postValue(user?.isDriver == true)
        }
        return isDriver
    }

    fun hasAnySuscription(userId: String): LiveData<Boolean>? {
        FirestoreRepository.getUserById(userId).addSnapshotListener { value, error ->
            if (error != null) {
                Log.w(tag, "Listen failed.", error)
                hasAnySuscription.postValue(false)
            }
            val user = value?.toObject(User::class.java)
            hasAnySuscription.postValue(user?.channels?.isNotEmpty())
        }
        return hasAnySuscription
    }

    fun getUserById(id: String): LiveData<DocumentSnapshot> {
        FirestoreRepository.getUserById(id)
            .addSnapshotListener { value, error ->
                if ( error != null) {
                    Log.w(tag, "Listen failed.", error)
                    userDocumentById.value = null
                }
                userDocumentById.postValue(value)
               }
        return userDocumentById
    }

    fun createNewUser(inputs: Map<SignUpFormEnum, String>) {
        isLoading.postValue(true)
        FirestoreRepository.usernameInUse(inputs[SignUpFormEnum.USERNAME].toString()).get().addOnSuccessListener {
            if (it.documents.size > 0) {
                validate(inputs, true)
            } else {
                validate(inputs, false)
            }
        }.addOnFailureListener {
            isLoading.postValue(false)
            treatAuthExceptions(it)
        }
    }

    fun editNameAndSurname(userId: String, inputs: MutableMap<SignUpFormEnum, String>) {
        if (validateEditNameAndSurnameForm(inputs)) {
            viewModelScope.launch(Dispatchers.IO) {
                FirestoreRepository.updateName(userId, inputs[SignUpFormEnum.NAME])
                FirestoreRepository.updateSurname(userId, inputs[SignUpFormEnum.SURNAME])
            }

            redirect.postValue(Pair(true, ""))
        }
    }

    fun uploadProfileImageToFirebase(imageUri: Uri, userId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val storeReference = firebaseStorage.getReference("users/$userId")
            storeReference.putFile(imageUri).addOnSuccessListener {
                val currentValueIT: Boolean? = imageTrigger.value
                currentValueIT?.let {
                    imageTrigger.postValue(!it)
                }
            }
        }
    }

    fun deleteImageByUserId(userId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val storeReference = firebaseStorage.getReference("users/$userId")
            storeReference.delete().addOnSuccessListener {
                val currentValueIT: Boolean? = imageTrigger.value
                currentValueIT?.let {
                    imageTrigger.postValue(!it)
                }
            }
        }
    }

    //VEHICLE FUNCTIONS -----------------------------------------------------

    private fun validateNewVehicle(textInputs: Map<VehicleFormEnum, String>): Boolean{
        val errorMap = mutableMapOf<VehicleFormEnum, Int>()
        var res = true

        //Brand
        if (textInputs[VehicleFormEnum.BRAND].isNullOrBlank()){
            errorMap[VehicleFormEnum.BRAND] = R.string.sign_up_form_error_not_empty
            res = false
        }

        //Model
        if (textInputs[VehicleFormEnum.MODEL].isNullOrBlank()){
            errorMap[VehicleFormEnum.MODEL] = R.string.sign_up_form_error_not_empty
            res = false
        }

        //Color
        if (textInputs[VehicleFormEnum.COLOR].isNullOrBlank()){
            errorMap[VehicleFormEnum.COLOR] = R.string.sign_up_form_error_not_empty
            res = false
        }

        //Seats
        if (textInputs[VehicleFormEnum.SEAT].isNullOrBlank()){
            errorMap[VehicleFormEnum.SEAT] = R.string.sign_up_form_error_not_empty
            res = false
        }
        vehicleFormError.postValue(errorMap)
        return res
    }

    private fun validateEditVehicleForm(toEditVehicle: Vehicle): Boolean {
        val errorMap = mutableMapOf<VehicleFormEnum, Int>()
        var res = true

        //Color
        if (toEditVehicle.color.isNullOrBlank()){
            errorMap[VehicleFormEnum.COLOR] = R.string.sign_up_form_error_not_empty
            res = false
        }

        //Seats
        if (toEditVehicle.seats.toString().isBlank() || toEditVehicle.seats == 0){
            errorMap[VehicleFormEnum.SEAT] = R.string.sign_up_form_error_not_empty
            res = false
        }

        vehicleFormError.postValue(errorMap)
        return res
    }

    private fun getNewVehicleFromInputs(inputs: Map<VehicleFormEnum, String>): Vehicle {
        return Vehicle(
            brand = inputs[VehicleFormEnum.BRAND],
            model = inputs[VehicleFormEnum.MODEL],
            seats = inputs[VehicleFormEnum.SEAT]?.toInt(),
            color = inputs[VehicleFormEnum.COLOR],
            description = inputs[VehicleFormEnum.DESCRIPTION])
    }

    private fun getToEditVehicleFromInputs(inputs: Map<VehicleFormEnum, String>, oldVehicle: Vehicle): Vehicle {
        val newSeats = if (inputs[VehicleFormEnum.SEAT].isNullOrBlank()) {
            0
        } else {
            inputs[VehicleFormEnum.SEAT]?.toInt()
        }

        return Vehicle(brand = oldVehicle.brand,
                model = oldVehicle.model,
                seats = newSeats,
                color = inputs[VehicleFormEnum.COLOR],
                description = inputs[VehicleFormEnum.DESCRIPTION],
                expanded = oldVehicle.expanded,
                isInRide = oldVehicle.isInRide
        )
    }

    private fun updateUserRole(id: String, value: QuerySnapshot? ) {
        viewModelScope.launch(Dispatchers.IO) {
            if (value?.documents?.isEmpty() == true) {
                FirestoreRepository.updateIsDriver(id, false)
            }
        }
    }

    fun addNewVehicle(inputs: Map<VehicleFormEnum, String>, documentId: String) {
        if (validateNewVehicle(inputs)){
            viewModelScope.launch(Dispatchers.IO){
                FirestoreRepository.addVehicle(getNewVehicleFromInputs(inputs), documentId)
                FirestoreRepository.updateIsDriver(documentId, true)
            }
            redirectVehicle.postValue(true)
        }
    }

    fun getFormVehicleErrors(): LiveData<MutableMap<VehicleFormEnum, Int>> {
        return vehicleFormError
    }

    fun getRedirectVehicle(): LiveData<Boolean> {
        return redirectVehicle
    }

    fun getVehiclesByUserId(id: String): LiveData<Map<String,Vehicle>> {
        FirestoreRepository.getAllVehiclesByUserId(id)
            .addSnapshotListener { value, error ->
                if (error != null) {
                    Log.w(tag, "Listen failed.", error)
                    vehiclesByUserId.value = mutableMapOf()
                }

                updateUserRole(id, value)

                val documents = value?.documents
                val mapIdVehicles:MutableMap<String, Vehicle> = mutableMapOf()

                if (documents != null) {
                    for (d in documents) {
                        d.toObject(Vehicle::class.java)?.let {
                                it1 -> mapIdVehicles.put(d.id, it1)
                        }
                    }
                }

                vehiclesByUserId.postValue(mapIdVehicles)
            }

        return vehiclesByUserId
    }

    fun getVehicleById(vehicleId: String, driverId: String): LiveData<Vehicle> {
        FirestoreRepository.getVehicleById(vehicleId, driverId)
            .addSnapshotListener { value, error ->
                if (error != null) {
                    Log.w(tag, "Listen failed.", error)
                    vehicleById.value = null
                }

                val vehicle = value?.toObject(Vehicle::class.java)

                vehicle?.let {
                    vehicleById.postValue(it)
                }
            }

        return vehicleById
    }


    fun deleteVehicleById(userId: String, vehicleId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            FirestoreRepository.deleteVehicleById(userId, vehicleId)
        }
    }

    fun editVehicle(userId: String, vehicleId: String, oldVehicle: Vehicle, newInputs: Map<VehicleFormEnum, String>) {
        val toEditVehicle = getToEditVehicleFromInputs(newInputs, oldVehicle)
        if (validateEditVehicleForm(toEditVehicle)) {
            viewModelScope.launch(Dispatchers.IO) {
                FirestoreRepository.updateVehicle(userId, vehicleId, toEditVehicle)
            }

            redirectVehicle.postValue(true)
        }
    }

}