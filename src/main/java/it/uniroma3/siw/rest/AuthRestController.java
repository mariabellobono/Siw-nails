package it.uniroma3.siw.rest;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import it.uniroma3.siw.dto.ApiDto.LoginRequest;
import it.uniroma3.siw.dto.ApiDto.UtenteCorrenteDto;
import it.uniroma3.siw.model.Credenziali;
import it.uniroma3.siw.model.Utente;
import it.uniroma3.siw.service.CredenzialiService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

// LOGIN per l'API (usato da React). Stesso meccanismo del sito: username/password controllati dal database,
// poi l'utente resta collegato con la SESSIONE (cookie JSESSIONID). Il logout e' in SecurityConfiguration
// (POST /api/auth/logout).
@RestController
@RequestMapping("/api/auth")
public class AuthRestController {

	private final AuthenticationManager authenticationManager;
	private final CredenzialiService credenzialiService;
	// dove Spring Security conserva "chi e' collegato" fra una richiesta e l'altra: la sessione HTTP
	private final SecurityContextRepository contextRepository = new HttpSessionSecurityContextRepository();

	public AuthRestController(AuthenticationManager authenticationManager, CredenzialiService credenzialiService) {
		this.authenticationManager = authenticationManager;
		this.credenzialiService = credenzialiService;
	}

	// GET /api/auth/me  ->  200 sempre: chi sono (authenticated=false se non sono collegato)
	@GetMapping("/me")
	public UtenteCorrenteDto me(Authentication authentication) {
		return descrivi(authentication);
	}

	// POST /api/auth/login  ->  200 con i dati dell'utente, 400 campi vuoti, 401 credenziali errate
	@PostMapping("/login")
	public UtenteCorrenteDto login(@Valid @RequestBody LoginRequest richiesta, HttpServletRequest request,
			HttpServletResponse response) {
		// Se le credenziali sono sbagliate lancia BadCredentialsException (-> 401 in RestExceptionHandler)
		Authentication autenticato = authenticationManager.authenticate(
				UsernamePasswordAuthenticationToken.unauthenticated(richiesta.username(), richiesta.password()));
		if (request.getSession(false) != null) {
			request.changeSessionId(); // nuovo id di sessione dopo il login (difesa da "session fixation")
		}
		SecurityContext context = SecurityContextHolder.createEmptyContext();
		context.setAuthentication(autenticato);
		SecurityContextHolder.setContext(context);
		contextRepository.saveContext(context, request, response); // memorizza il login nella sessione
		return descrivi(autenticato);
	}

	private UtenteCorrenteDto descrivi(Authentication authentication) {
		if (authentication == null || authentication instanceof AnonymousAuthenticationToken) {
			return new UtenteCorrenteDto(false, null, null, null, false);
		}
		String username = authentication.getName();
		Utente utente = credenzialiService.findByUsername(username).map(Credenziali::getUtente).orElse(null);
		boolean admin = authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ADMIN"));
		return new UtenteCorrenteDto(true, username, utente == null ? null : utente.getId(),
				utente == null ? null : utente.getNome(), admin);
	}
}
