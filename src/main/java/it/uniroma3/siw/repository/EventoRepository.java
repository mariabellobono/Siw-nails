package it.uniroma3.siw.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.repository.CrudRepository;

import it.uniroma3.siw.model.Evento;

public interface EventoRepository extends CrudRepository<Evento, Long> {

	// Tutti gli eventi, dal piu' vicino nel tempo (OrderByDataInizio = ORDER BY data_inizio)
	List<Evento> findAllByOrderByDataInizio();

	// Un evento con le sue offerte E il trattamento di ogni offerta, in una sola query (EntityGraph = "carica anche questi").
	// Senza il grafo, la lista offerte (LAZY) e il trattamento di ciascuna causerebbero tante query (N+1).
	@EntityGraph(attributePaths = { "offerte", "offerte.trattamento" })
	Optional<Evento> findConOfferteById(Long id);
}
