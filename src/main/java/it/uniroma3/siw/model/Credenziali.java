package it.uniroma3.siw.model;

import java.util.Objects;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.OneToOne;
import jakarta.validation.constraints.NotBlank;

// ACCESSO: come entra e con quali permessi (usata da Spring Security).
// Ogni persona (Utente) ha una sola Credenziali e viceversa: relazione uno a uno.
@Entity
public class Credenziali extends EntitaBase {

	@Column(unique = true, nullable = false)
	@NotBlank(message = "Compila il campo vuoto")
	private String username;

	// Nel database NON si salva la password in chiaro ma il suo hash (BCrypt, blocco 2).
	@Column(nullable = false)
	@NotBlank(message = "Compila il campo vuoto")
	private String password;

	// Enum invece di una String: un errore di battitura (ADMN) non compila.
	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private Ruolo ruolo;

	// cascade = ALL: salvando le credenziali Hibernate salva anche l'utente collegato.
	// Alternativa: salvare i due oggetti a mano, ma un errore a meta' lascerebbe dati incompleti.
	@OneToOne(cascade = CascadeType.ALL)
	private Utente utente;

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public Ruolo getRuolo() {
		return ruolo;
	}

	public void setRuolo(Ruolo ruolo) {
		this.ruolo = ruolo;
	}

	public Utente getUtente() {
		return utente;
	}

	public void setUtente(Utente utente) {
		this.utente = utente;
	}

	@Override
	public int hashCode() {
		return Objects.hash(username);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null || getClass() != obj.getClass())
			return false;
		Credenziali other = (Credenziali) obj;
		return Objects.equals(username, other.username);
	}
}
