package it.uniroma3.siw.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import it.uniroma3.siw.dto.PrenotazioneForm;
import it.uniroma3.siw.exception.AppuntamentoNotFoundException;
import it.uniroma3.siw.exception.OperatriceNotFoundException;
import it.uniroma3.siw.exception.OperazioneNonConsentitaException;
import it.uniroma3.siw.exception.PrenotazioneNonValidaException;
import it.uniroma3.siw.model.Appuntamento;
import it.uniroma3.siw.model.Credenziali;
import it.uniroma3.siw.model.Evento;
import it.uniroma3.siw.model.Offerta;
import it.uniroma3.siw.model.Operatrice;
import it.uniroma3.siw.model.Stato;
import it.uniroma3.siw.model.Utente;
import it.uniroma3.siw.repository.AppuntamentoRepository;
import it.uniroma3.siw.repository.CredenzialiRepository;
import it.uniroma3.siw.repository.OffertaRepository;
import it.uniroma3.siw.repository.OperatriceRepository;

@Service
public class AppuntamentoService {

	private final AppuntamentoRepository appuntamentoRepository;
	private final OffertaRepository offertaRepository;
	private final OperatriceRepository operatriceRepository;
	private final CredenzialiRepository credenzialiRepository;

	public AppuntamentoService(AppuntamentoRepository appuntamentoRepository, OffertaRepository offertaRepository,
			OperatriceRepository operatriceRepository, CredenzialiRepository credenzialiRepository) {
		this.appuntamentoRepository = appuntamentoRepository;
		this.offertaRepository = offertaRepository;
		this.operatriceRepository = operatriceRepository;
		this.credenzialiRepository = credenzialiRepository;
	}

	// Per la pagina elenco: offerta, operatrice e cliente arrivano gia' caricati (1 query invece di N+1)
	@Transactional(readOnly = true)
	public List<Appuntamento> findAllConDettagli() {
		return this.appuntamentoRepository.findAllConDettagli();
	}

	@Transactional(readOnly = true)
	public Appuntamento findById(Long id) {
		return this.appuntamentoRepository.findById(id)
				.orElseThrow(() -> new AppuntamentoNotFoundException(id));
	}

	// Agenda di un evento in ordine cronologico
	@Transactional(readOnly = true)
	public List<Appuntamento> findByEvento(Long eventoId) {
		return this.appuntamentoRepository.findByOffertaEventoIdOrderByDataAscOraAsc(eventoId);
	}

	// Gli appuntamenti della cliente collegata (si parte dallo username di Spring Security)
	@Transactional(readOnly = true)
	public List<Appuntamento> findByUsername(String username) {
		Utente utente = caricaUtente(username);
		return this.appuntamentoRepository.findByUtenteIdOrderByDataAscOraAsc(utente.getId());
	}

	// L'appuntamento (404 se non esiste) e' della cliente collegata? Serve all'API per rispondere 403.
	@Transactional(readOnly = true)
	public boolean isProprietario(Long id, String username) {
		return findById(id).getUtente().getId().equals(caricaUtente(username).getId());
	}

	// Serve alla pagina di dettaglio per mostrare "Sposta" e "Annulla": solo se l'appuntamento e' ancora
	// prenotato e chi guarda e' la cliente che l'ha preso (o un admin).
	@Transactional(readOnly = true)
	public boolean puoGestire(Appuntamento appuntamento, String username, boolean admin) {
		if (username == null || appuntamento.getStato() != Stato.PRENOTATO) {
			return false;
		}
		return admin || appuntamento.getUtente().getId().equals(caricaUtente(username).getId());
	}

	// Carica un appuntamento da modificare, dopo aver controllato proprietario e stato
	@Transactional(readOnly = true)
	public Appuntamento findPerModifica(Long id, String username, boolean admin) {
		Appuntamento appuntamento = findById(id);
		verificaGestibile(appuntamento, username, admin);
		return appuntamento;
	}

