package com.sisalma.vehicleandusermanagement.model


data class FirebaseUserDataStructure(var Nama: String = "", var OwnedGID: String = "")
data class FirebaseGroupDataStructure(val OwnedVID: ArrayList<FundamentalVehicleData> = arrayListOf())


data class FundamentalVehicleData(var Merk: String = "", var Tahun: Int = 0, var VID: Int = 0,
                                  var Type: String = "", var TrainedNNPath: String = "", var MemberTotal: Int = 0, var PoliceNum: String = "")