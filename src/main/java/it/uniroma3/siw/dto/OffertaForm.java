package it.uniroma3.siw.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

// Modulo admin "aggiungi un trattamento a un evento": crea una Offerta (evento + trattamento + prezzo + tema).
public class OffertaForm {

	@NotNull(message = "Compila il campo vuoto")
	private Long eventoId;

	@NotNull(message = "Compila il campo vuoto")
	private Long trattamentoId;

	@NotNull(message = "Compila il campo vuoto") @Positive(message = "Il prezzo deve essere maggiore di 0")
	private BigDecimal prezzo;

	@NotBlank(message = "Compila il campo vuoto")
	private String tema;

	private String descrizione;

	public Long getEventoId() {
		return eventoId;
	}

	public void setEventoId(Long eventoId) {
		this.eventoId = eventoId;
	}

	public Long getTrattamentoId() {
		return trattamentoId;
	}

	public void setTrattamentoId(Long trattamentoId) {
		this.trattamentoId = trattamentoId;
	}

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
}
