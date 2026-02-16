package com.dh.ondot.schedule.domain

import com.dh.ondot.core.BaseTimeEntity
import jakarta.persistence.*

@Entity
@Table(name = "places")
class Place(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "place_id")
    val id: Long = 0L,

    @Column(name = "title", nullable = false)
    var title: String,

    @Column(name = "road_address", nullable = false)
    var roadAddress: String,

    @Column(name = "longitude", nullable = false)
    var longitude: Double,

    @Column(name = "latitude", nullable = false)
    var latitude: Double,
) : BaseTimeEntity() {

    protected constructor() : this(
        title = "",
        roadAddress = "",
        longitude = 0.0,
        latitude = 0.0,
    )

    fun update(title: String, roadAddress: String, longitude: Double, latitude: Double) {
        this.title = title
        this.roadAddress = roadAddress
        this.longitude = longitude
        this.latitude = latitude
    }

    fun isPlaceChanged(roadAddress: String, longitude: Double, latitude: Double): Boolean =
        this.roadAddress != roadAddress
            || this.longitude.compareTo(longitude) != 0
            || this.latitude.compareTo(latitude) != 0

    companion object {
        @JvmStatic
        fun createPlace(title: String, roadAddress: String, longitude: Double, latitude: Double): Place =
            Place(title = title, roadAddress = roadAddress, longitude = longitude, latitude = latitude)
    }
}
