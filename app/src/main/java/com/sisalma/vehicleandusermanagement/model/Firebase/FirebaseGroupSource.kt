package com.sisalma.vehicleandusermanagement.model.Firebase

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.sisalma.vehicleandusermanagement.model.FirebaseGroupDataStructure

class FirebaseGroupSource(GID: String) {
    val firebaseEndpoint = "https://latihan-34c76-default-rtdb.asia-southeast1.firebasedatabase.app/"
    val groupObject = FirebaseDatabase
        .getInstance(firebaseEndpoint)
        .getReference("GIDMember").child(GID)

    private val _liveDataGroupSource: MutableLiveData<FirebaseGroupDataStructure> = MutableLiveData()
    val liveDataGroupSource: LiveData<FirebaseGroupDataStructure> get() = _liveDataGroupSource

    init {
        attachGroupData()
    }
    fun attachGroupData(){
        groupObject.addValueEventListener(object:ValueEventListener{
            override fun onDataChange(p0: DataSnapshot) {
                _liveDataGroupSource.value = p0.getValue(FirebaseGroupDataStructure::class.java)
            }

            override fun onCancelled(p0: DatabaseError) {
                throw p0.toException()
            }

        })
    }
}