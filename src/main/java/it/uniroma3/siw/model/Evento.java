package it.uniroma3.siw.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

// EVENTO = il "festival" del salone: un periodo, una citta' e un programma di trattamenti
// (es. "Nail Week Roma", "Natale in Rosso"). Gli appuntamenti si prenotano dentro un evento.
@Entity
public class Evento extends EntitaBase {

	@Column(nullable = false)
	@NotBlank(message = "Compila il campo vuoto")
	private String nome;

	@NotNull(message = "Compila il campo vuoto") @Min(value = 1900, message = "Anno non valido") @Max(value = 2100, message = "Anno non valido")
	private Integer anno;

	@Column(nullable = false)
	@NotBlank(message = "Compila il campo vuoto")
	private String citta;

	@NotNull(message = "Compila il campo vuoto")
	private LocalDate dataInizio;

	@NotNull(message = "Compila il campo vuoto")
	private LocalDate dataFine;

	private String descrizione;

	// I trattamenti dell'evento NON sono una semplice lista di Trattamento: ogni trattamento, in ogni evento,
	// ha un prezzo e un tema suoi. Per questo la relazione e' una classe, Offerta (come Interpretazione nel festival).
	// Lista inizializzata: cosi' un oggetto creato con "new" ha una lista pronta e non null.
	@OneToMany(mappedBy = "evento")
	private List<Offerta> offerte = new ArrayList<>();

	public String getNome() {
		return nome;
	}

	public void setNome(String nome) {
		this.nome = nome;
	}

	public Integer getAnno() {
		return anno;
	}

	public void setAnno(Integer anno) {
		this.anno = anno;
	}

	public String getCitta() {
		return citta;
	}

	public void setCitta(String citta) {
		this.citta = citta;
	}

	public LocalDate getDataInizio() {
		return dataInizio;
	}

	public void setDataInizio(LocalDate dataInizio) {
		this.dataInizio = dataInizio;
	}

	public LocalDate getDataFine() {
		return dataFine;
	}

	public void setDataFine(LocalDate dataFine) {
		this.dataFine = dataFine;
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
		return Objects.hash(nome, anno);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null || getClass() != obj.getClass())
			return false;
		Evento other = (Evento) obj;
		return Objects.equals(nome, other.nome) && Objects.equals(anno, other.anno);
	}

}
