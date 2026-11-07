package it.uniroma3.siw.exception;

// Lanciata dal service quando una prenotazione viola una regola: operatrice occupata o fuori orario,
// data fuori dal periodo dell'evento, data nel passato, appuntamento non piu' modificabile.
public class PrenotazioneNonValidaException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	public PrenotazioneNonValidaException(String messaggio) {
		super(messaggio);
	}
}
