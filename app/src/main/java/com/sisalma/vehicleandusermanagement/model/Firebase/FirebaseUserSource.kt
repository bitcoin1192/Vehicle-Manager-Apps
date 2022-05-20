package com.sisalma.vehicleandusermanagement.model.Firebase

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.sisalma.vehicleandusermanagement.model.FirebaseUserDataStructure

class FirebaseUserSource(UID: String) {
    val firebaseEndpoint = "https://latihan-34c76-default-rtdb.asia-southeast1.firebasedatabase.app/"
    val userObject = FirebaseDatabase
        .getInstance(firebaseEndpoint)
        .getReference("Userdata").child("UID-$UID")
    val _liveDataUserData: MutableLiveData<FirebaseUserDataStructure> = MutableLiveData()
    val liveDataUserData: LiveData<FirebaseUserDataStructure> get() = _liveDataUserData
    var GroupID = ""
    init {
        attachUserData()
    }
    fun attachUserData(){
        userObject.addValueEventListener(object: ValueEventListener{
            override fun onDataChange(p0: DataSnapshot) {
                GroupID = p0.getValue(FirebaseUserDataStructure::class.java)?.OwnedGID ?: ""
                _liveDataUserData.value = p0.getValue(FirebaseUserDataStructure::class.java)
            }

            override fun onCancelled(p0: DatabaseError) {
                throw p0.toException()
            }

        })
    }
}