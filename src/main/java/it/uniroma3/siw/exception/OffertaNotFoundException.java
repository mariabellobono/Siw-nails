package it.uniroma3.siw.exception;

public class OffertaNotFoundException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public OffertaNotFoundException(Long id) {
		super("L'offerta con ID " + id + " non esiste.");
	}
}
