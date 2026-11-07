package it.uniroma3.siw.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import it.uniroma3.siw.service.OperatriceService;

@Controller
public class OperatriceController {

	private final OperatriceService operatriceService;

	public OperatriceController(OperatriceService operatriceService) {
		this.operatriceService = operatriceService;
	}

	@GetMapping("/operatrici")
	public String tutteOperatrici(Model model) {
		model.addAttribute("operatrici", this.operatriceService.findAll());
		return "operatrici/tutteoperatrici";
	}

	@GetMapping("/operatrici/{id}")
	public String singolaOperatrice(@PathVariable("id") Long id, Model model) {
		model.addAttribute("operatrice", this.operatriceService.findById(id));
		return "operatrici/operatrice";
	}

}
