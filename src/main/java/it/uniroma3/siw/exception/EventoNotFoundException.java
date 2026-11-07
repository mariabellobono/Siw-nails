package it.uniroma3.siw.exception;

public class EventoNotFoundException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public EventoNotFoundException(Long id) {
		super("L'evento con ID " + id + " non esiste.");
	}
}
