package it.uniroma3.siw.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

// Tutti gli oggetti che viaggiano nell'API REST (il JSON) stanno qui, in un solo file.
// Perche' DTO e non le entita': le entita' hanno liste che si richiamano a vicenda
// (Evento -> Offerta -> Evento ...) e il JSON andrebbe in ciclo infinito; inoltre toccherebbero
// collezioni LAZY fuori dalla transazione. Un DTO contiene solo i campi che vogliamo mostrare.
// Un "record" e' una classe immutabile con costruttore, getter, equals e toString gia' pronti.
public final class ApiDto {

	private ApiDto() {
	}

	// ---------- RISPOSTE (cosa esce dall'API) ----------

	public record EventoDto(Long id, String nome, Integer anno, String citta, LocalDate dataInizio,
			LocalDate dataFine, String descrizione, String immagine) {
	}

	// Un trattamento in un evento, con prezzo e tema di QUELL'evento
	public record OffertaDto(Long id, String tema, BigDecimal prezzo, String descrizione, Long eventoId,
			String eventoNome, Long trattamentoId, String trattamentoNome, Integer durata) {
	}

	public record EventoDettaglioDto(Long id, String nome, Integer anno, String citta, LocalDate dataInizio,
			LocalDate dataFine, String descrizione, String immagine, List<OffertaDto> offerte) {
	}

	public record TrattamentoDto(Long id, String nome, String categoria, Integer durata, String descrizione) {
	}

	// Dettaglio: in piu' la media dei voti, il numero di recensioni e dove e a che prezzo e' proposto
	public record TrattamentoDettaglioDto(Long id, String nome, String categoria, Integer durata, String descrizione,
			Double mediaVoti, long numeroRecensioni, List<OffertaDto> offerte) {
	}

	public record OperatriceDto(Long id, String nome, String specializzazione, LocalTime oraInizio,
			LocalTime oraFine) {
	}

	public record RecensioneDto(Long id, String testo, Integer voto, LocalDate data, Long appuntamentoId,
			Long trattamentoId, String autore) {
	}

	// L'appuntamento NON espone la cliente (la pagina e' pubblica). "recensione" c'e' solo in /api/miei-appuntamenti.
	public record AppuntamentoDto(Long id, LocalDate data, LocalTime ora, String stato, Long offertaId, Long eventoId,
			String eventoNome, Long trattamentoId, String trattamentoNome, String tema, BigDecimal prezzo,
			Integer durata, Long operatriceId, String operatriceNome, String note, RecensioneDto recensione) {
	}

	// Risposta di /api/auth/me: chi e' collegato (authenticated=false se nessuno)
	public record UtenteCorrenteDto(boolean authenticated, String username, Long utenteId, String nome,
			boolean admin) {
	}

	// Formato unico degli errori: status HTTP, titolo, messaggio e (per la validazione) campo -> messaggio
	public record ErroreDto(int status, String errore, String messaggio, java.util.Map<String, String> campi) {
	}

	// ---------- RICHIESTE (cosa entra nell'API) ----------
	// Le annotazioni di validazione sono le stesse delle entita' e dei moduli del sito.

	public record LoginRequest(@NotBlank(message = "Compila il campo vuoto") String username,
			@NotBlank(message = "Compila il campo vuoto") String password) {
	}

	// Prenotare o spostare un appuntamento. La cliente NON e' nel corpo: e' l'utente collegato.
	public record PrenotazioneRequest(
			@NotNull(message = "Compila il campo vuoto") Long trattamentoId,
			@NotNull(message = "Compila il campo vuoto") Long operatriceId,
			@NotNull(message = "Compila il campo vuoto") LocalDate data,
			@NotNull(message = "Compila il campo vuoto") LocalTime ora,
			@Size(max = 255, message = "Massimo 255 caratteri") String note) {
	}

	public record RecensioneRequest(
			@NotBlank(message = "Compila il campo vuoto") @Size(max = 255, message = "Massimo 255 caratteri") String testo,
			@NotNull(message = "Compila il campo vuoto") @Min(value = 1, message = "Il voto deve essere compreso tra 1 e 5") @Max(value = 5, message = "Il voto deve essere compreso tra 1 e 5") Integer voto) {
	}
}
