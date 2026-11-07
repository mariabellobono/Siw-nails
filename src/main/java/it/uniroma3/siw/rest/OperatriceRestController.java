package it.uniroma3.siw.rest;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import it.uniroma3.siw.dto.ApiDto.OperatriceDto;
import it.uniroma3.siw.service.OperatriceService;

// API delle OPERATRICI (sola lettura)
@RestController
@RequestMapping("/api/operatrici")
public class OperatriceRestController {

	private final OperatriceService operatriceService;

	public OperatriceRestController(OperatriceService operatriceService) {
		this.operatriceService = operatriceService;
	}

	// GET /api/operatrici  ->  200 con tutte le operatrici e i loro orari
	@GetMapping
	public List<OperatriceDto> elenco() {
		return operatriceService.findAll().stream().map(RestMapper::operatrice).toList();
	}
}
