package it.uniroma3.siw.exception;

// Lanciata dai service quando un'operazione viola una regola (es. offerta con appuntamenti che non si puo' eliminare,
// appuntamento di un'altra cliente). E' una RuntimeException: dentro un metodo @Transactional provoca il rollback.
public class OperazioneNonConsentitaException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	public OperazioneNonConsentitaException(String messaggio) {
		super(messaggio);
	}
}