	// INSERIMENTO (caso d'uso: prenotare un appuntamento).
	// Operazione ATOMICA su piu' entita' e piu' repository (cliente, offerta, operatrice, appuntamento).
	// ISOLAMENTO SERIALIZABLE: il rischio e' la corsa "controllo e poi inserimento" fra due clienti che
	// prenotano la stessa operatrice nello stesso momento. Con READ COMMITTED (default di PostgreSQL) la seconda
	// non vedrebbe ancora l'inserimento della prima e passerebbero entrambe; con SERIALIZABLE una delle due
	// transazioni fallisce (ConcurrencyFailureException) e la cliente puo' riprovare.
	@Transactional(isolation = Isolation.SERIALIZABLE)
	public Appuntamento prenota(PrenotazioneForm form, String username) {
		Utente utente = caricaUtente(username);
		Offerta offerta = trovaOfferta(form.getTrattamentoId(), form.getData());
		Operatrice operatrice = caricaOperatrice(form.getOperatriceId());

		verificaPrenotazione(offerta, operatrice, form.getData(), form.getOra(), null);

		Appuntamento appuntamento = new Appuntamento();
		appuntamento.setData(form.getData());
		appuntamento.setOra(form.getOra());
		appuntamento.setNote(pulisci(form.getNote()));
		appuntamento.setStato(Stato.PRENOTATO);
		appuntamento.setOfferta(offerta);
		appuntamento.setOperatrice(operatrice);
		appuntamento.setUtente(utente);
		this.appuntamentoRepository.save(appuntamento);

		// lato inverso delle associazioni, per tenere coerenti gli oggetti in memoria
		offerta.getAppuntamenti().add(appuntamento);
		operatrice.getAppuntamenti().add(appuntamento);

		return appuntamento;
	}

	// AGGIORNAMENTO (caso d'uso: spostare/modificare un appuntamento): stessi controlli della prenotazione,
	// ma l'appuntamento stesso non conta come "operatrice occupata" (si escluderebbe da solo).
	@Transactional(isolation = Isolation.SERIALIZABLE)
	public Appuntamento modifica(Long id, PrenotazioneForm form, String username, boolean admin) {
		Appuntamento appuntamento = findById(id);
		verificaGestibile(appuntamento, username, admin);
		Offerta offerta = trovaOfferta(form.getTrattamentoId(), form.getData());
		Operatrice operatrice = caricaOperatrice(form.getOperatriceId());

		verificaPrenotazione(offerta, operatrice, form.getData(), form.getOra(), id);

		// si tolgono le associazioni vecchie e si mettono le nuove
		appuntamento.getOfferta().getAppuntamenti().remove(appuntamento);
		appuntamento.getOperatrice().getAppuntamenti().remove(appuntamento);

		appuntamento.setData(form.getData());
		appuntamento.setOra(form.getOra());
		appuntamento.setNote(pulisci(form.getNote()));
		appuntamento.setOfferta(offerta);
		appuntamento.setOperatrice(operatrice);

		offerta.getAppuntamenti().add(appuntamento);
		operatrice.getAppuntamenti().add(appuntamento);

		return this.appuntamentoRepository.save(appuntamento);
	}

	// ANNULLAMENTO: l'appuntamento non viene cancellato dal database ma passa ad ANNULLATO.
	// Cosi' si conserva lo storico e l'operatrice torna libera (gli ANNULLATO non contano nel controllo).
	@Transactional
	public Appuntamento annulla(Long id, String username, boolean admin) {
		Appuntamento appuntamento = findById(id);
		verificaGestibile(appuntamento, username, admin);
		appuntamento.setStato(Stato.ANNULLATO);
		return this.appuntamentoRepository.save(appuntamento);
	}

	private Utente caricaUtente(String username) {
		Credenziali credenziali = this.credenzialiRepository.findByUsername(username)
				.orElseThrow(() -> new IllegalStateException("Utente non trovato: " + username));
		return credenziali.getUtente();
	}

	// La cliente sceglie il TRATTAMENTO e la data: l'offerta (cioe' l'evento, il tema e il prezzo) e' quella del
	// trattamento il cui evento comprende quella data.
	private Offerta trovaOfferta(Long trattamentoId, LocalDate data) {
		if (data.isBefore(LocalDate.now())) {
			throw new PrenotazioneNonValidaException("Non si puo prenotare una data passata");
		}
		List<Offerta> offerte = this.offertaRepository.findByTrattamentoEData(trattamentoId, data);
		if (offerte.isEmpty()) {
			throw new PrenotazioneNonValidaException(
					"Il trattamento scelto non e previsto in nessun evento nella data " + data + ": scegli un'altra data");
		}
		return offerte.get(0);
	}

