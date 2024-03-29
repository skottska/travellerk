package se.skoosh.travellerk

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.*

@Controller
class TravellerController(val travellerService: TravellerService) {

    private val logger = LoggerFactory.getLogger(TravellerController::class.java)

    @GetMapping("/")
    fun showVisits(model: Model): String {
        val future = travellerService.futureVisits()
        val past = travellerService.pastVisits()

        val pastCountries = past.map { visit -> visit.country }.distinct()
        val futureCountries = future.map { visit -> visit.country }.distinct().filter { !pastCountries.contains(it) }

        model.addAttribute("numFuture", futureCountries.size)
        model.addAttribute("numPast", pastCountries.size)
        model.addAttribute("futureVisits", travellerService.convertVisits(future))
        model.addAttribute("pastVisits", travellerService.convertVisits(past))
        return "index"
    }

    @GetMapping("/mostVisits")
    fun mostVisits(model: Model): String {
        model.addAttribute("mostVisits", travellerService.mostVisits())
        return "mostVisits"
    }

    @GetMapping("/mostDays")
    fun mostDays(model: Model): String {
        model.addAttribute("mostVisits", travellerService.mostDays())
        return "mostDays"
    }

    @GetMapping("/continents")
    fun continentVisitsByCountry(model: Model): String {
        model.addAttribute("continents", travellerService.continentVisitsByCountry())
        return "continents"
    }

    @GetMapping("/states")
    fun countryVisitsByStates(model: Model): String {
        model.addAttribute("countries", travellerService.countryVisitsByState())
        return "states"
    }

    @GetMapping("/stateVisits")
    fun states(model: Model): String {
        val future = travellerService.futureStateVisits()
        val past = travellerService.pastStateVisits()

        val pastStates = past.map { visit -> visit.state }.distinct()
        val futureStates = future.map { visit -> visit.state }.distinct().filter { !pastStates.contains(it) }

        model.addAttribute("numFuture", futureStates.size)
        model.addAttribute("numPast", pastStates.size)
        model.addAttribute("futureVisits", travellerService.convertStateVisits(future))
        model.addAttribute("pastVisits", travellerService.convertStateVisits(past))
        return "stateVisits"
    }

    @GetMapping("/years")
    fun years(model: Model): String {
        model.addAttribute("years", travellerService.years())
        return "years"
    }

    @GetMapping("/map")
    fun map(model: Model): String {
        model.addAttribute("map", travellerService.map())
        return "map"
    }

    @GetMapping("/add")
    fun add(model: Model): String {
        model.addAttribute("countries", travellerService.findCountries())
        return "add"
    }

    @PostMapping("/add")
    fun addPost(model: Model, @RequestParam body: Map<String, String>): String {
        model.addAttribute("countries", travellerService.findCountries())
        model.addAttribute("result", travellerService.addVisit(body["country"], body["startDate"], body["endDate"]))
        return "add"
    }

    @GetMapping("/addState")
    fun addState(model: Model): String {
        model.addAttribute("states", travellerService.states())
        return "addState"
    }

    @PostMapping("/addState")
    fun addStatePost(model: Model, @RequestParam body: Map<String, String>): String {
        model.addAttribute("states", travellerService.states())
        model.addAttribute("result", travellerService.addStateVisit(body["state"], body["startDate"], body["endDate"]))
        return "addState"
    }

    @GetMapping("/delete/{id}")
    fun delete(model: Model, @PathVariable id: String): String {
        travellerService.deleteVisit(id)
        return "redirect:/"
    }

    @GetMapping("/deleteStateVisit/{id}")
    fun deleteStateVisit(model: Model, @PathVariable id: String): String {
        travellerService.deleteStateVisit(id)
        return "redirect:/"
    }
}