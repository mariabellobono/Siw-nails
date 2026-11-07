package it.uniroma3.siw.service;

import java.util.Optional;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import it.uniroma3.siw.model.Credenziali;
import it.uniroma3.siw.model.Ruolo;
import it.uniroma3.siw.repository.CredenzialiRepository;

@Service
public class CredenzialiService {

	private final CredenzialiRepository credenzialiRepository;
	private final PasswordEncoder passwordEncoder;

	public CredenzialiService(CredenzialiRepository credenzialiRepository,PasswordEncoder passwordEncoder) {
		this.credenzialiRepository = credenzialiRepository;
		this.passwordEncoder = passwordEncoder;
	}

	@Transactional(readOnly = true)
	public Optional<Credenziali> findByUsername(String username) {
		return this.credenzialiRepository.findByUsername(username);
	}

	@Transactional(readOnly = true)
	public boolean existsByUsername(String username) {
		return this.credenzialiRepository.existsByUsername(username);
	}

	// La cifratura della password (BCrypt) viene aggiunta nel blocco 2, con Spring Security.
	@Transactional
	public Credenziali save(Credenziali credenziali) {
		credenziali.setPassword(this.passwordEncoder.encode(credenziali.getPassword()));   //1. trasforma la password in hash 2.sostituisce la password in chiaro con l'hash
		credenziali.setRuolo(Ruolo.USER);  //ruolo fisso per i nuovi registrati
		return this.credenzialiRepository.save(credenziali); //salva con il cascade all salva l'utente
	}

}
