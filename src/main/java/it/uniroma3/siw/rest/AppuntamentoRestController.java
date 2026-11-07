package it.uniroma3.siw.rest;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import it.uniroma3.siw.dto.ApiDto.AppuntamentoDto;
import it.uniroma3.siw.dto.ApiDto.PrenotazioneRequest;
import it.uniroma3.siw.dto.PrenotazioneForm;
import it.uniroma3.siw.model.Appuntamento;
import it.uniroma3.siw.model.Recensione;
import it.uniroma3.siw.service.AppuntamentoService;
import it.uniroma3.siw.service.RecensioneService;
import jakarta.validation.Valid;

// API degli APPUNTAMENTI. Leggere l'agenda: tutti. Prenotare, spostare, annullare: serve il login
// (SecurityConfiguration) e si agisce solo sui PROPRI appuntamenti (controllo qui sotto -> 403; l'admin puo' tutto).
@RestController
@RequestMapping("/api")
public class AppuntamentoRestController {

	private final AppuntamentoService appuntamentoService;
	private final RecensioneService recensioneService;

	public AppuntamentoRestController(AppuntamentoService appuntamentoService, RecensioneService recensioneService) {
		this.appuntamentoService = appuntamentoService;
		this.recensioneService = recensioneService;
	}

	// GET /api/appuntamenti  ->  200 con l'agenda (senza il nome della cliente)
	@GetMapping("/appuntamenti")
	public List<AppuntamentoDto> agenda() {
		return appuntamentoService.findAllConDettagli().stream().map(a -> RestMapper.appuntamento(a, null)).toList();
	}

	// GET /api/appuntamenti/{id}  ->  200 oppure 404
	@GetMapping("/appuntamenti/{id}")
	public AppuntamentoDto dettaglio(@PathVariable("id") Long id) {
		return RestMapper.appuntamento(appuntamentoService.findById(id), recensioneService.delAppuntamento(id));
	}

	// GET /api/miei-appuntamenti  ->  200 con gli appuntamenti della cliente collegata (e le sue recensioni), 401
	@GetMapping("/miei-appuntamenti")
	public List<AppuntamentoDto> miei(Authentication authentication) {
		// le recensioni della cliente si caricano in UNA query e si abbinano per id appuntamento
		Map<Long, Recensione> recensioni = recensioneService.delleUtente(authentication.getName()).stream()
				.collect(Collectors.toMap(r -> r.getAppuntamento().getId(), Function.identity()));
		return appuntamentoService.findByUsername(authentication.getName()).stream()
				.map(a -> RestMapper.appuntamento(a, recensioni.get(a.getId()))).toList();
	}

	// POST /api/appuntamenti  ->  201 + Location, 400 dati non validi, 401, 404 offerta/operatrice,
	// 409 regole di prenotazione violate (data passata, fuori evento, fuori orario, operatrice occupata)
	@PostMapping("/appuntamenti")
	public ResponseEntity<AppuntamentoDto> prenota(@Valid @RequestBody PrenotazioneRequest richiesta,
			Authentication authentication) {
		Appuntamento salvato = appuntamentoService.prenota(daRichiesta(richiesta), authentication.getName());
		return ResponseEntity.created(URI.create("/api/appuntamenti/" + salvato.getId()))
				.body(RestMapper.appuntamento(salvato, null));
	}

	// PUT /api/appuntamenti/{id}  ->  200, 400, 401, 403 (non e' tuo), 404, 409
	@PutMapping("/appuntamenti/{id}")
	public AppuntamentoDto sposta(@PathVariable("id") Long id, @Valid @RequestBody PrenotazioneRequest richiesta,
			Authentication authentication) {
		controllaProprietario(id, authentication);
		return RestMapper.appuntamento(appuntamentoService.modifica(id, daRichiesta(richiesta),
				authentication.getName(), isAdmin(authentication)), null);
	}

	// POST /api/appuntamenti/{id}/annulla  ->  200 con l'appuntamento ANNULLATO, 401, 403, 404, 409 se non e' piu' prenotato.
	// Non e' una DELETE: l'appuntamento non sparisce, resta nello storico con stato ANNULLATO.
	@PostMapping("/appuntamenti/{id}/annulla")
	public AppuntamentoDto annulla(@PathVariable("id") Long id, Authentication authentication) {
		controllaProprietario(id, authentication);
		return RestMapper.appuntamento(
				appuntamentoService.annulla(id, authentication.getName(), isAdmin(authentication)), null);
	}

	// 404 se l'appuntamento non esiste, 403 se e' di un'altra cliente (e chi chiede non e' admin)
	private void controllaProprietario(Long id, Authentication authentication) {
		if (!isAdmin(authentication) && !appuntamentoService.isProprietario(id, authentication.getName())) {
			throw new AccessDeniedException("Questo appuntamento appartiene a un'altra cliente");
		}
	}

	private boolean isAdmin(Authentication authentication) {
		return authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ADMIN"));
	}

	// Il service lavora con il modulo del sito: dal DTO di richiesta si costruisce lo stesso oggetto
	private PrenotazioneForm daRichiesta(PrenotazioneRequest r) {
		PrenotazioneForm form = new PrenotazioneForm();
		form.setTrattamentoId(r.trattamentoId());
		form.setOperatriceId(r.operatriceId());
		form.setData(r.data());
		form.setOra(r.ora());
		form.setNote(r.note());
		return form;
	}
}
