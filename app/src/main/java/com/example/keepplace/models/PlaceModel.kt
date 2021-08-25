package com.example.keepplace.models

import java.io.Serializable

data class PlaceModel(
    val id: Int,
    val title: String,
    val image : String,
    val description: String,
    val date: String,
    val location: String,
    val latitude: Double,
    val longitude: Double
): Serializable