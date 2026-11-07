package it.uniroma3.siw.repository;

import java.util.Optional;

import org.springframework.data.repository.CrudRepository;

import it.uniroma3.siw.model.Credenziali;

public interface CredenzialiRepository extends CrudRepository<Credenziali, Long> {

	// Query derivata dal nome del metodo: "cerca per username". Optional perche' potrebbe non esistere.
	Optional<Credenziali> findByUsername(String username);

	// Serve alla registrazione per sapere se lo username e' gia' preso.
	boolean existsByUsername(String username);

}
