package it.uniroma3.siw.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import it.uniroma3.siw.dto.OffertaForm;
import it.uniroma3.siw.exception.EventoNotFoundException;
import it.uniroma3.siw.exception.OffertaNotFoundException;
import it.uniroma3.siw.exception.OperazioneNonConsentitaException;
import it.uniroma3.siw.exception.TrattamentoNotFoundException;
import it.uniroma3.siw.model.Evento;
import it.uniroma3.siw.model.Offerta;
import it.uniroma3.siw.model.Trattamento;
import it.uniroma3.siw.repository.EventoRepository;
import it.uniroma3.siw.repository.OffertaRepository;
import it.uniroma3.siw.repository.TrattamentoRepository;

@Service
public class OffertaService {

	private final OffertaRepository offertaRepository;
	private final EventoRepository eventoRepository;
	private final TrattamentoRepository trattamentoRepository;

	public OffertaService(OffertaRepository offertaRepository, EventoRepository eventoRepository,
			TrattamentoRepository trattamentoRepository) {
		this.offertaRepository = offertaRepository;
		this.eventoRepository = eventoRepository;
		this.trattamentoRepository = trattamentoRepository;
	}

	@Transactional(readOnly = true)
	public Offerta findById(Long id) {
		return this.offertaRepository.findById(id)
				.orElseThrow(() -> new OffertaNotFoundException(id));
	}

	// Per la tendina della prenotazione: evento e trattamento gia' caricati (1 query)
	@Transactional(readOnly = true)
	public List<Offerta> findAllConDettagli() {
		return this.offertaRepository.findAllConDettagli();
	}

	@Transactional(readOnly = true)
	public boolean existsByEventoIdAndTrattamentoId(Long eventoId, Long trattamentoId) {
		return this.offertaRepository.existsByEventoIdAndTrattamentoId(eventoId, trattamentoId);
	}

	@Transactional
	public Offerta save(Offerta offerta) {
		return this.offertaRepository.save(offerta);
	}

	// INSERIMENTO: aggiunge un trattamento a un evento con il suo prezzo e il suo tema.
	// Operazione su piu' entita' (evento, trattamento, offerta): se qualcosa fallisce non resta nulla a meta'.
	@Transactional
	public Offerta crea(OffertaForm form) {
		Evento evento = this.eventoRepository.findById(form.getEventoId())
				.orElseThrow(() -> new EventoNotFoundException(form.getEventoId()));
		Trattamento trattamento = this.trattamentoRepository.findById(form.getTrattamentoId())
				.orElseThrow(() -> new TrattamentoNotFoundException(form.getTrattamentoId()));
		if (this.offertaRepository.existsByEventoIdAndTrattamentoId(evento.getId(), trattamento.getId())) {
			throw new OperazioneNonConsentitaException("Questo trattamento e gia presente nell'evento");
		}
		Offerta offerta = new Offerta();
		offerta.setPrezzo(form.getPrezzo());
		offerta.setTema(form.getTema());
		offerta.setDescrizione(form.getDescrizione());
		offerta.setEvento(evento);
		offerta.setTrattamento(trattamento);
		this.offertaRepository.save(offerta);
		evento.getOfferte().add(offerta);
		trattamento.getOfferte().add(offerta);
		return offerta;
	}

	// ELIMINAZIONE: un'offerta con appuntamenti (anche annullati: restano nello storico) non si cancella.
	// Restituisce l'id dell'evento, cosi' il controller sa dove tornare.
	@Transactional
	public Long elimina(Long id) {
		Offerta offerta = findById(id);
		if (!offerta.getAppuntamenti().isEmpty()) {
			throw new OperazioneNonConsentitaException("L'offerta ha degli appuntamenti e non puo essere eliminata");
		}
		Long eventoId = offerta.getEvento().getId();
		offerta.getEvento().getOfferte().remove(offerta);
		offerta.getTrattamento().getOfferte().remove(offerta);
		this.offertaRepository.delete(offerta);
		return eventoId;
	}
}
