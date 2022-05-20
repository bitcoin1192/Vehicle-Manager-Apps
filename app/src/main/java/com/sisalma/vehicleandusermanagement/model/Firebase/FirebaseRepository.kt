package com.sisalma.vehicleandusermanagement.model.Firebase

import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.sisalma.vehicleandusermanagement.model.FirebaseGroupDataStructure
import com.sisalma.vehicleandusermanagement.model.FirebaseUserDataStructure

class FirebaseRepository(UID: String, context: AppCompatActivity) {
    val FirebaseUserSource = FirebaseUserSource(UID)
    lateinit var FirebaseGroupSource: FirebaseGroupSource

    private val _userData: MutableLiveData<FirebaseUserDataStructure> = MutableLiveData()
    val userData: LiveData<FirebaseUserDataStructure> get()= _userData
    private val _groupData: MutableLiveData<FirebaseGroupDataStructure> = MutableLiveData()
    val groupData: LiveData<FirebaseGroupDataStructure> get()= _groupData

    init {
        FirebaseUserSource.liveDataUserData.observe(context) { userData ->
            Log.i("FirebaseRepository", "$userData")
            _userData.value = userData
            FirebaseGroupSource = FirebaseGroupSource(userData.OwnedGID)
            FirebaseGroupSource.liveDataGroupSource.observe(context) { groupData ->
                Log.i("FirebaseRepository", "Incoming group data for user ${userData.Nama}")
                _groupData.value = groupData
            }
        }
    }
}