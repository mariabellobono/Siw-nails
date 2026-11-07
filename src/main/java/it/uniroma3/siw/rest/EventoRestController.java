package it.uniroma3.siw.rest;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import it.uniroma3.siw.dto.ApiDto.EventoDettaglioDto;
import it.uniroma3.siw.dto.ApiDto.EventoDto;
import it.uniroma3.siw.dto.ApiDto.OffertaDto;
import it.uniroma3.siw.service.EventoService;
import it.uniroma3.siw.service.OffertaService;

// API degli EVENTI (sola lettura). @RestController = @Controller + @ResponseBody: il valore restituito diventa JSON
// (non il nome di una pagina). Chi puo' fare cosa e' deciso in SecurityConfiguration: GET per tutti.
@RestController
@RequestMapping("/api")
public class EventoRestController {

	private final EventoService eventoService;
	private final OffertaService offertaService;

	public EventoRestController(EventoService eventoService, OffertaService offertaService) {
		this.eventoService = eventoService;
		this.offertaService = offertaService;
	}

	// GET /api/eventi  ->  200 con tutti gli eventi, dal piu' vicino
	@GetMapping("/eventi")
	public List<EventoDto> elenco() {
		return eventoService.findAll().stream().map(RestMapper::evento).toList();
	}

	// GET /api/eventi/{id}  ->  200 con il dettaglio (trattamenti con prezzo e tema) oppure 404
	@GetMapping("/eventi/{id}")
	public EventoDettaglioDto dettaglio(@PathVariable("id") Long id) {
		return RestMapper.eventoDettaglio(eventoService.findById(id));
	}

	// GET /api/offerte  ->  200 con tutti i trattamenti di tutti gli eventi (serve al modulo di prenotazione)
	@GetMapping("/offerte")
	public List<OffertaDto> offerte() {
		return offertaService.findAllConDettagli().stream().map(RestMapper::offerta).toList();
	}
}
