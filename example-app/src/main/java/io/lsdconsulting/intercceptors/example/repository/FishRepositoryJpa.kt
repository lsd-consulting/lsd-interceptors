package io.lsdconsulting.intercceptors.example.repository

import io.lsdconsulting.intercceptors.example.entity.Fish
import jakarta.transaction.Transactional
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Transactional
@Repository
interface FishRepositoryJpa : CrudRepository<Fish, String> {
    fun findFishByName(name: String?): Fish?
    fun deleteByName(name: String?)
}
