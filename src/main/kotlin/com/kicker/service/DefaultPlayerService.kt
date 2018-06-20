package com.kicker.service

import com.kicker.domain.model.player.CreatePlayerRequest
import com.kicker.domain.model.player.UpdateDataPlayerRequest
import com.kicker.domain.model.player.UpdatePasswordPlayerRequest
import com.kicker.exception.service.DuplicateUsernameException
import com.kicker.exception.service.PasswordIncorrectException
import com.kicker.model.Player
import com.kicker.repository.PlayerRepository
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * @author Yauheni Efimenko
 */
@Service
@Transactional(readOnly = true)
class DefaultPlayerService(
        private val repository: PlayerRepository,
        private val passwordEncoder: PasswordEncoder
) : DefaultBaseService<Player, PlayerRepository>(repository), PlayerService {

    override fun getByUsername(username: String): Player? = repository.findByUsername(username)

    override fun loadUserByUsername(username: String): UserDetails = getByUsername(username)
            ?: throw UsernameNotFoundException("User with username $username not found")

    @Transactional
    override fun create(request: CreatePlayerRequest): Player {
        if (isExist(request.username!!)) {
            throw DuplicateUsernameException("The player with such username already exist")
        }

        request.password = passwordEncoder.encode(request.password)

        return repository.save(Player.of(request))
    }

    @Transactional
    override fun updateData(currentPlayer: Player, request: UpdateDataPlayerRequest): Player {
        if (isExist(request.username!!)) {
            throw DuplicateUsernameException("The player with such username already exist")
        }

        currentPlayer.username = request.username!!
        currentPlayer.firstName = request.firstName!!
        currentPlayer.lastName = request.lastName!!

        return repository.save(currentPlayer)
    }

    @Transactional
    override fun updatePassword(currentPlayer: Player, request: UpdatePasswordPlayerRequest): Player {
        if (!passwordEncoder.matches(request.currentPassword, currentPlayer.password)) {
            throw PasswordIncorrectException("Password is incorrect")
        }

        currentPlayer.password = passwordEncoder.encode(request.newPassword)

        return repository.save(currentPlayer)
    }

    private fun isExist(username: String): Boolean {
        return getByUsername(username)?.let { true } ?: false
    }

}