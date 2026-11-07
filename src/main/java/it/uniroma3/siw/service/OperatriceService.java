package it.uniroma3.siw.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import it.uniroma3.siw.exception.OperatriceNotFoundException;
import it.uniroma3.siw.model.Operatrice;
import it.uniroma3.siw.repository.OperatriceRepository;

@Service
public class OperatriceService {

	private final OperatriceRepository operatriceRepository;

	public OperatriceService(OperatriceRepository operatriceRepository) {
		this.operatriceRepository = operatriceRepository;
	}

	@Transactional(readOnly = true)
	public List<Operatrice> findAll() {
		return this.operatriceRepository.findAllByOrderByNome();
	}

	// L'operatrice arriva con gli appuntamenti gia' caricati
	@Transactional(readOnly = true)
	public Operatrice findById(Long id) {
		return this.operatriceRepository.findConAppuntamentiById(id)
				.orElseThrow(() -> new OperatriceNotFoundException(id));
	}

	@Transactional
	public Operatrice save(Operatrice operatrice) {
		return this.operatriceRepository.save(operatrice);
	}

	@Transactional
	public Operatrice aggiorna(Long id, Operatrice dati) {
		Operatrice operatrice = this.operatriceRepository.findById(id)
				.orElseThrow(() -> new OperatriceNotFoundException(id));
		operatrice.setNome(dati.getNome());
		operatrice.setSpecializzazione(dati.getSpecializzazione());
		operatrice.setOraInizio(dati.getOraInizio());
		operatrice.setOraFine(dati.getOraFine());
		return this.operatriceRepository.save(operatrice);
	}
}
