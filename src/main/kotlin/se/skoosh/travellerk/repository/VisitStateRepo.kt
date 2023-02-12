package se.skoosh.travellerk.repository

import org.springframework.data.jdbc.repository.query.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.query.Param
import se.skoosh.travellerk.model.Visit
import se.skoosh.travellerk.model.VisitState

interface VisitStateRepo : CrudRepository<VisitState, Int>{

    @Query("select * from visit_state order by start_year, start_month, start_day")
    fun all(): List<VisitState>

    @Query("select * from visit_state where state = :state order by start_year, start_month, start_day")
    fun getVisitsByState(@Param("state") state: Int): List<VisitState>

}