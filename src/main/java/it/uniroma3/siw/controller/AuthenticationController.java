package it.uniroma3.siw.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import it.uniroma3.siw.model.Credenziali;
import it.uniroma3.siw.model.Utente;
import it.uniroma3.siw.service.CredenzialiService;
import it.uniroma3.siw.service.UtenteService;
import jakarta.validation.Valid;

@Controller
public class AuthenticationController {
	
	private final UtenteService utenteService;
	private final CredenzialiService credenzialiService;
	
	public AuthenticationController(UtenteService utenteService,CredenzialiService credenzialiService) {
		this.utenteService = utenteService;
		this.credenzialiService = credenzialiService;
		
	}
	

    @GetMapping("/login")
    public String mostraLogin() {
        return "auth/login";
    }
    
    @GetMapping("/register")
    public String registrazione(Model model) {
    	model.addAttribute("utente",new Utente());
    	model.addAttribute("credenziali", new Credenziali());
    	return "auth/register";
    }
    @PostMapping("/register")
    public String registra(@Valid @ModelAttribute("utente") Utente utente, BindingResult utenteErrori,
                           @Valid @ModelAttribute("credenziali") Credenziali credenziali, BindingResult credenzialiErrori) {

        if (utenteService.existsByEmail(utente.getEmail())) {
            utenteErrori.rejectValue("email", "duplicata", "Email già registrata");
        }
        if (credenzialiService.existsByUsername(credenziali.getUsername())) {
            credenzialiErrori.rejectValue("username", "duplicato", "Username già in uso");
        }
        if (utenteErrori.hasErrors() || credenzialiErrori.hasErrors()) {
            return "auth/register";      // rimostra il modulo con gli errori
        }

        utente.setId(null);              // sicurezza: ignora un eventuale id inviato a mano
        credenziali.setId(null);
        credenziali.setUtente(utente);
        credenzialiService.save(credenziali);
        return "redirect:/login?registrato";
    }
    
    
}