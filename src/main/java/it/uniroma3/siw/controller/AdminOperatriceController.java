package it.uniroma3.siw.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import it.uniroma3.siw.model.Operatrice;
import it.uniroma3.siw.service.OperatriceService;
import jakarta.validation.Valid;

@Controller
public class AdminOperatriceController {
	private final OperatriceService operatriceService;

	public AdminOperatriceController(OperatriceService operatriceService) {
		this.operatriceService = operatriceService;
	}

	@GetMapping("/admin/operatrici/new")
	public String inserisciOperatrice(Model model) {
		model.addAttribute("operatrice", new Operatrice());
		return "admin/operatrice-form";
	}

	@PostMapping("/admin/operatrici")
	public String salvaOperatrice(@Valid @ModelAttribute("operatrice") Operatrice operatrice, BindingResult errori,
			RedirectAttributes redirectAttributes) {
		controllaOrario(operatrice, errori);
		if (errori.hasErrors()) {
			return "admin/operatrice-form";
		}
		operatrice.setId(null);
		operatriceService.save(operatrice);

		redirectAttributes.addFlashAttribute("messaggio", "Inserimento effettuato");
		return "redirect:/operatrici/" + operatrice.getId();
	}

	@GetMapping("/admin/operatrici/{id}/edit")
	public String modificaOperatrice(@PathVariable("id") Long id, Model model) {
		model.addAttribute("operatrice", operatriceService.findById(id));
		return "admin/operatrice-form";
	}

	@PostMapping("/admin/operatrici/{id}")
	public String aggiornaOperatrice(@PathVariable("id") Long id,
			@Valid @ModelAttribute("operatrice") Operatrice operatrice, BindingResult errori,
			RedirectAttributes redirectAttributes) {
		operatrice.setId(id);
		controllaOrario(operatrice, errori);
		if (errori.hasErrors()) {
			return "admin/operatrice-form";
		}
		operatriceService.aggiorna(id, operatrice);

		redirectAttributes.addFlashAttribute("messaggio", "Modifica effettuata");
		return "redirect:/operatrici/" + id;
	}

	// la fine del turno deve venire dopo l'inizio
	private void controllaOrario(Operatrice operatrice, BindingResult errori) {
		if (operatrice.getOraInizio() != null && operatrice.getOraFine() != null
				&& !operatrice.getOraFine().isAfter(operatrice.getOraInizio())) {
			errori.rejectValue("oraFine", "orarioNonValido", "L'orario di fine deve essere dopo quello di inizio");
		}
	}
}
