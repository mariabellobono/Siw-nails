package it.uniroma3.siw.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

// Apre la mini-applicazione React (file statici in resources/static/app).
// "forward:" = il server serve internamente quel file, l'indirizzo nel browser non cambia.
@Controller
public class AppController {

	@GetMapping({ "/app", "/app/" })
	public String app() {
		return "forward:/app/index.html";
	}
}
