package it.uniroma3.siw.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import it.uniroma3.siw.model.Recensione;

public interface RecensioneRepository extends CrudRepository<Recensione, Long> {

	// La recensione di un appuntamento (al massimo una: vincolo unique sul database), con l'autrice gia' caricata
	@EntityGraph(attributePaths = { "appuntamento", "appuntamento.utente" })
	Optional<Recensione> findByAppuntamentoId(Long appuntamentoId);

	boolean existsByAppuntamentoId(Long appuntamentoId);

	// Recensioni di un trattamento, dalla piu' recente. Si scende con le relazioni: recensione > appuntamento > offerta > trattamento.
	// L'autrice (appuntamento.utente) arriva nella stessa query (EntityGraph).
	@EntityGraph(attributePaths = { "appuntamento", "appuntamento.utente" })
	List<Recensione> findByAppuntamentoOffertaTrattamentoIdOrderByDataDescIdDesc(Long trattamentoId);

	// Le recensioni scritte da una cliente (una query sola): servono a /api/miei-appuntamenti
	@EntityGraph(attributePaths = { "appuntamento", "appuntamento.utente", "appuntamento.offerta" })
	List<Recensione> findByAppuntamentoUtenteId(Long utenteId);

	long countByAppuntamentoOffertaTrattamentoId(Long trattamentoId);

	// Media dei voti calcolata dal database (null se il trattamento non ha recensioni)
	@Query("select avg(r.voto) from Recensione r where r.appuntamento.offerta.trattamento.id = :trattamentoId")
	Double mediaVoti(@Param("trattamentoId") Long trattamentoId);
}
