package it.uniroma3.siw.controller;

import org.springframework.dao.ConcurrencyFailureException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import it.uniroma3.siw.dto.PrenotazioneForm;
import it.uniroma3.siw.exception.OperatriceNotFoundException;
import it.uniroma3.siw.exception.OperazioneNonConsentitaException;
import it.uniroma3.siw.exception.PrenotazioneNonValidaException;
import it.uniroma3.siw.model.Appuntamento;
import it.uniroma3.siw.service.AppuntamentoService;
import it.uniroma3.siw.service.TrattamentoService;
import it.uniroma3.siw.service.OperatriceService;
import jakarta.validation.Valid;

// Area della CLIENTE: prenota, vede i suoi appuntamenti, li sposta o li annulla.
// Questi indirizzi non sono nell'elenco "permitAll" della sicurezza: richiedono il login.
// Lo username arriva da Authentication (Spring Security), mai dal modulo: la cliente non puo' fingersi un'altra.
@Controller
public class AppuntamentoClienteController {
	private final AppuntamentoService appuntamentoService;
	private final TrattamentoService trattamentoService;
	private final OperatriceService operatriceService;

	public AppuntamentoClienteController(AppuntamentoService appuntamentoService, TrattamentoService trattamentoService,
			OperatriceService operatriceService) {
		this.appuntamentoService = appuntamentoService;
		this.trattamentoService = trattamentoService;
		this.operatriceService = operatriceService;
	}

	// ---------- I MIEI APPUNTAMENTI ----------
	@GetMapping("/miei-appuntamenti")
	public String mieiAppuntamenti(Authentication authentication, Model model) {
		model.addAttribute("appuntamenti", this.appuntamentoService.findByUsername(authentication.getName()));
		return "appuntamenti/miei";
	}

	// ---------- PRENOTAZIONE ----------
	@GetMapping("/prenota")
	public String prenota(@org.springframework.web.bind.annotation.RequestParam(name = "trattamentoId", required = false) Long trattamentoId,
			Model model) {
		PrenotazioneForm form = new PrenotazioneForm();
		form.setTrattamentoId(trattamentoId); // preselezionato se si arriva dalla pagina del trattamento
		model.addAttribute("prenotazioneForm", form);
		caricaTendine(model);
		return "appuntamenti/prenota-form";
	}

	@PostMapping("/prenota")
	public String salvaPrenotazione(@Valid @ModelAttribute("prenotazioneForm") PrenotazioneForm form,
			BindingResult errori, Authentication authentication, Model model, RedirectAttributes redirectAttributes) {
		if (!errori.hasErrors()) {
			try {
				Appuntamento appuntamento = this.appuntamentoService.prenota(form, authentication.getName());
				redirectAttributes.addFlashAttribute("messaggio", "Prenotazione effettuata");
				return "redirect:/appuntamenti/" + appuntamento.getId();
			} catch (PrenotazioneNonValidaException e) {
				errori.reject("prenotazioneNonValida", e.getMessage());
			} catch (ConcurrencyFailureException e) {
				errori.reject("accessoSimultaneo", "Un'altra prenotazione e in corso sulla stessa operatrice: riprova");
			} catch (OperatriceNotFoundException e) {
				errori.reject("datiNonValidi", e.getMessage());
			}
		}
		caricaTendine(model);
		return "appuntamenti/prenota-form";
	}

	// ---------- SPOSTAMENTO / MODIFICA ----------
	@GetMapping("/miei-appuntamenti/{id}/edit")
	public String modifica(@PathVariable("id") Long id, Authentication authentication, Model model,
			RedirectAttributes redirectAttributes) {
		try {
			Appuntamento a = this.appuntamentoService.findPerModifica(id, authentication.getName(), isAdmin(authentication));
			PrenotazioneForm form = new PrenotazioneForm();
			form.setTrattamentoId(a.getOfferta().getTrattamento().getId());
			form.setOperatriceId(a.getOperatrice().getId());
			form.setData(a.getData());
			form.setOra(a.getOra());
			form.setNote(a.getNote());
			model.addAttribute("prenotazioneForm", form);
			model.addAttribute("appuntamentoId", id);
			caricaTendine(model);
			return "appuntamenti/prenota-form";
		} catch (OperazioneNonConsentitaException | PrenotazioneNonValidaException e) {
			redirectAttributes.addFlashAttribute("messaggio", e.getMessage());
			return "redirect:/appuntamenti/" + id;
		}
	}

	@PostMapping("/miei-appuntamenti/{id}")
	public String aggiorna(@PathVariable("id") Long id, @Valid @ModelAttribute("prenotazioneForm") PrenotazioneForm form,
			BindingResult errori, Authentication authentication, Model model, RedirectAttributes redirectAttributes) {
		if (!errori.hasErrors()) {
			try {
				this.appuntamentoService.modifica(id, form, authentication.getName(), isAdmin(authentication));
				redirectAttributes.addFlashAttribute("messaggio", "Modifica effettuata");
				return "redirect:/appuntamenti/" + id;
			} catch (OperazioneNonConsentitaException e) {
				redirectAttributes.addFlashAttribute("messaggio", e.getMessage());
				return "redirect:/appuntamenti/" + id;
			} catch (PrenotazioneNonValidaException e) {
				errori.reject("prenotazioneNonValida", e.getMessage());
			} catch (ConcurrencyFailureException e) {
				errori.reject("accessoSimultaneo", "Un'altra prenotazione e in corso sulla stessa operatrice: riprova");
			} catch (OperatriceNotFoundException e) {
				errori.reject("datiNonValidi", e.getMessage());
			}
		}
		model.addAttribute("appuntamentoId", id);
		caricaTendine(model);
		return "appuntamenti/prenota-form";
	}

	// ---------- ANNULLAMENTO ----------
	@PostMapping("/miei-appuntamenti/{id}/annulla")
	public String annulla(@PathVariable("id") Long id, Authentication authentication,
			RedirectAttributes redirectAttributes) {
		try {
			this.appuntamentoService.annulla(id, authentication.getName(), isAdmin(authentication));
			redirectAttributes.addFlashAttribute("messaggio", "Appuntamento annullato");
		} catch (OperazioneNonConsentitaException | PrenotazioneNonValidaException e) {
			redirectAttributes.addFlashAttribute("messaggio", e.getMessage());
		}
		return "redirect:/appuntamenti/" + id;
	}

	// Le tendine vanno ricaricate ogni volta che si mostra il modulo (anche dopo un errore)
	private void caricaTendine(Model model) {
		model.addAttribute("trattamenti", this.trattamentoService.findAll());
		model.addAttribute("operatrici", this.operatriceService.findAll());
	}

	static boolean isAdmin(Authentication authentication) {
		return authentication != null && authentication.getAuthorities().stream()
				.anyMatch(autorita -> autorita.getAuthority().equals("ADMIN"));
	}
}
