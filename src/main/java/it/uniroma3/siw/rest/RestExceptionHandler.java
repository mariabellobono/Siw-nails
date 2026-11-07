package it.uniroma3.siw.rest;

import java.util.LinkedHashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.dao.ConcurrencyFailureException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import it.uniroma3.siw.dto.ApiDto.ErroreDto;
import it.uniroma3.siw.exception.AppuntamentoNotFoundException;
import it.uniroma3.siw.exception.EventoNotFoundException;
import it.uniroma3.siw.exception.OffertaNotFoundException;
import it.uniroma3.siw.exception.OperatriceNotFoundException;
import it.uniroma3.siw.exception.OperazioneNonConsentitaException;
import it.uniroma3.siw.exception.PrenotazioneNonValidaException;
import it.uniroma3.siw.exception.RecensioneNotFoundException;
import it.uniroma3.siw.exception.TrattamentoNotFoundException;

// Gestione ERRORI dell'API: ogni eccezione diventa una risposta JSON con il codice HTTP giusto.
// basePackages: vale SOLO per i controller del package "rest" (le pagine HTML continuano a usare
// AllExceptionHandler, che risponderebbe con una pagina HTML e con 500 a quasi tutto).
// @Order(HIGHEST_PRECEDENCE): questo handler viene consultato PRIMA di AllExceptionHandler.
@RestControllerAdvice(basePackages = "it.uniroma3.siw.rest")
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RestExceptionHandler {

	private static final Logger log = LoggerFactory.getLogger(RestExceptionHandler.class);

	// 404 - la risorsa richiesta non esiste
	@ExceptionHandler({ EventoNotFoundException.class, TrattamentoNotFoundException.class,
			OperatriceNotFoundException.class, OffertaNotFoundException.class, AppuntamentoNotFoundException.class,
			RecensioneNotFoundException.class })
	public ResponseEntity<ErroreDto> nonTrovato(RuntimeException ex) {
		return risposta(HttpStatus.NOT_FOUND, ex.getMessage(), null);
	}

	// 400 - i dati del corpo non rispettano i vincoli (@Valid): si elencano i campi sbagliati
	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ErroreDto> validazione(MethodArgumentNotValidException ex) {
		Map<String, String> campi = new LinkedHashMap<>();
		ex.getBindingResult().getFieldErrors().forEach(e -> campi.putIfAbsent(e.getField(), e.getDefaultMessage()));
		return risposta(HttpStatus.BAD_REQUEST, "Dati non validi", campi);
	}

	// 400 - JSON malformato, campo del tipo sbagliato (es. una data scritta male), parametro non numerico
	@ExceptionHandler({ HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class,
			IllegalArgumentException.class })
	public ResponseEntity<ErroreDto> richiestaNonValida(Exception ex) {
		String messaggio = (ex instanceof IllegalArgumentException) ? ex.getMessage()
				: "Richiesta non valida: controlla formato e tipo dei dati";
		return risposta(HttpStatus.BAD_REQUEST, messaggio, null);
	}

	// 401 - login con credenziali sbagliate
	@ExceptionHandler(BadCredentialsException.class)
	public ResponseEntity<ErroreDto> credenzialiErrate(BadCredentialsException ex) {
		return risposta(HttpStatus.UNAUTHORIZED, "Username o password errati", null);
	}

	// 403 - utente collegato ma senza il permesso (es. sposta l'appuntamento di un'altra cliente)
	@ExceptionHandler(AccessDeniedException.class)
	public ResponseEntity<ErroreDto> vietato(AccessDeniedException ex) {
		return risposta(HttpStatus.FORBIDDEN, ex.getMessage(), null);
	}

	// 409 - l'operazione va in conflitto con lo stato attuale (operatrice occupata, fuori orario, gia' recensito...)
	@ExceptionHandler({ OperazioneNonConsentitaException.class, PrenotazioneNonValidaException.class })
	public ResponseEntity<ErroreDto> conflitto(RuntimeException ex) {
		return risposta(HttpStatus.CONFLICT, ex.getMessage(), null);
	}

	// 409 - vincolo unique/foreign key violato dal database (ultima difesa)
	@ExceptionHandler(DataIntegrityViolationException.class)
	public ResponseEntity<ErroreDto> vincoloDatabase(DataIntegrityViolationException ex) {
		log.warn("Violazione di un vincolo del database", ex);
		return risposta(HttpStatus.CONFLICT, "Operazione in conflitto con i dati esistenti", null);
	}

	// 409 - due transazioni SERIALIZABLE in conflitto (due prenotazioni sulla stessa operatrice): si puo' riprovare
	@ExceptionHandler(ConcurrencyFailureException.class)
	public ResponseEntity<ErroreDto> concorrenza(ConcurrencyFailureException ex) {
		return risposta(HttpStatus.CONFLICT, "Un'altra prenotazione e in corso sulla stessa operatrice: riprova", null);
	}

	// Tutto il resto. Le eccezioni standard di Spring MVC (metodo non consentito = 405, tipo di contenuto
	// non supportato = 415, parametro mancante = 400...) portano con se' il loro codice HTTP: lo rispettiamo.
	// Qualsiasi altro errore e' un imprevisto: 500, all'esterno nessun dettaglio tecnico, nel log si'.
	@ExceptionHandler(Exception.class)
	public ResponseEntity<ErroreDto> imprevisto(Exception ex) {
		if (ex instanceof ErrorResponse errore) {
			HttpStatus status = HttpStatus.valueOf(errore.getStatusCode().value());
			return risposta(status, status.getReasonPhrase(), null);
		}
		log.error("Errore imprevisto nell'API", ex);
		return risposta(HttpStatus.INTERNAL_SERVER_ERROR, "Errore interno del server", null);
	}

	private ResponseEntity<ErroreDto> risposta(HttpStatus status, String messaggio, Map<String, String> campi) {
		ErroreDto corpo = new ErroreDto(status.value(), status.getReasonPhrase(), messaggio,
				campi == null ? Map.of() : campi);
		return ResponseEntity.status(status).body(corpo);
	}
}
