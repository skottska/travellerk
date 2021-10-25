package se.skoosh.travellerk.repository

import org.springframework.data.jdbc.repository.query.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.query.Param
import se.skoosh.travellerk.model.Continent
import se.skoosh.travellerk.model.Country
import se.skoosh.travellerk.model.Visit

interface VisitRepo : CrudRepository<Visit, Int>{

    @Query("select * from visit order by start_year, start_month, start_day")
    fun all(): List<Visit>

    @Query("select * from visit where country = :country order by start_year, start_month, start_day")
    fun getVisitsByCountry(@Param("country") country: Int): List<Visit>

}