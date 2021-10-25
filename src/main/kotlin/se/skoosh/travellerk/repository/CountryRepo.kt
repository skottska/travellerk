package se.skoosh.travellerk.repository

import org.springframework.data.jdbc.repository.query.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.query.Param
import se.skoosh.travellerk.model.Country

interface CountryRepo : CrudRepository<Country, Int>{

    @Query("select * from country order by name")
    fun all(): List<Country>

    @Query("select * from country where continent = :continent order by name")
    fun findByContinent(@Param("continent") continent: Int): List<Country>


    @Query("select c.* from country c join visit v on v.country = c.id")
    fun visitedCountries(): List<Country>

}