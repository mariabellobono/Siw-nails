package it.uniroma3.siw.model;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

// OFFERTA = un trattamento proposto in un evento, con prezzo e tema di QUEL evento
// (es. "Semipermanente" a "Nail Week" -> tema Bianco, 35 euro; a "Natale in Rosso" -> tema Natale, 45 euro).
// Serve una classe perche' la relazione molti-a-molti Evento-Trattamento ha dei dati suoi (prezzo, tema):
// una semplice @ManyToMany non puo' contenerli. Stessa idea di Interpretazione (Attore-Film) nel festival:
// due @ManyToOne + gli attributi della relazione. La coppia (evento, trattamento) e' unica.
@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = { "evento_id", "trattamento_id" }))
public class Offerta extends EntitaBase {

	// BigDecimal: e' il tipo giusto per i soldi (double ha errori di arrotondamento)
	@Column(nullable = false, precision = 8, scale = 2)
	@NotNull(message = "Compila il campo vuoto") @Positive(message = "Il prezzo deve essere maggiore di 0")
	private BigDecimal prezzo;

	@NotBlank(message = "Compila il campo vuoto")
	private String tema;

	private String descrizione;

	@ManyToOne
	private Evento evento;

	@ManyToOne
	private Trattamento trattamento;

	// gli appuntamenti si prenotano su una offerta: da li' si ricavano evento, trattamento e prezzo
	@OneToMany(mappedBy = "offerta")
	private List<Appuntamento> appuntamenti = new ArrayList<>();

	public BigDecimal getPrezzo() {
		return prezzo;
	}

	public void setPrezzo(BigDecimal prezzo) {
		this.prezzo = prezzo;
	}

	public String getTema() {
		return tema;
	}

	public void setTema(String tema) {
		this.tema = tema;
	}

	public String getDescrizione() {
		return descrizione;
	}

	public void setDescrizione(String descrizione) {
		this.descrizione = descrizione;
	}

	public Evento getEvento() {
		return evento;
	}

	public void setEvento(Evento evento) {
		this.evento = evento;
	}

	public Trattamento getTrattamento() {
		return trattamento;
	}

	public void setTrattamento(Trattamento trattamento) {
		this.trattamento = trattamento;
	}

	public List<Appuntamento> getAppuntamenti() {
		return appuntamenti;
	}

	public void setAppuntamenti(List<Appuntamento> appuntamenti) {
		this.appuntamenti = appuntamenti;
	}

	@Override
	public int hashCode() {
		return Objects.hash(evento, trattamento);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null || getClass() != obj.getClass())
			return false;
		Offerta other = (Offerta) obj;
		return Objects.equals(evento, other.evento) && Objects.equals(trattamento, other.trattamento);
	}

}
