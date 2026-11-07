package it.uniroma3.siw.controller;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import it.uniroma3.siw.exception.OperazioneNonConsentitaException;
import it.uniroma3.siw.model.Recensione;
import it.uniroma3.siw.service.AppuntamentoService;
import it.uniroma3.siw.service.RecensioneService;
import jakarta.validation.Valid;

// Le recensioni si scrivono SUGLI APPUNTAMENTI completati. Tutti gli indirizzi /recensioni/** richiedono il login
// (regola anyRequest().authenticated() in SecurityConfiguration).
// La cliente non arriva mai dal modulo: si ricava dal login (authentication.getName()).
@Controller
public class RecensioneController {
	private final RecensioneService recensioneService;
	private final AppuntamentoService appuntamentoService;

	public RecensioneController(RecensioneService recensioneService, AppuntamentoService appuntamentoService) {
		this.recensioneService = recensioneService;
		this.appuntamentoService = appuntamentoService;
	}

	// ---------- INSERIMENTO ----------
	@GetMapping("/recensioni/appuntamento/{appuntamentoId}/new")
	public String nuova(@PathVariable("appuntamentoId") Long appuntamentoId, Authentication authentication, Model model,
			RedirectAttributes redirectAttributes) {
		// Se non si puo' recensire (non e' sua, non e' completato, gia' recensito) si torna all'appuntamento con il motivo
		try {
			this.recensioneService.verificaRecensibile(appuntamentoId, authentication.getName());
		} catch (OperazioneNonConsentitaException e) {
			redirectAttributes.addFlashAttribute("messaggio", e.getMessage());
			return "redirect:/appuntamenti/" + appuntamentoId;
		}
		model.addAttribute("appuntamento", this.appuntamentoService.findById(appuntamentoId));
		model.addAttribute("recensione", new Recensione());
		return "recensioni/recensione-form";
	}

	@PostMapping("/recensioni/appuntamento/{appuntamentoId}")
	public String salva(@PathVariable("appuntamentoId") Long appuntamentoId,
			@Valid @ModelAttribute("recensione") Recensione recensione, BindingResult errori,
			Authentication authentication, Model model, RedirectAttributes redirectAttributes) {
		if (errori.hasErrors()) {
			model.addAttribute("appuntamento", this.appuntamentoService.findById(appuntamentoId));
			return "recensioni/recensione-form";
		}
		try {
			this.recensioneService.inserisci(appuntamentoId, authentication.getName(), recensione);
			redirectAttributes.addFlashAttribute("messaggio", "Recensione inserita");
		} catch (OperazioneNonConsentitaException e) {
			redirectAttributes.addFlashAttribute("messaggio", e.getMessage());
		} catch (DataIntegrityViolationException e) {
			// due richieste simultanee: ha vinto l'altra, il vincolo unique ha bloccato questa
			redirectAttributes.addFlashAttribute("messaggio", "Hai già scritto una recensione per questo appuntamento");
		}
		return "redirect:/appuntamenti/" + appuntamentoId;
	}

	// ---------- MODIFICA (solo l'autrice) ----------
	@GetMapping("/recensioni/{id}/edit")
	public String modifica(@PathVariable("id") Long id, Authentication authentication, Model model,
			RedirectAttributes redirectAttributes) {
		Recensione recensione = this.recensioneService.findById(id);
		Long appuntamentoId = recensione.getAppuntamento().getId();
		if (!this.recensioneService.isAutrice(recensione, authentication.getName())) {
			redirectAttributes.addFlashAttribute("messaggio", "Puoi modificare solo le tue recensioni");
			return "redirect:/appuntamenti/" + appuntamentoId;
		}
		model.addAttribute("appuntamento", recensione.getAppuntamento());
		model.addAttribute("recensione", recensione);
		return "recensioni/recensione-form";
	}

	@PostMapping("/recensioni/{id}")
	public String aggiorna(@PathVariable("id") Long id, @Valid @ModelAttribute("recensione") Recensione recensione,
			BindingResult errori, Authentication authentication, Model model, RedirectAttributes redirectAttributes) {
		Long appuntamentoId = this.recensioneService.findById(id).getAppuntamento().getId();
		recensione.setId(id); // serve al modulo per capire che e' una modifica se si torna indietro con errori
		if (errori.hasErrors()) {
			model.addAttribute("appuntamento", this.appuntamentoService.findById(appuntamentoId));
			return "recensioni/recensione-form";
		}
		try {
			this.recensioneService.modifica(id, authentication.getName(), recensione);
			redirectAttributes.addFlashAttribute("messaggio", "Modifica effettuata");
		} catch (OperazioneNonConsentitaException e) {
			redirectAttributes.addFlashAttribute("messaggio", e.getMessage());
		}
		return "redirect:/appuntamenti/" + appuntamentoId;
	}

	// ---------- ELIMINAZIONE (solo l'autrice) ----------
	@PostMapping("/recensioni/{id}/elimina")
	public String elimina(@PathVariable("id") Long id, Authentication authentication,
			RedirectAttributes redirectAttributes) {
		Long appuntamentoId = this.recensioneService.findById(id).getAppuntamento().getId();
		try {
			this.recensioneService.elimina(id, authentication.getName());
			redirectAttributes.addFlashAttribute("messaggio", "Recensione eliminata");
		} catch (OperazioneNonConsentitaException e) {
			redirectAttributes.addFlashAttribute("messaggio", e.getMessage());
		}
		return "redirect:/appuntamenti/" + appuntamentoId;
	}
}
