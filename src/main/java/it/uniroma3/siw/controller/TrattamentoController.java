package it.uniroma3.siw.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import it.uniroma3.siw.service.RecensioneService;
import it.uniroma3.siw.service.TrattamentoService;

@Controller
public class TrattamentoController {
	private final TrattamentoService trattamentoService;
	private final RecensioneService recensioneService;

	public TrattamentoController(TrattamentoService trattamentoService, RecensioneService recensioneService) {
		this.trattamentoService = trattamentoService;
		this.recensioneService = recensioneService;
	}

	@GetMapping("/trattamenti")
	public String tuttiTrattamenti(Model model) {
		model.addAttribute("trattamenti", this.trattamentoService.findAll());
		return "trattamenti/tuttitrattamenti";
	}

	@GetMapping("/trattamenti/{id}")
	public String singoloTrattamento(@PathVariable("id") Long id, Model model) {
		model.addAttribute("trattamento", this.trattamentoService.findById(id));
		// Le recensioni degli appuntamenti di questo trattamento (con l'autrice gia' caricata), media e numero:
		// la media e il conteggio li calcola il database
		model.addAttribute("recensioni", this.recensioneService.delTrattamento(id));
		model.addAttribute("mediaVoti", this.recensioneService.mediaVoti(id));
		model.addAttribute("numeroRecensioni", this.recensioneService.numero(id));
		return "trattamenti/trattamento";
	}
}
