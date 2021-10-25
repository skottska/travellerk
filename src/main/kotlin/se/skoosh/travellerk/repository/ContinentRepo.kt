package se.skoosh.travellerk.repository

import org.springframework.data.jdbc.repository.query.Query
import org.springframework.data.repository.CrudRepository
import se.skoosh.travellerk.model.Continent

interface ContinentRepo : CrudRepository<Continent, Int>{

    @Query("select * from continent order by name")
    fun all(): List<Continent>
}