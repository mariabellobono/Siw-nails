package it.uniroma3.siw.exception;

public class RecensioneNotFoundException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	public RecensioneNotFoundException(Long id) {
		super("La recensione con ID " + id + " non esiste.");
	}
}
