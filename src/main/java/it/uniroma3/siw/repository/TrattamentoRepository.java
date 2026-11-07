package it.uniroma3.siw.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.repository.CrudRepository;

import it.uniroma3.siw.model.Trattamento;

public interface TrattamentoRepository extends CrudRepository<Trattamento, Long> {

	List<Trattamento> findAllByOrderByNome();

	// Un trattamento con le sue offerte e l'evento di ciascuna, in una sola query
	@EntityGraph(attributePaths = { "offerte", "offerte.evento" })
	Optional<Trattamento> findConOfferteById(Long id);

	boolean existsByNome(String nome);

	boolean existsByNomeAndIdNot(String nome, Long id);
}
