package it.uniroma3.siw.configuration;

import java.io.IOException;

import javax.sql.DataSource;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.JdbcUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;

import jakarta.servlet.http.HttpServletResponse;

@Configuration
@EnableWebSecurity
public class SecurityConfiguration {
	
	private final DataSource dataSource;
	
	public SecurityConfiguration(DataSource dataSource) { // connessione al database
		this.dataSource = dataSource;
	}
	@Bean
	public PasswordEncoder confrontaPassword() {
	    return new BCryptPasswordEncoder();
	}
	
	@Bean
	public UserDetailsService cercaUtente() {
		JdbcUserDetailsManager manager = new JdbcUserDetailsManager(dataSource);  // oggetto della libreria che interroga il database passato 
		manager.setUsersByUsernameQuery("SELECT username,password,true AS enabled FROM credenziali WHERE username=?"); //
		manager.setAuthoritiesByUsernameQuery("SELECT username,ruolo FROM credenziali WHERE username =?");
		return manager;  //consegna al magazzino 
	}
	// Serve al login dell'API (AuthRestController): e' il "controllore" di username e password,
	// costruito da Spring con il UserDetailsService e il PasswordEncoder definiti qui sopra.
	@Bean
	public AuthenticationManager authenticationManager(AuthenticationConfiguration configurazione) throws Exception {
		return configurazione.getAuthenticationManager();
	}

	// SECONDA catena di regole, SOLO per l'API (/api/**). Spring prova le catene in ordine (@Order):
	// la prima il cui securityMatcher corrisponde all'indirizzo gestisce la richiesta.
	// Differenze rispetto al sito: niente pagina di login ma errori 401/403 in JSON,
	// e il token CSRF viaggia in un cookie che React legge e rimanda nell'header X-XSRF-TOKEN.
	@Bean
	@Order(1)
	public SecurityFilterChain regoleApi(HttpSecurity http) throws Exception {
		http.securityMatcher("/api/**");

		// CSRF resta ATTIVO (la sessione e' un cookie: senza token un altro sito potrebbe far partire richieste a nome tuo).
		// withHttpOnlyFalse: il cookie XSRF-TOKEN deve essere leggibile da JavaScript.
		// setCsrfRequestAttributeName(null): il token viene generato subito, cosi' il cookie arriva gia' alla prima GET.
		CsrfTokenRequestAttributeHandler gestoreCsrf = new CsrfTokenRequestAttributeHandler();
		gestoreCsrf.setCsrfRequestAttributeName(null);
		http.csrf(csrf -> {
			csrf.csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse());
			csrf.csrfTokenRequestHandler(gestoreCsrf);
		});

		http.authorizeHttpRequests(auth -> {
			auth.requestMatchers(HttpMethod.GET, "/api/miei-appuntamenti").authenticated(); // i miei appuntamenti: login (prima della regola sui GET!)
			auth.requestMatchers(HttpMethod.GET, "/api/**").permitAll();                // leggere: tutti
			auth.requestMatchers(HttpMethod.POST, "/api/auth/login", "/api/auth/logout").permitAll();
			// prenotare, spostare, annullare, recensire: serve il login (poi il controller verifica che sia roba tua)
			auth.requestMatchers("/api/appuntamenti/**", "/api/recensioni/**").authenticated();
			auth.anyRequest().hasAuthority("ADMIN");                                    // tutto il resto: solo admin
		});

		// non collegato -> 401, collegato ma senza permesso -> 403 (in JSON, mai il redirect alla pagina di login)
		http.exceptionHandling(eccezioni -> {
			eccezioni.authenticationEntryPoint((request, response, ex) -> scriviErrore(response,
					HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized", "Devi effettuare il login"));
			eccezioni.accessDeniedHandler((request, response, ex) -> scriviErrore(response,
					HttpServletResponse.SC_FORBIDDEN, "Forbidden", "Non hai il permesso per questa operazione"));
		});

		http.logout(logout -> {
			logout.logoutUrl("/api/auth/logout");
			logout.logoutSuccessHandler((request, response, authentication) -> response
					.setStatus(HttpServletResponse.SC_NO_CONTENT)); // 204
			logout.invalidateHttpSession(true);
			logout.deleteCookies("JSESSIONID");
		});
		return http.build();
	}

	private static void scriviErrore(HttpServletResponse response, int status, String errore, String messaggio)
			throws IOException {
		response.setStatus(status);
		response.setContentType("application/json;charset=UTF-8");
		response.getWriter().write("{\"status\":" + status + ",\"errore\":\"" + errore + "\",\"messaggio\":\""
				+ messaggio + "\",\"campi\":{}}");
	}

	// Regole del SITO (pagine HTML): valutate dopo la catena dell'API
	@Bean
	@Order(2)
	public SecurityFilterChain regoleAccesso(HttpSecurity http) throws Exception {
	    http.authorizeHttpRequests(auth -> {
	        auth.requestMatchers(HttpMethod.GET, "/", "/index", "/login", "/register", "/error",
	                "/eventi/**", "/trattamenti/**", "/operatrici/**", "/appuntamenti/**",
	                "/css/**", "/images/**", "/favicon.ico", "/app", "/app/**").permitAll();
	        auth.requestMatchers(HttpMethod.POST, "/register", "/login").permitAll();
	        auth.requestMatchers("/admin/**").hasAuthority("ADMIN");    //hasAuthority protegge gia l'admin quindi non deve passare per il controller 
	        auth.anyRequest().authenticated();
	    });
	    http.formLogin(form -> {
	        form.loginPage("/login").permitAll();
	        form.defaultSuccessUrl("/", true);
	        form.failureUrl("/login?error=true");
	   });
	    http.logout(logout -> {
	        logout.logoutUrl("/logout");
	        logout.logoutSuccessUrl("/");
	        logout.invalidateHttpSession(true);
	        logout.deleteCookies("JSESSIONID");
	        logout.clearAuthentication(true);
	        logout.permitAll();
	    });
	
	return http.build();
	}
}
