package se.skoosh.travellerk

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import se.skoosh.travellerk.model.Visit
import se.skoosh.travellerk.model.VisitState
import se.skoosh.travellerk.repository.*
import java.lang.Exception
import java.time.LocalDate
import java.time.Year

@Service
class TravellerService(val continentRepo: ContinentRepo, val countryRepo: CountryRepo, val visitRepo: VisitRepo, val stateRepo: StateRepo, val visitStateRepo: VisitStateRepo) {

    private val logger = LoggerFactory.getLogger(TravellerService::class.java)

    fun findContinents() = continentRepo.all()

    fun findCountries() = countryRepo.all()

    fun pastVisits() = visitRepo.all().filter { it.isInThePast() }
    fun futureVisits() = visitRepo.all().filter { !it.isInThePast() }
    fun pastStateVisits() = visitStateRepo.all().filter { it.isInThePast() }
    fun futureStateVisits() = visitStateRepo.all().filter { !it.isInThePast() }

    fun convertVisits(visits: List<Visit>) = visits
            .map { visit -> VisitView(
                    id = visit.id,
                    startDate = visit.startDate(),
                    endDate = visit.endDate(),
                    name = countryRepo.findById(visit.country).get().name
            ) }
            .sortedWith(compareBy<VisitView> { it.startDate }.thenBy { it.endDate }).reversed()

    fun convertStateVisits(visits: List<VisitState>) = visits
        .map { visit -> VisitView(
            id = visit.id,
            startDate = visit.startDate(),
            endDate = visit.endDate(),
            name = stateRepo.findById(visit.state).get().name
        ) }
        .sortedWith(compareBy<VisitView> { it.startDate }.thenBy { it.endDate }).reversed()

    private fun calculateNumberVisitsForFunc(func: (List<Visit>) -> Int) = pastVisits()
            .groupBy { visit -> visit.country }
            .map { visitsByCountry -> visitsByCountry.key to func(visitsByCountry.value) }
            .groupBy { it.second }
            .map {
                NumberOfVisits(
                        it.key,
                        it.value.map { pair -> countryRepo.findById(pair.first).get().name }.sorted()
                )
            }
            .sortedByDescending { it.number }.toList()

    fun mostVisits(): List<NumberOfVisits> = calculateNumberVisitsForFunc { it.size }

    fun mostDays(): List<NumberOfVisits> = calculateNumberVisitsForFunc { it.sumOf { visit -> visit.visitLength() } }

    fun states(): List<StateView> =
        stateRepo.all().map {
            StateView(it.id, it.name, countryRepo.findById(it.country).get().name)
        }

    fun continentVisitsByCountry(): List<VisitedContinent> {
        val visitedCounties = visitedCountries()
        return continentRepo.all().map { continent ->
            VisitedContinent(
                    continent.name,
                    countryRepo.findByContinent(continent.id).map { country ->
                        VisitedCountry(country.name, visitedCounties.contains(country.id))
                    }.sortedBy { it.country }
            )
        }
    }

    fun countryVisitsByState(): List<VisitedCountryStates> {
        val visitedStates = visitedStates()
        return stateRepo.all().map { it.country }.distinct().map { country ->
            VisitedCountryStates(
                countryRepo.findById(country).get().name,
                stateRepo.findByCountry(country).map { state ->
                    VisitedState(state.name, visitedStates.contains(state.id))
                }.sortedBy { it.state }
            )
        }
    }

    private fun visitedCountries() = pastVisits().map { visit -> visit.country }.distinct()
    private fun visitedStates() = pastStateVisits().map { visit -> visit.state }.distinct()

    fun years(): List<YearView> {
        val visits = pastVisits().sortedBy { it.startDate() }
        val yearRange = visits.minOf { visit -> visit.startYear } .. Year.now().value
        val visitsByYear = yearRange.associateWith { year -> visits.filter { visit -> visit.containsYear(year) } }
        val newVisits = visits.distinctBy { it.country }.groupBy { visit -> visit.startYear }.map { it.key to it.value.size }.toMap()

        return yearRange.reversed().map { year -> YearView(
                year = year,
                totalVisits = visitsByYear.getOrDefault(year, emptyList()).size,
                newVisits = newVisits.getOrDefault(year, 0),
                totalDays = visitsByYear.getOrDefault(year, emptyList()).sumOf { visit -> visit.visitLengthWithinYear(year) },
                distinctCountries = visitsByYear.getOrDefault(year, emptyList()).map { it.country }.toSet().size
        ) }
    }

