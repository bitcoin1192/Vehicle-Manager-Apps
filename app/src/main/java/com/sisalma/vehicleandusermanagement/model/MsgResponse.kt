package com.sisalma.vehicleandusermanagement.model

data class MsgResponse(
    val BorrowedVehicle: List<VehicleInformation>,
    val OwnedVehicle: List<VehicleInformation>
)