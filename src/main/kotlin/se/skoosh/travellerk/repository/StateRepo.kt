package se.skoosh.travellerk.repository

import org.springframework.data.jdbc.repository.query.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.query.Param
import se.skoosh.travellerk.model.Country
import se.skoosh.travellerk.model.State

interface StateRepo : CrudRepository<State, Int>{

    @Query("select * from state order by name")
    fun all(): List<State>

    @Query("select * from state where country = :country order by name")
    fun findByCountry(@Param("country") country: Int): List<State>


    @Query("select c.* from state c join visit_state v on v.state = c.id")
    fun visitedStates(): List<State>

}