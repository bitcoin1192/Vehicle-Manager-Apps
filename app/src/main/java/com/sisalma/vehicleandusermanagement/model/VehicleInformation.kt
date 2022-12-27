package com.sisalma.vehicleandusermanagement.model

data class VehicleInformation(
    val AccKey: String,
    val Manufacturer: String,
    val Model: String,
    val PoliceNum: String,
    val Type: String,
    val UID: Int,
    val VID: Int
)
data class SearchResult(
    val UID: Int,
    val Username: String
)