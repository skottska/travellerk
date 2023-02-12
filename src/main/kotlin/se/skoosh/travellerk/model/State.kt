package se.skoosh.travellerk.model
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table

@Table
data class State(@Id val id: Int, val name: String, val country: Int)