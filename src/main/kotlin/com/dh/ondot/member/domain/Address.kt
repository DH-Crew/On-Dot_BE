package com.dh.ondot.member.domain

import com.dh.ondot.core.BaseTimeEntity
import com.dh.ondot.member.domain.enums.AddressType
import jakarta.persistence.*

@Entity
@Table(
    name = "addresses",
    uniqueConstraints = [
        UniqueConstraint(
            name = "uk_member_type",
            columnNames = ["member_id", "type"]
        )
    ]
)
class Address(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0L,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    val member: Member,

    @Enumerated(EnumType.STRING)
    @Column(name = "type")
    val type: AddressType? = null,

    @Column(name = "name")
    val name: String? = null,

    @Column(name = "road_address", nullable = false)
    var roadAddress: String,

    @Column(name = "longitude", nullable = false)
    var longitude: Double,

    @Column(name = "latitude", nullable = false)
    var latitude: Double,
) : BaseTimeEntity() {

    fun update(roadAddress: String, longitude: Double, latitude: Double) {
        this.roadAddress = roadAddress
        this.longitude = longitude
        this.latitude = latitude
    }

    companion object {
        @JvmStatic
        fun createByOnboarding(member: Member, roadAddress: String, longitude: Double, latitude: Double): Address =
            Address(
                member = member,
                type = AddressType.HOME,
                roadAddress = roadAddress,
                longitude = longitude,
                latitude = latitude,
            )
    }
}
