package com.kicker.model

import com.kicker.domain.model.player.CreatePlayerRequest
import com.kicker.model.base.BaseModel
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import javax.persistence.*

/**
 * @author Yauheni Efimenko
 */
@Entity
@Table(name = "players")
class Player(

        @Column(name = "username", nullable = false, unique = true)
        private var username: String,

        @Column(name = "password", nullable = false)
        private var password: String,

        @Column(name = "first_name", nullable = false)
        var firstName: String,

        @Column(name = "last_name", nullable = false)
        var lastName: String,

        @Column(name = "active", nullable = false)
        var active: Boolean = false,

        @Column(name = "current_rating", nullable = false)
        var currentRating: Double = INITIAL_RATING

) : BaseModel(), UserDetails {

    companion object {
        const val INITIAL_RATING: Double = 10000.0

        fun of(createPlayerRequest: CreatePlayerRequest): Player = Player(
                createPlayerRequest.username!!,
                createPlayerRequest.password!!,
                createPlayerRequest.firstName!!,
                createPlayerRequest.lastName!!)
    }


    override fun getAuthorities(): MutableCollection<out GrantedAuthority> = mutableListOf()

    override fun isEnabled(): Boolean = true

    override fun getUsername(): String = username

    override fun isCredentialsNonExpired(): Boolean = true

    override fun getPassword(): String = password

    override fun isAccountNonExpired(): Boolean = true

    override fun isAccountNonLocked(): Boolean = true

    fun setUsername(username: String) {
        this.username = username
    }

    fun setPassword(password: String) {
        this.password = password
    }

}