	private Operatrice caricaOperatrice(Long id) {
		return this.operatriceRepository.findById(id).orElseThrow(() -> new OperatriceNotFoundException(id));
	}

	private String pulisci(String testo) {
		return (testo == null || testo.isBlank()) ? null : testo.trim();
	}

	// Solo la proprietaria (o un admin) puo' agire, e solo su un appuntamento ancora prenotato
	private void verificaGestibile(Appuntamento appuntamento, String username, boolean admin) {
		if (!admin && !appuntamento.getUtente().getId().equals(caricaUtente(username).getId())) {
			throw new OperazioneNonConsentitaException("Questo appuntamento appartiene a un'altra cliente");
		}
		if (appuntamento.getStato() != Stato.PRENOTATO) {
			throw new PrenotazioneNonValidaException("Si possono modificare o annullare solo gli appuntamenti prenotati");
		}
	}

	// Controlli di consistenza comuni a prenotazione e modifica.
	// idDaEscludere: nella modifica e' l'id dell'appuntamento stesso (null nell'inserimento).
	private void verificaPrenotazione(Offerta offerta, Operatrice operatrice, LocalDate data, LocalTime ora,
			Long idDaEscludere) {
		// non si prenota nel passato
		if (data.isBefore(LocalDate.now())) {
			throw new PrenotazioneNonValidaException("Non si puo prenotare una data passata");
		}

		// la data deve cadere nel periodo dell'evento a cui appartiene l'offerta
		Evento evento = offerta.getEvento();
		if (data.isBefore(evento.getDataInizio()) || data.isAfter(evento.getDataFine())) {
			throw new PrenotazioneNonValidaException("La data deve essere compresa tra l'inizio e la fine dell'evento \""
					+ evento.getNome() + "\" (" + evento.getDataInizio() + " - " + evento.getDataFine() + ")");
		}

		// l'appuntamento deve stare dentro l'orario di lavoro dell'operatrice.
		// Intervallo = [inizio, inizio + durata del trattamento).
		LocalDateTime inizio = data.atTime(ora);
		LocalDateTime fine = inizio.plusMinutes(offerta.getTrattamento().getDurata());
		boolean finisceIlGiornoDopo = !fine.toLocalDate().equals(data);
		if (ora.isBefore(operatrice.getOraInizio()) || finisceIlGiornoDopo
				|| fine.toLocalTime().isAfter(operatrice.getOraFine())) {
			throw new PrenotazioneNonValidaException(operatrice.getNome() + " lavora dalle "
					+ operatrice.getOraInizio() + " alle " + operatrice.getOraFine()
					+ ": il trattamento deve iniziare e finire in quell'orario");
		}

		// l'operatrice deve essere libera. Due intervalli si sovrappongono se
		// inizioNuovo < fineEsistente E inizioEsistente < fineNuovo.
		List<Appuntamento> delGiorno = this.appuntamentoRepository.findByOperatriceIdAndData(operatrice.getId(), data);
		for (Appuntamento a : delGiorno) {
			if (a.getStato() == Stato.ANNULLATO) {
				continue; // un appuntamento annullato non occupa l'operatrice
			}
			if (idDaEscludere != null && a.getId().equals(idDaEscludere)) {
				continue; // nella modifica non si confronta l'appuntamento con se stesso
			}
			LocalDateTime aInizio = a.getData().atTime(a.getOra());
			LocalDateTime aFine = aInizio.plusMinutes(a.getOfferta().getTrattamento().getDurata());
			if (inizio.isBefore(aFine) && aInizio.isBefore(fine)) {
				throw new PrenotazioneNonValidaException(operatrice.getNome() + " e gia occupata dalle "
						+ aInizio.toLocalTime() + " alle " + aFine.toLocalTime() + " del " + a.getData());
			}
		}
	}
}
