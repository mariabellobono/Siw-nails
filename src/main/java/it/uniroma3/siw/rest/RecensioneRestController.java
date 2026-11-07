package it.uniroma3.siw.rest;

import java.net.URI;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import it.uniroma3.siw.dto.ApiDto.RecensioneDto;
import it.uniroma3.siw.dto.ApiDto.RecensioneRequest;
import it.uniroma3.siw.model.Recensione;
import it.uniroma3.siw.service.RecensioneService;
import jakarta.validation.Valid;

// API delle RECENSIONI (si scrivono sugli appuntamenti completati). Leggerle: GET su trattamenti e appuntamenti.
// Scrivere: serve il login (SecurityConfiguration) e si agisce solo sui PROPRI appuntamenti/recensioni (403).
@RestController
@RequestMapping("/api")
public class RecensioneRestController {

	private final RecensioneService recensioneService;

	public RecensioneRestController(RecensioneService recensioneService) {
		this.recensioneService = recensioneService;
	}

	// POST /api/appuntamenti/{id}/recensione  ->  201, 400, 401, 403 (non e' tuo), 404, 409 (non completato o gia' recensito)
	// La cliente NON arriva dal corpo della richiesta: e' l'utente collegato (altrimenti si potrebbe scrivere a nome di altri).
	@PostMapping("/appuntamenti/{id}/recensione")
	public ResponseEntity<RecensioneDto> crea(@PathVariable("id") Long appuntamentoId,
			@Valid @RequestBody RecensioneRequest richiesta, Authentication authentication) {
		if (!recensioneService.isProprietaria(appuntamentoId, authentication.getName())) { // 404 se non esiste
			throw new AccessDeniedException("Puoi recensire solo i tuoi appuntamenti");
		}
		Recensione recensione = recensioneService.inserisci(appuntamentoId, authentication.getName(),
				daRichiesta(richiesta));
		return ResponseEntity.created(URI.create("/api/recensioni/" + recensione.getId()))
				.body(RestMapper.recensione(recensione));
	}

	// PUT /api/recensioni/{id}  ->  200, 400, 401, 403 (non e' tua), 404
	@PutMapping("/recensioni/{id}")
	public RecensioneDto modifica(@PathVariable("id") Long id, @Valid @RequestBody RecensioneRequest richiesta,
			Authentication authentication) {
		controllaAutrice(id, authentication);
		return RestMapper
				.recensione(recensioneService.modifica(id, authentication.getName(), daRichiesta(richiesta)));
	}

	// DELETE /api/recensioni/{id}  ->  204, 401, 403 (non e' tua), 404
	@DeleteMapping("/recensioni/{id}")
	public ResponseEntity<Void> elimina(@PathVariable("id") Long id, Authentication authentication) {
		controllaAutrice(id, authentication);
		recensioneService.elimina(id, authentication.getName());
		return ResponseEntity.noContent().build();
	}

	// 404 se la recensione non esiste, 403 se e' di un'altra cliente
	private void controllaAutrice(Long id, Authentication authentication) {
		Recensione recensione = recensioneService.findById(id);
		if (!recensioneService.isAutrice(recensione, authentication.getName())) {
			throw new AccessDeniedException("Puoi modificare o eliminare solo le tue recensioni");
		}
	}

	private Recensione daRichiesta(RecensioneRequest r) {
		Recensione dati = new Recensione();
		dati.setTesto(r.testo().trim());
		dati.setVoto(r.voto());
		return dati;
	}
}
