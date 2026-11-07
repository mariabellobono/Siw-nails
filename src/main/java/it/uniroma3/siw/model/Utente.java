package it.uniroma3.siw.model;

import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

// ANAGRAFICA: chi e' la persona. Le credenziali di accesso (username, password, ruolo)
// stanno nella classe Credenziali (slide 10: User + Credentials).
// Perche' separate: l'anagrafica serve alle pagine (es. "appuntamento di Maria Rossi"),
// le credenziali servono solo al login. Alternativa: tutto in una classe sola (piu' semplice,
// ma la password viaggerebbe insieme ai dati della persona).
@Entity
public class Utente extends EntitaBase {

	@NotBlank(message = "Compila il campo vuoto")
	private String nome;

	@NotBlank(message = "Compila il campo vuoto")
	private String cognome;

	@Column(unique = true, nullable = false)
	@NotBlank(message = "Compila il campo vuoto")
	@Email(message = "Inserisci una email valida")
	private String email;

	public String getNome() {
		return nome;
	}

	public void setNome(String nome) {
		this.nome = nome;
	}

	public String getCognome() {
		return cognome;
	}

	public void setCognome(String cognome) {
		this.cognome = cognome;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	// L'email identifica la persona (e' unica): e' il campo naturale per equals/hashCode.
	@Override
	public int hashCode() {
		return Objects.hash(email);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null || getClass() != obj.getClass())
			return false;
		Utente other = (Utente) obj;
		return Objects.equals(email, other.email);
	}
}
