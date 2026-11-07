package it.uniroma3.siw.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import it.uniroma3.siw.exception.AppuntamentoNotFoundException;
import it.uniroma3.siw.exception.EventoNotFoundException;
import it.uniroma3.siw.exception.OffertaNotFoundException;
import it.uniroma3.siw.exception.OperatriceNotFoundException;
import it.uniroma3.siw.exception.RecensioneNotFoundException;
import it.uniroma3.siw.exception.TrattamentoNotFoundException;

// Una sola classe che intercetta le eccezioni di TUTTI i controller (invece di un try/catch in ognuno).
@ControllerAdvice
public class AllExceptionHandler {

	private static final Logger log = LoggerFactory.getLogger(AllExceptionHandler.class);

	// Una risorsa richiesta non esiste: risposta HTTP 404 (non 200) con la pagina error/404
	@ExceptionHandler({ EventoNotFoundException.class, TrattamentoNotFoundException.class,
			OffertaNotFoundException.class, OperatriceNotFoundException.class, AppuntamentoNotFoundException.class,
			RecensioneNotFoundException.class })
	@ResponseStatus(HttpStatus.NOT_FOUND)
	public String risorsaNonTrovata(RuntimeException ex, Model model) {
		model.addAttribute("messaggio", ex.getMessage());
		return "error/404";
	}

	// Indirizzo inesistente (es. /pippo)
	@ExceptionHandler(NoResourceFoundException.class)
	@ResponseStatus(HttpStatus.NOT_FOUND)
	public String paginaNonTrovata(Model model) {
		model.addAttribute("messaggio", "La pagina richiesta non esiste.");
		return "error/404";
	}

	// Qualsiasi altro errore imprevisto: all'utente una pagina generica, il dettaglio nel log.
	// Spring sceglie sempre il metodo piu' specifico: i 404 qui sopra non finiscono in questo.
	@ExceptionHandler(Exception.class)
	@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
	public String erroreInterno(Exception ex) {
		log.error("Errore imprevisto", ex);
		return "error/500";
	}
}
