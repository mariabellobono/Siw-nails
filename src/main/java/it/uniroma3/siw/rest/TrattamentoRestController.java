package it.uniroma3.siw.rest;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import it.uniroma3.siw.dto.ApiDto.RecensioneDto;
import it.uniroma3.siw.dto.ApiDto.TrattamentoDettaglioDto;
import it.uniroma3.siw.dto.ApiDto.TrattamentoDto;
import it.uniroma3.siw.model.Trattamento;
import it.uniroma3.siw.service.RecensioneService;
import it.uniroma3.siw.service.TrattamentoService;

// API dei TRATTAMENTI e delle loro recensioni (sola lettura)
@RestController
@RequestMapping("/api")
public class TrattamentoRestController {

	private final TrattamentoService trattamentoService;
	private final RecensioneService recensioneService;

	public TrattamentoRestController(TrattamentoService trattamentoService, RecensioneService recensioneService) {
		this.trattamentoService = trattamentoService;
		this.recensioneService = recensioneService;
	}

	// GET /api/trattamenti  ->  200 con tutti i trattamenti
	@GetMapping("/trattamenti")
	public List<TrattamentoDto> elenco() {
		return trattamentoService.findAll().stream().map(RestMapper::trattamento).toList();
	}

	// GET /api/trattamenti/{id}  ->  200 con il dettaglio (offerte, voto medio) oppure 404
	@GetMapping("/trattamenti/{id}")
	public TrattamentoDettaglioDto dettaglio(@PathVariable("id") Long id) {
		Trattamento trattamento = trattamentoService.findById(id);
		return RestMapper.trattamentoDettaglio(trattamento, recensioneService.mediaVoti(id),
				recensioneService.numero(id));
	}

	// GET /api/trattamenti/{id}/recensioni  ->  200 con le recensioni degli appuntamenti di quel trattamento, 404
	@GetMapping("/trattamenti/{id}/recensioni")
	public List<RecensioneDto> recensioni(@PathVariable("id") Long id) {
		return recensioneService.delTrattamento(id).stream().map(RestMapper::recensione).toList();
	}
}
