package it.uniroma3.siw.exception;

public class TrattamentoNotFoundException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public TrattamentoNotFoundException(Long id) {
		super("Il trattamento con ID " + id + " non esiste.");
	}
}
