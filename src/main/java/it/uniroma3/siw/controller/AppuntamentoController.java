package it.uniroma3.siw.controller;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import it.uniroma3.siw.model.Appuntamento;
import it.uniroma3.siw.model.Recensione;
import it.uniroma3.siw.model.Stato;
import it.uniroma3.siw.service.AppuntamentoService;
import it.uniroma3.siw.service.RecensioneService;

@Controller
public class AppuntamentoController {
	private final AppuntamentoService appuntamentoService;
	private final RecensioneService recensioneService;

	public AppuntamentoController(AppuntamentoService appuntamentoService, RecensioneService recensioneService) {
		this.appuntamentoService = appuntamentoService;
		this.recensioneService = recensioneService;
	}

	@GetMapping("/appuntamenti")
	public String tuttiAppuntamenti(Model model) {
		model.addAttribute("appuntamenti", this.appuntamentoService.findAllConDettagli());
		return "appuntamenti/tuttiappuntamenti";
	}

	// authentication e' null se chi guarda non ha fatto il login (la pagina e' pubblica)
	@GetMapping("/appuntamenti/{id}")
	public String singoloAppuntamento(@PathVariable("id") Long id, Authentication authentication, Model model) {
		Appuntamento appuntamento = this.appuntamentoService.findById(id);
		model.addAttribute("appuntamento", appuntamento);
		String username = authentication == null ? null : authentication.getName();
		model.addAttribute("puoGestire", this.appuntamentoService.puoGestire(appuntamento, username,
				AppuntamentoClienteController.isAdmin(authentication)));

		// Recensione dell'appuntamento (null se non c'e') e cosa puo' fare chi guarda:
		// scriverla (solo la proprietaria, solo se COMPLETATO e senza recensione) o modificarla/eliminarla (solo l'autrice)
		Recensione recensione = this.recensioneService.delAppuntamento(id);
		boolean proprietaria = username != null && this.recensioneService.isProprietaria(id, username);
		model.addAttribute("recensione", recensione);
		model.addAttribute("puoRecensire", proprietaria && recensione == null && appuntamento.getStato() == Stato.COMPLETATO);
		model.addAttribute("puoModificareRecensione", proprietaria && recensione != null);
		return "appuntamenti/appuntamento";
	}
}
