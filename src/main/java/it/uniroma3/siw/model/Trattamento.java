package it.uniroma3.siw.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

// TRATTAMENTO = il "film": il servizio in se' (manicure, semipermanente, ricostruzione...).
// Prezzo e tema NON stanno qui: dipendono dall'evento e stanno nell'Offerta.
@Entity
public class Trattamento extends EntitaBase {

	@Column(nullable = false, unique = true)
	@NotBlank(message = "Compila il campo vuoto")
	private String nome;

	@NotBlank(message = "Compila il campo vuoto")
	private String categoria;

	// durata in minuti: serve a calcolare quando finisce un appuntamento
	@NotNull(message = "Compila il campo vuoto") @Min(value = 1, message = "La durata deve essere di almeno 1 minuto")
	private Integer durata;

	private String descrizione;

	@OneToMany(mappedBy = "trattamento")
	private List<Offerta> offerte = new ArrayList<>();

	public String getNome() {
		return nome;
	}

	public void setNome(String nome) {
		this.nome = nome;
	}

	public String getCategoria() {
		return categoria;
	}

	public void setCategoria(String categoria) {
		this.categoria = categoria;
	}

	public Integer getDurata() {
		return durata;
	}

	public void setDurata(Integer durata) {
		this.durata = durata;
	}

	public String getDescrizione() {
		return descrizione;
	}

	public void setDescrizione(String descrizione) {
		this.descrizione = descrizione;
	}

	public List<Offerta> getOfferte() {
		return offerte;
	}

	public void setOfferte(List<Offerta> offerte) {
		this.offerte = offerte;
	}

	@Override
	public int hashCode() {
		return Objects.hash(nome);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null || getClass() != obj.getClass())
			return false;
		Trattamento other = (Trattamento) obj;
		return Objects.equals(nome, other.nome);
	}

}
