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
        model.addAttribute("futureVisits", travellerService.convertVisits(travellerService.futureVisits()))
        model.addAttribute("pastVisits", travellerService.convertVisits(travellerService.pastVisits()))
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

    @GetMapping("/delete/{id}")
    fun delete(model: Model, @PathVariable id: String): String {
        travellerService.deleteVisit(id)
        return "redirect:/"
    }
}