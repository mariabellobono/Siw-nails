package it.uniroma3.siw.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import it.uniroma3.siw.model.Evento;
import it.uniroma3.siw.service.EventoService;
import jakarta.validation.Valid;

@Controller
public class AdminEventoController {
	private final EventoService eventoService;

	public AdminEventoController(EventoService eventoService) {
		this.eventoService = eventoService;
	}

	@GetMapping("/admin/eventi/new")
	public String inserisciEvento(Model model) {
		model.addAttribute("evento", new Evento());
		return "admin/evento-form";
	}

	@PostMapping("/admin/eventi")
	public String salvaEvento(@Valid @ModelAttribute("evento") Evento evento, BindingResult errori,
			RedirectAttributes redirectAttributes) {
		controllaDate(evento, errori);
		if (errori.hasErrors()) {
			return "admin/evento-form";
		}
		evento.setId(null);
		eventoService.save(evento);

		redirectAttributes.addFlashAttribute("messaggio", "Inserimento effettuato");
		return "redirect:/eventi/" + evento.getId();
	}

	// MODIFICA: stesso modulo dell'inserimento, ma gia' compilato con i dati dell'evento
	@GetMapping("/admin/eventi/{id}/edit")
	public String modificaEvento(@PathVariable("id") Long id, Model model) {
		model.addAttribute("evento", eventoService.findById(id));
		return "admin/evento-form";
	}

	@PostMapping("/admin/eventi/{id}")
	public String aggiornaEvento(@PathVariable("id") Long id, @Valid @ModelAttribute("evento") Evento evento,
			BindingResult errori, RedirectAttributes redirectAttributes) {
		evento.setId(id); // l'id arriva dall'indirizzo, non dal modulo; serve anche al modulo se si torna indietro con errori
		controllaDate(evento, errori);
		if (errori.hasErrors()) {
			return "admin/evento-form";
		}
		eventoService.aggiorna(id, evento);

		redirectAttributes.addFlashAttribute("messaggio", "Modifica effettuata");
		return "redirect:/eventi/" + id;
	}

	// la fine non puo' precedere l'inizio (controllo solo se le due date sono state compilate)
	private void controllaDate(Evento evento, BindingResult errori) {
		if (evento.getDataInizio() != null && evento.getDataFine() != null
				&& evento.getDataFine().isBefore(evento.getDataInizio())) {
			errori.rejectValue("dataFine", "dataNonValida", "La data di fine non puo precedere quella di inizio");
		}
	}
}
