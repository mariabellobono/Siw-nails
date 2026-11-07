package it.uniroma3.siw.exception;

public class OperatriceNotFoundException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public OperatriceNotFoundException(Long id) {
		super("L'operatrice con ID " + id + " non esiste.");
	}
}
