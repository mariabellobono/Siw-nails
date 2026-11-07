package it.uniroma3.siw.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import it.uniroma3.siw.exception.EventoNotFoundException;
import it.uniroma3.siw.model.Evento;
import it.uniroma3.siw.repository.EventoRepository;

@Service
public class EventoService {

	private final EventoRepository eventoRepository;

	public EventoService(EventoRepository eventoRepository) {
		this.eventoRepository = eventoRepository;
	}

	@Transactional(readOnly = true)
	public List<Evento> findAll() {
		return this.eventoRepository.findAllByOrderByDataInizio();
	}

	// L'evento arriva con le offerte (e i trattamenti) gia' caricati: la pagina puo' mostrarli senza altre query
	@Transactional(readOnly = true)
	public Evento findById(Long id) {
		return this.eventoRepository.findConOfferteById(id)
				.orElseThrow(() -> new EventoNotFoundException(id));
	}

	@Transactional
	public Evento save(Evento evento) {
		return this.eventoRepository.save(evento);
	}

	// MODIFICA: si carica l'evento dal database e si copiano solo i campi del modulo
	// (cosi' la lista delle offerte non viene toccata)
	@Transactional
	public Evento aggiorna(Long id, Evento dati) {
		Evento evento = this.eventoRepository.findById(id)
				.orElseThrow(() -> new EventoNotFoundException(id));
		evento.setNome(dati.getNome());
		evento.setAnno(dati.getAnno());
		evento.setCitta(dati.getCitta());
		evento.setDataInizio(dati.getDataInizio());
		evento.setDataFine(dati.getDataFine());
		evento.setDescrizione(dati.getDescrizione());
		evento.setImmagine(dati.getImmagine());
		return this.eventoRepository.save(evento);
	}
}
