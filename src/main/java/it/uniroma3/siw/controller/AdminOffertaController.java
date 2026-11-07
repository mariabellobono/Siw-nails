package it.uniroma3.siw.controller;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import it.uniroma3.siw.dto.OffertaForm;
import it.uniroma3.siw.exception.EventoNotFoundException;
import it.uniroma3.siw.exception.OperazioneNonConsentitaException;
import it.uniroma3.siw.exception.TrattamentoNotFoundException;
import it.uniroma3.siw.service.EventoService;
import it.uniroma3.siw.service.OffertaService;
import it.uniroma3.siw.service.TrattamentoService;
import jakarta.validation.Valid;

@Controller
public class AdminOffertaController {
	private final OffertaService offertaService;
	private final EventoService eventoService;
	private final TrattamentoService trattamentoService;

	public AdminOffertaController(OffertaService offertaService, EventoService eventoService,
			TrattamentoService trattamentoService) {
		this.offertaService = offertaService;
		this.eventoService = eventoService;
		this.trattamentoService = trattamentoService;
	}

	// ---------- INSERIMENTO: aggiungi un trattamento a un evento ----------
	// eventoId e' facoltativo: se arriva dalla pagina di un evento, la tendina parte gia' selezionata
	@GetMapping("/admin/offerte/new")
	public String nuovaOfferta(@RequestParam(name = "eventoId", required = false) Long eventoId, Model model) {
		OffertaForm form = new OffertaForm();
		form.setEventoId(eventoId);
		model.addAttribute("offertaForm", form);
		caricaTendine(model);
		return "admin/offerta-form";
	}

	@PostMapping("/admin/offerte")
	public String salvaOfferta(@Valid @ModelAttribute("offertaForm") OffertaForm form, BindingResult errori,
			Model model, RedirectAttributes redirectAttributes) {
		if (!errori.hasErrors()) {
			try {
				this.offertaService.crea(form);
				redirectAttributes.addFlashAttribute("messaggio", "Trattamento aggiunto all'evento");
				return "redirect:/eventi/" + form.getEventoId();
			} catch (OperazioneNonConsentitaException e) {
				errori.reject("offertaDuplicata", e.getMessage());
			} catch (DataIntegrityViolationException e) {
				errori.reject("offertaDuplicata", "Questo trattamento e gia presente nell'evento");
			} catch (EventoNotFoundException | TrattamentoNotFoundException e) {
				errori.reject("datiNonValidi", e.getMessage());
			}
		}
		caricaTendine(model);
		return "admin/offerta-form";
	}

	// ---------- ELIMINAZIONE ----------
	@PostMapping("/admin/offerte/{id}/elimina")
	public String eliminaOfferta(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
		// se l'offerta ha appuntamenti il service lancia l'eccezione: si resta sulla pagina dell'evento con il messaggio
		try {
			Long eventoId = this.offertaService.elimina(id);
			redirectAttributes.addFlashAttribute("messaggio", "Trattamento tolto dall'evento");
			return "redirect:/eventi/" + eventoId;
		} catch (OperazioneNonConsentitaException e) {
			redirectAttributes.addFlashAttribute("messaggio", e.getMessage());
			return "redirect:/eventi/" + this.offertaService.findById(id).getEvento().getId();
		}
	}

	private void caricaTendine(Model model) {
		model.addAttribute("eventi", this.eventoService.findAll());
		model.addAttribute("trattamenti", this.trattamentoService.findAll());
	}
}
