package it.uniroma3.siw.model;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

// OPERATRICE = la "sala": la risorsa che deve essere libera quando si prenota.
// In piu' ha un orario di lavoro giornaliero (oraInizio - oraFine): un appuntamento deve starci dentro.
@Entity
public class Operatrice extends EntitaBase {

	@NotBlank(message = "Compila il campo vuoto")
	private String nome;

	@NotBlank(message = "Compila il campo vuoto")
	private String specializzazione;

	@NotNull(message = "Compila il campo vuoto")
	private LocalTime oraInizio;

	@NotNull(message = "Compila il campo vuoto")
	private LocalTime oraFine;

	@OneToMany(mappedBy = "operatrice")
	private List<Appuntamento> appuntamenti = new ArrayList<>();

	public String getNome() {
		return nome;
	}

	public void setNome(String nome) {
		this.nome = nome;
	}

	public String getSpecializzazione() {
		return specializzazione;
	}

	public void setSpecializzazione(String specializzazione) {
		this.specializzazione = specializzazione;
	}

	public LocalTime getOraInizio() {
		return oraInizio;
	}

	public void setOraInizio(LocalTime oraInizio) {
		this.oraInizio = oraInizio;
	}

	public LocalTime getOraFine() {
		return oraFine;
	}

	public void setOraFine(LocalTime oraFine) {
		this.oraFine = oraFine;
	}

	public List<Appuntamento> getAppuntamenti() {
		return appuntamenti;
	}

	public void setAppuntamenti(List<Appuntamento> appuntamenti) {
		this.appuntamenti = appuntamenti;
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
		Operatrice other = (Operatrice) obj;
		return Objects.equals(nome, other.nome);
	}

}
