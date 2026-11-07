package it.uniroma3.siw.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import it.uniroma3.siw.exception.AppuntamentoNotFoundException;
import it.uniroma3.siw.exception.OperazioneNonConsentitaException;
import it.uniroma3.siw.exception.RecensioneNotFoundException;
import it.uniroma3.siw.exception.TrattamentoNotFoundException;
import it.uniroma3.siw.model.Appuntamento;
import it.uniroma3.siw.model.Recensione;
import it.uniroma3.siw.model.Stato;
import it.uniroma3.siw.model.Utente;
import it.uniroma3.siw.repository.AppuntamentoRepository;
import it.uniroma3.siw.repository.CredenzialiRepository;
import it.uniroma3.siw.repository.RecensioneRepository;
import it.uniroma3.siw.repository.TrattamentoRepository;

@Service
public class RecensioneService {

	private final RecensioneRepository recensioneRepository;
	private final AppuntamentoRepository appuntamentoRepository;
	private final TrattamentoRepository trattamentoRepository;
	private final CredenzialiRepository credenzialiRepository;

	public RecensioneService(RecensioneRepository recensioneRepository, AppuntamentoRepository appuntamentoRepository,
			TrattamentoRepository trattamentoRepository, CredenzialiRepository credenzialiRepository) {
		this.recensioneRepository = recensioneRepository;
		this.appuntamentoRepository = appuntamentoRepository;
		this.trattamentoRepository = trattamentoRepository;
		this.credenzialiRepository = credenzialiRepository;
	}

	@Transactional(readOnly = true)
	public Recensione findById(Long id) {
		return this.recensioneRepository.findById(id)
				.orElseThrow(() -> new RecensioneNotFoundException(id));
	}

	// La recensione di un appuntamento, oppure null se non ne ha
	@Transactional(readOnly = true)
	public Recensione delAppuntamento(Long appuntamentoId) {
		return this.recensioneRepository.findByAppuntamentoId(appuntamentoId).orElse(null);
	}

	// Recensioni di un trattamento (404 se il trattamento non esiste), con l'autrice gia' caricata
	@Transactional(readOnly = true)
	public List<Recensione> delTrattamento(Long trattamentoId) {
		this.trattamentoRepository.findById(trattamentoId)
				.orElseThrow(() -> new TrattamentoNotFoundException(trattamentoId));
		return this.recensioneRepository.findByAppuntamentoOffertaTrattamentoIdOrderByDataDescIdDesc(trattamentoId);
	}

	// Le recensioni scritte dalla cliente collegata
	@Transactional(readOnly = true)
	public List<Recensione> delleUtente(String username) {
		return this.recensioneRepository.findByAppuntamentoUtenteId(utenteDi(username).getId());
	}

	@Transactional(readOnly = true)
	public Double mediaVoti(Long trattamentoId) {
		return this.recensioneRepository.mediaVoti(trattamentoId);
	}

	@Transactional(readOnly = true)
	public long numero(Long trattamentoId) {
		return this.recensioneRepository.countByAppuntamentoOffertaTrattamentoId(trattamentoId);
	}

	// L'appuntamento appartiene alla cliente collegata?
	@Transactional(readOnly = true)
	public boolean isProprietaria(Long appuntamentoId, String username) {
		return caricaAppuntamento(appuntamentoId).getUtente().getId().equals(utenteDi(username).getId());
	}

	// INSERIMENTO. Regole: l'appuntamento e' della cliente collegata, e' COMPLETATO e non ha ancora una recensione.
	// Controllo qui (messaggio chiaro) e vincolo unique sul database (difesa anche con due richieste simultanee).
	@Transactional
	public Recensione inserisci(Long appuntamentoId, String username, Recensione dati) {
		Appuntamento appuntamento = verificaRecensibile(appuntamentoId, username);
		// L'appuntamento NON arriva dal modulo: si prende dall'indirizzo e si controlla che sia di chi e' collegata.
		Recensione recensione = new Recensione();
		recensione.setTesto(dati.getTesto());
		recensione.setVoto(dati.getVoto());
		recensione.setData(LocalDate.now());
		recensione.setAppuntamento(appuntamento);
		return this.recensioneRepository.save(recensione);
	}

	// Le tre regole per scrivere una recensione (usate dal modulo e dal salvataggio); restituisce l'appuntamento
	@Transactional(readOnly = true)
	public Appuntamento verificaRecensibile(Long appuntamentoId, String username) {
		Appuntamento appuntamento = caricaAppuntamento(appuntamentoId);
		if (!appuntamento.getUtente().getId().equals(utenteDi(username).getId())) {
			throw new OperazioneNonConsentitaException("Puoi recensire solo i tuoi appuntamenti");
		}
		if (appuntamento.getStato() != Stato.COMPLETATO) {
			throw new OperazioneNonConsentitaException("Si possono recensire solo gli appuntamenti completati");
		}
		if (this.recensioneRepository.existsByAppuntamentoId(appuntamentoId)) {
			throw new OperazioneNonConsentitaException("Hai già scritto una recensione per questo appuntamento");
		}
		return appuntamento;
	}

	@Transactional
	public Recensione modifica(Long id, String username, Recensione dati) {
		Recensione recensione = findById(id);
		if (!isAutrice(recensione, username)) {
			throw new OperazioneNonConsentitaException("Puoi modificare solo le tue recensioni");
		}
		recensione.setTesto(dati.getTesto());
		recensione.setVoto(dati.getVoto());
		return this.recensioneRepository.save(recensione);
	}

	@Transactional
	public void elimina(Long id, String username) {
		Recensione recensione = findById(id);
		if (!isAutrice(recensione, username)) {
			throw new OperazioneNonConsentitaException("Puoi eliminare solo le tue recensioni");
		}
		this.recensioneRepository.delete(recensione);
	}

	@Transactional(readOnly = true)
	public boolean isAutrice(Recensione recensione, String username) {
		return recensione.getAppuntamento().getUtente().getId().equals(utenteDi(username).getId());
	}

	private Appuntamento caricaAppuntamento(Long id) {
		return this.appuntamentoRepository.findById(id).orElseThrow(() -> new AppuntamentoNotFoundException(id));
	}

	private Utente utenteDi(String username) {
		return this.credenzialiRepository.findByUsername(username)
				.orElseThrow(() -> new OperazioneNonConsentitaException("Utente non trovato"))
				.getUtente();
	}
}
