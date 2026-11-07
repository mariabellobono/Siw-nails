package it.uniroma3.siw.model;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.ManyToOne;
import jakarta.validation.constraints.NotNull;

// APPUNTAMENTO = la "proiezione": una cliente, una offerta (quindi evento + trattamento + prezzo),
// un'operatrice, una data e un'ora. Evento e trattamento NON si salvano due volte: si ricavano dall'offerta,
// cosi' non possono andare fuori sincrono.
@Entity
public class Appuntamento extends EntitaBase {

	@Column(nullable = false)
	@NotNull
	private LocalDate data;

	@Column(nullable = false)
	@NotNull
	private LocalTime ora;

	@Enumerated(EnumType.STRING)
	private Stato stato;

	private String note;

	@ManyToOne
	private Offerta offerta;

	@ManyToOne
	private Operatrice operatrice;

	// la cliente che ha prenotato
	@ManyToOne
	private Utente utente;

	public LocalDate getData() {
		return data;
	}

	public void setData(LocalDate data) {
		this.data = data;
	}

	public LocalTime getOra() {
		return ora;
	}

	public void setOra(LocalTime ora) {
		this.ora = ora;
	}

	public Stato getStato() {
		return stato;
	}

	public void setStato(Stato stato) {
		this.stato = stato;
	}

	public String getNote() {
		return note;
	}

	public void setNote(String note) {
		this.note = note;
	}

	public Offerta getOfferta() {
		return offerta;
	}

	public void setOfferta(Offerta offerta) {
		this.offerta = offerta;
	}

	public Operatrice getOperatrice() {
		return operatrice;
	}

	public void setOperatrice(Operatrice operatrice) {
		this.operatrice = operatrice;
	}

	public Utente getUtente() {
		return utente;
	}

	public void setUtente(Utente utente) {
		this.utente = utente;
	}

	@Override
	public int hashCode() {
		return Objects.hash(operatrice, data, ora);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null || getClass() != obj.getClass())
			return false;
		Appuntamento other = (Appuntamento) obj;
		return Objects.equals(operatrice, other.operatrice) && Objects.equals(data, other.data)
				&& Objects.equals(ora, other.ora);
	}

}