    fun map(): MapView {
        val pastCountriesById = pastVisits().map { it.country }.distinct()
        val futureCountriesById = futureVisits().map { it.country }.distinct().filter { !pastCountriesById.contains(it) }

        val pastCountries = pastCountriesById.map { countryRepo.findById(it).get().name }
        val futureCountries = futureCountriesById.map { countryRepo.findById(it).get().name }

        return MapView(pastCountries.size + futureCountries.size, pastCountries, futureCountries)
    }

    fun addVisit(countryId: String?, startDate: String?, endDate: String?): String {
        if (countryId == null || startDate == null || endDate == null) return "Input data missing"
        try {
            val countryIdAsInt = Integer.parseInt(countryId)
            val country = countryRepo.findById(countryIdAsInt)
            if (country.isEmpty) return "No country with id $countryIdAsInt"

            val start = LocalDate.parse(startDate)
            val end = LocalDate.parse(endDate)

            val visit = Visit(
                country = countryIdAsInt,
                startDay = start.dayOfMonth,
                startMonth = start.monthValue,
                startYear = start.year,
                endDay = end.dayOfMonth,
                endMonth = end.monthValue,
                endYear = end.year
            )

            visitRepo.save(visit)
            return "Visit added successfully!"
        }
        catch (e: Exception) {
            return e.message ?: "Unknown exception"
        }
    }

    fun addStateVisit(stateId: String?, startDate: String?, endDate: String?): String {
        if (stateId == null || startDate == null || endDate == null) return "Input data missing"
        try {
            val stateIdAsInt = Integer.parseInt(stateId)
            val state = stateRepo.findById(stateIdAsInt)
            if (state.isEmpty) return "No state with id $stateIdAsInt"

            val start = LocalDate.parse(startDate)
            val end = LocalDate.parse(endDate)

            val visit = VisitState(
                state = stateIdAsInt,
                startDay = start.dayOfMonth,
                startMonth = start.monthValue,
                startYear = start.year,
                endDay = end.dayOfMonth,
                endMonth = end.monthValue,
                endYear = end.year
            )

            visitStateRepo.save(visit)
            return "Visit added successfully!"
        }
        catch (e: Exception) {
            return e.message ?: "Unknown exception"
        }
    }

    fun deleteVisit(visitId: String) {
        try {
            val visitIdAsInt = Integer.parseInt(visitId)
            visitRepo.deleteById(visitIdAsInt)
        }
        catch (e: Exception) {
            logger.info("Couldn't delete id=$visitId", e)
        }
    }

    fun deleteStateVisit(visitId: String) {
        try {
            val visitIdAsInt = Integer.parseInt(visitId)
            visitStateRepo.deleteById(visitIdAsInt)
        }
        catch (e: Exception) {
            logger.info("Couldn't delete id=$visitId", e)
        }
    }
}

data class NumberOfVisits(val number: Int, val countries: List<String>)
data class VisitedCountry(val country: String, val visited: Boolean)
data class VisitedState(val state: String, val visited: Boolean)
data class VisitView(val id: Int, val startDate: String, val endDate: String, val name: String)
data class YearView(val year: Int, val totalVisits: Int, val newVisits: Int, val totalDays: Int, val distinctCountries: Int)

data class VisitedContinent(val continent: String, val visitedCountries: List<VisitedCountry>) {
    fun summary() = "" + visitedCountries.filter { it.visited }.size + "/" + visitedCountries.size
}

data class VisitedCountryStates(val country: String, val visitedStates: List<VisitedState>) {
    fun summary() = "" + visitedStates.filter { it.visited }.size + "/" + visitedStates.size
}

data class MapView(val numberOfCountries: Int, val pastCountries: List<String>, val futureCountries: List<String>)

data class StateView(val id: Int, val stateName: String, val countryName: String)