package se.skoosh.travellerk.model
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import java.time.LocalDate
import java.time.Year
import java.time.temporal.ChronoUnit
import java.time.temporal.TemporalAdjuster
import java.time.temporal.TemporalAdjusters

@Table
data class Visit(@Id val id: Int, val country: Int,
                 val startDay: Int, val startMonth: Int, val startYear: Int,
                 val endDay: Int, val endMonth: Int, val endYear: Int
                 )
{
    fun startDate() = dateToString(startYear, startMonth, startDay);
    fun startLocalDate(): LocalDate = LocalDate.parse(startDate())

    fun endDate() = dateToString(endYear, endMonth, endDay);
    fun endLocalDate(): LocalDate = LocalDate.parse(endDate())

    fun visitLength() = when {
        hasMissingField() -> 0
        else -> ChronoUnit.DAYS.between(startLocalDate(), endLocalDate()).toInt() + 1
    }

    fun containsYear(year: Int) = year in startYear..endYear

    fun visitLengthWithinYear(year: Int) = when {
        hasMissingField() || !containsYear(year) -> 0
        else -> ChronoUnit.DAYS.between(maxStart(year), minEnd(year)).toInt() + 1
    }

    private fun maxStart(year: Int): LocalDate {
        val firstDayOfYear = Year.of(year).atDay(1);
        return if (firstDayOfYear.isAfter(startLocalDate())) firstDayOfYear else startLocalDate()
    }

    private fun minEnd(year: Int): LocalDate {
        val lastDayOfYear = Year.of(year).atDay(1).with(TemporalAdjusters.lastDayOfYear());
        return if (lastDayOfYear.isBefore(endLocalDate())) lastDayOfYear else endLocalDate()
    }

    private fun hasMissingField() = startMonth == -1 || startDay == -1 || endMonth == -1 || endDay == -1

    private fun dateToString(year: Int, month: Int, day: Int) = "" + year + datePartToString(month) + datePartToString(day)

    private fun datePartToString(datePart: Int) = when(datePart) {
        -1 -> ""
        in 1 .. 9 -> "-0$datePart"
        else -> "-$datePart"
    }
}