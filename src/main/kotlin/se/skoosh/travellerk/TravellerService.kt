package se.skoosh.travellerk.service

import org.springframework.data.annotation.Id
import org.springframework.stereotype.Service
import se.skoosh.travellerk.model.Country
import se.skoosh.travellerk.model.Visit
import se.skoosh.travellerk.repository.ContinentRepo
import se.skoosh.travellerk.repository.CountryRepo
import se.skoosh.travellerk.repository.VisitRepo
import java.time.Year

@Service
class TravellerService(val continentRepo: ContinentRepo, val countryRepo: CountryRepo, val visitRepo: VisitRepo) {

    fun findContinents() = continentRepo.all()

    fun findCountries() = countryRepo.all()

    fun findVisits() = visitRepo.all()
            .map { visit -> VisitView(
                    id = visit.id,
                    startDate = visit.startDate(),
                    endDate = visit.endDate(),
                    country = countryRepo.findById(visit.country).get().name
            ) }
            .sortedByDescending { it.startDate }

    private fun calculateNumberVisitsForFunc(func: (List<Visit>) -> Int) = visitRepo.all()
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

    private fun visitedCountries() = visitRepo.all().map { visit -> visit.country }.distinct()

    fun years(): List<YearView> {
        val visits = visitRepo.all().sortedBy { it.startDate() }
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
        val visitedCountries = countryRepo.visitedCountries()
        return MapView(visitedCountries.size, visitedCountries.map { it. name })
    }
}

data class NumberOfVisits(val number: Int, val countries: List<String>)
data class VisitedCountry(val country: String, val visited: Boolean)
data class VisitView(val id: Int, val startDate: String, val endDate: String, val country: String)
data class YearView(val year: Int, val totalVisits: Int, val newVisits: Int, val totalDays: Int)

data class VisitedContinent(val continent: String, val visitedCountries: List<VisitedCountry>) {
    fun summary() = "" + visitedCountries.filter { it.visited }.size + "/" + visitedCountries.size
}

data class MapView(val numberOfCountries: Int, val countries: List<String>)
