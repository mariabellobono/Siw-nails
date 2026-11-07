package it.uniroma3.siw.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import it.uniroma3.siw.model.Trattamento;
import it.uniroma3.siw.service.TrattamentoService;
import jakarta.validation.Valid;

@Controller
public class AdminTrattamentoController {
	private final TrattamentoService trattamentoService;

	public AdminTrattamentoController(TrattamentoService trattamentoService) {
		this.trattamentoService = trattamentoService;
	}

	@GetMapping("/admin/trattamenti/new")
	public String inserisciTrattamento(Model model) {
		model.addAttribute("trattamento", new Trattamento());
		return "admin/trattamento-form";
	}

	@PostMapping("/admin/trattamenti")
	public String salvaTrattamento(@Valid @ModelAttribute("trattamento") Trattamento trattamento,
			BindingResult errori, RedirectAttributes redirectAttributes) {
		if (!errori.hasErrors() && trattamentoService.existsByNome(trattamento.getNome())) {
			errori.rejectValue("nome", "duplicato", "Trattamento gia esistente");
		}
		if (errori.hasErrors()) {
			return "admin/trattamento-form";
		}
		trattamento.setId(null);
		trattamentoService.save(trattamento);

		redirectAttributes.addFlashAttribute("messaggio", "Inserimento effettuato");
		return "redirect:/trattamenti/" + trattamento.getId();
	}

	@GetMapping("/admin/trattamenti/{id}/edit")
	public String modificaTrattamento(@PathVariable("id") Long id, Model model) {
		model.addAttribute("trattamento", trattamentoService.findById(id));
		return "admin/trattamento-form";
	}

	@PostMapping("/admin/trattamenti/{id}")
	public String aggiornaTrattamento(@PathVariable("id") Long id,
			@Valid @ModelAttribute("trattamento") Trattamento trattamento, BindingResult errori,
			RedirectAttributes redirectAttributes) {
		trattamento.setId(id);
		if (!errori.hasErrors() && trattamentoService.existsByNomeAndIdNot(trattamento.getNome(), id)) {
			errori.rejectValue("nome", "duplicato", "Trattamento gia esistente");
		}
		if (errori.hasErrors()) {
			return "admin/trattamento-form";
		}
		trattamentoService.aggiorna(id, trattamento);

		redirectAttributes.addFlashAttribute("messaggio", "Modifica effettuata");
		return "redirect:/trattamenti/" + id;
	}
}
