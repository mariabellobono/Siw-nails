package it.uniroma3.siw.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import it.uniroma3.siw.service.AppuntamentoService;
import it.uniroma3.siw.service.EventoService;

@Controller
public class EventoController {

	private final EventoService eventoService;
	private final AppuntamentoService appuntamentoService;

	public EventoController(EventoService eventoService, AppuntamentoService appuntamentoService) {
		this.eventoService = eventoService;
		this.appuntamentoService = appuntamentoService;
	}

	@GetMapping("/eventi")
	public String tuttiEventi(Model model) {
		model.addAttribute("eventi", this.eventoService.findAll());
		return "eventi/tuttieventi";
	}

	@GetMapping("/eventi/{id}")
	public String singoloEvento(@PathVariable("id") Long id, Model model) {
		model.addAttribute("evento", this.eventoService.findById(id));
		// l'agenda dell'evento (appuntamenti in ordine di data e ora)
		model.addAttribute("appuntamenti", this.appuntamentoService.findByEvento(id));
		return "eventi/evento";
	}

}
