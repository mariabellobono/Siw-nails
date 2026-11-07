package it.uniroma3.siw.repository;

import java.util.List;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import it.uniroma3.siw.model.Offerta;

public interface OffertaRepository extends CrudRepository<Offerta, Long> {

	// Esiste gia' una offerta per questo evento e questo trattamento? (la coppia e' unica)
	boolean existsByEventoIdAndTrattamentoId(Long eventoId, Long trattamentoId);

	// Tutte le offerte con evento e trattamento in una sola query: servono alla tendina del modulo di prenotazione
	@EntityGraph(attributePaths = { "evento", "trattamento" })
	@Query("select o from Offerta o order by o.evento.dataInizio, o.trattamento.nome")
	List<Offerta> findAllConDettagli();

	// L'offerta di un trattamento valida in una certa data: quella il cui evento comprende la data.
	// Serve alla prenotazione: la cliente sceglie il trattamento e la data, l'evento si ricava da qui.
	@EntityGraph(attributePaths = { "evento", "trattamento" })
	@Query("select o from Offerta o where o.trattamento.id = :trattamentoId and o.evento.dataInizio <= :data and o.evento.dataFine >= :data order by o.evento.dataInizio")
	List<Offerta> findByTrattamentoEData(@org.springframework.data.repository.query.Param("trattamentoId") Long trattamentoId,
			@org.springframework.data.repository.query.Param("data") java.time.LocalDate data);
}
