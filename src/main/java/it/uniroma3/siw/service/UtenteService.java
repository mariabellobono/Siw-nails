package it.uniroma3.siw.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import it.uniroma3.siw.model.Utente;
import it.uniroma3.siw.repository.UtenteRepository;

@Service
public class UtenteService {

	private final UtenteRepository utenteRepository;

	public UtenteService(UtenteRepository utenteRepository) {
		this.utenteRepository = utenteRepository;
	}

	@Transactional(readOnly = true)
	public Utente findById(Long id) {
		return this.utenteRepository.findById(id)
				.orElseThrow(() -> new IllegalArgumentException("L'utente con ID " + id + " non esiste."));
	}

	@Transactional
	public Utente save(Utente utente) {
		return this.utenteRepository.save(utente);
	}
	@Transactional(readOnly = true)
	public boolean existsByEmail(String email) {
		return this.utenteRepository.existsByEmail(email);
		
	}

}
