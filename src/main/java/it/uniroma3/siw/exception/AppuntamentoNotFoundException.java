package it.uniroma3.siw.exception;

public class AppuntamentoNotFoundException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public AppuntamentoNotFoundException(Long id) {
		super("L'appuntamento con ID " + id + " non esiste.");
	}
}
