package se.skoosh.travellerk

import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import se.skoosh.travellerk.model.Continent
import se.skoosh.travellerk.model.Country
import se.skoosh.travellerk.model.Visit
import se.skoosh.travellerk.service.TravellerService
import kotlin.math.log
import org.slf4j.LoggerFactory

@Controller
class TravellerController(val travellerService: TravellerService) {

    private val logger = LoggerFactory.getLogger(TravellerController::class.java);

    @GetMapping("/")
    fun showVisits(model: Model): String {
        model.addAttribute("visits", travellerService.findVisits())
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
}
