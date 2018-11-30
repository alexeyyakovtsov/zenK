package com.kicker.api.service

import com.kicker.api.domain.PageRequest
import com.kicker.api.model.base.BaseModel
import com.kicker.api.repository.BaseRepository
import org.springframework.data.domain.Page
import org.springframework.transaction.annotation.Transactional

/**
 * @author Yauheni Efimenko
 */
@Transactional(readOnly = true)
abstract class DefaultBaseService<T : BaseModel, R : BaseRepository<T>>(
        private val repository: R
) : BaseService<T> {

    override fun get(id: Long): T = repository.findById(id).get()

    override fun getAll(): List<T> = repository.findAll()

    override fun getAll(pageRequest: PageRequest): Page<T> = repository.findAll(pageRequest)

    @Transactional
    override fun save(entity: T): T = repository.save(entity)

}