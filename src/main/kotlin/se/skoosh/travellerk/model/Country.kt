package se.skoosh.travellerk.model
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table

@Table
data class Country(@Id val id: Int, val name: String, val continent: Int)