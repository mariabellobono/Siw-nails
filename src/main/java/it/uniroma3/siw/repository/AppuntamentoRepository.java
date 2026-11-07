package it.uniroma3.siw.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import it.uniroma3.siw.model.Appuntamento;

public interface AppuntamentoRepository extends CrudRepository<Appuntamento, Long> {

	// Tutti gli appuntamenti CON offerta (evento e trattamento), operatrice e cliente in UNA SOLA query.
	// Senza il grafo ogni appuntamento causerebbe query in piu' per ciascuna di queste relazioni (N+1).
	@EntityGraph(attributePaths = { "offerta", "offerta.evento", "offerta.trattamento", "operatrice", "utente" })
	@Query("select a from Appuntamento a order by a.data, a.ora")
	List<Appuntamento> findAllConDettagli();

	// Agenda di un evento, in ordine cronologico. "OffertaEventoId" = a.offerta.evento.id (si scende con le relazioni).
	@EntityGraph(attributePaths = { "offerta", "offerta.evento", "offerta.trattamento", "operatrice", "utente" })
	List<Appuntamento> findByOffertaEventoIdOrderByDataAscOraAsc(Long eventoId);

	// Appuntamenti di una operatrice in un giorno: serve a controllare che non si sovrappongano
	@EntityGraph(attributePaths = { "offerta", "offerta.trattamento" })
	List<Appuntamento> findByOperatriceIdAndData(Long operatriceId, LocalDate data);

	// Gli appuntamenti di una cliente, in ordine cronologico
	@EntityGraph(attributePaths = { "offerta", "offerta.evento", "offerta.trattamento", "operatrice" })
	List<Appuntamento> findByUtenteIdOrderByDataAscOraAsc(Long utenteId);
}
