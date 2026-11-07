package it.uniroma3.siw.dto;

import java.time.LocalDate;
import java.time.LocalTime;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

// Oggetto del modulo "prenota": contiene solo i valori inviati dal browser.
// Dalle tendine arrivano gli id (Long), non gli oggetti: sara' il service a caricare le entita'.
// Si sceglie il TRATTAMENTO: evento, tema e prezzo NON si chiedono, il service li ricava dalla data scelta.
public class PrenotazioneForm {

	@NotNull(message = "Compila il campo vuoto")
	private Long trattamentoId;

	@NotNull(message = "Compila il campo vuoto")
	private Long operatriceId;

	@NotNull(message = "Compila il campo vuoto")
	private LocalDate data;

	@NotNull(message = "Compila il campo vuoto")
	private LocalTime ora;

	@Size(max = 255, message = "Massimo 255 caratteri")
	private String note;

	public Long getTrattamentoId() {
		return trattamentoId;
	}

	public void setTrattamentoId(Long trattamentoId) {
		this.trattamentoId = trattamentoId;
	}

	public Long getOperatriceId() {
		return operatriceId;
	}

	public void setOperatriceId(Long operatriceId) {
		this.operatriceId = operatriceId;
	}

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

	public String getNote() {
		return note;
	}

	public void setNote(String note) {
		this.note = note;
	}
}
