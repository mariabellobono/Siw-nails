package it.uniroma3.siw.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.repository.CrudRepository;

import it.uniroma3.siw.model.Operatrice;

public interface OperatriceRepository extends CrudRepository<Operatrice, Long> {

	List<Operatrice> findAllByOrderByNome();

	// Un'operatrice con i suoi appuntamenti e tutto cio' che la pagina mostra di ciascuno, in una sola query
	@EntityGraph(attributePaths = { "appuntamenti", "appuntamenti.offerta", "appuntamenti.offerta.trattamento",
			"appuntamenti.offerta.evento", "appuntamenti.utente" })
	Optional<Operatrice> findConAppuntamentiById(Long id);
}
