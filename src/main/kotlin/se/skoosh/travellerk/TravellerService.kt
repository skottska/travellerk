package se.skoosh.travellerk

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import se.skoosh.travellerk.model.Visit
import se.skoosh.travellerk.repository.ContinentRepo
import se.skoosh.travellerk.repository.CountryRepo
import se.skoosh.travellerk.repository.VisitRepo
import java.lang.Exception
import java.time.LocalDate
import java.time.Year

@Service
class TravellerService(val continentRepo: ContinentRepo, val countryRepo: CountryRepo, val visitRepo: VisitRepo) {

    private val logger = LoggerFactory.getLogger(TravellerService::class.java)

    fun findContinents() = continentRepo.all()

    fun findCountries() = countryRepo.all()

    fun pastVisits() = visitRepo.all().filter { it.isInThePast() }
    fun futureVisits() = visitRepo.all().filter { !it.isInThePast() }

    fun convertVisits(visits: List<Visit>) = visits
            .map { visit -> VisitView(
                    id = visit.id,
                    startDate = visit.startDate(),
                    endDate = visit.endDate(),
                    country = countryRepo.findById(visit.country).get().name
            ) }
            .sortedByDescending { it.startDate }

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

    private fun visitedCountries() = pastVisits().map { visit -> visit.country }.distinct()

    fun years(): List<YearView> {
        val visits = pastVisits().sortedBy { it.startDate() }
        val yearRange = visits.minOf { visit -> visit.startYear } .. Year.now().value
        val visitsByYear = yearRange.associateWith { year -> visits.filter { visit -> visit.containsYear(year) } }
        val newVisits = visits.distinctBy { it.country }.groupBy { visit -> visit.startYear }.map { it.key to it.value.size }.toMap()

        return yearRange.reversed().map { year -> YearView(
                year = year,
                totalVisits = visitsByYear.getOrDefault(year, emptyList()).size,
                newVisits = newVisits.getOrDefault(year, 0),
                totalDays = visitsByYear.getOrDefault(year, emptyList()).sumOf { visit -> visit.visitLengthWithinYear(year) }
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

    fun deleteVisit(visitId: String) {
        try {
            val visitIdAsInt = Integer.parseInt(visitId)
            visitRepo.deleteById(visitIdAsInt)
        }
        catch (e: Exception) {
            logger.info("Couldn't delete id=$visitId", e)
        }
    }
}

data class NumberOfVisits(val number: Int, val countries: List<String>)
data class VisitedCountry(val country: String, val visited: Boolean)
data class VisitView(val id: Int, val startDate: String, val endDate: String, val country: String)
data class YearView(val year: Int, val totalVisits: Int, val newVisits: Int, val totalDays: Int)

data class VisitedContinent(val continent: String, val visitedCountries: List<VisitedCountry>) {
    fun summary() = "" + visitedCountries.filter { it.visited }.size + "/" + visitedCountries.size
}

data class MapView(val numberOfCountries: Int, val pastCountries: List<String>, val futureCountries: List<String>)
