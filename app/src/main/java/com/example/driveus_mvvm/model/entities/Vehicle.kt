package com.example.driveus_mvvm.model.entities

data class Vehicle(var brand : String? = "",
              var color: String? = "",
              var description: String? = "",
              var model: String? = "",
              var seats: Int? = 5,
              var expanded: Boolean = false,
              var delete: Boolean = false) {

    override fun toString(): String {
        return "$color $brand $model ($seats seats)"
    }
}