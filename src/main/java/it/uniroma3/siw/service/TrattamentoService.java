package it.uniroma3.siw.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import it.uniroma3.siw.exception.TrattamentoNotFoundException;
import it.uniroma3.siw.model.Trattamento;
import it.uniroma3.siw.repository.TrattamentoRepository;

@Service
public class TrattamentoService {

	private final TrattamentoRepository trattamentoRepository;

	public TrattamentoService(TrattamentoRepository trattamentoRepository) {
		this.trattamentoRepository = trattamentoRepository;
	}

	@Transactional(readOnly = true)
	public List<Trattamento> findAll() {
		return this.trattamentoRepository.findAllByOrderByNome();
	}

	// Il trattamento arriva con le offerte (e gli eventi) gia' caricati
	@Transactional(readOnly = true)
	public Trattamento findById(Long id) {
		return this.trattamentoRepository.findConOfferteById(id)
				.orElseThrow(() -> new TrattamentoNotFoundException(id));
	}

	@Transactional
	public Trattamento save(Trattamento trattamento) {
		return this.trattamentoRepository.save(trattamento);
	}

	@Transactional(readOnly = true)
	public boolean existsByNome(String nome) {
		return this.trattamentoRepository.existsByNome(nome);
	}

	@Transactional(readOnly = true)
	public boolean existsByNomeAndIdNot(String nome, Long id) {
		return this.trattamentoRepository.existsByNomeAndIdNot(nome, id);
	}

	// MODIFICA: prezzo e tema NON sono qui (stanno nelle offerte), quindi si copiano solo i dati del trattamento
	@Transactional
	public Trattamento aggiorna(Long id, Trattamento dati) {
		Trattamento trattamento = this.trattamentoRepository.findById(id)
				.orElseThrow(() -> new TrattamentoNotFoundException(id));
		trattamento.setNome(dati.getNome());
		trattamento.setCategoria(dati.getCategoria());
		trattamento.setDurata(dati.getDurata());
		trattamento.setDescrizione(dati.getDescrizione());
		return this.trattamentoRepository.save(trattamento);
	}
}
