package it.uniroma3.siw.model;

import java.time.LocalDate;
import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

// RECENSIONE: la cliente valuta un appuntamento che ha gia' fatto (voto da 1 a 5).
// Una sola recensione per appuntamento (unique sulla colonna appuntamento_id).
// Cliente e trattamento NON si salvano di nuovo: si ricavano dall'appuntamento
// (recensione.appuntamento.utente, recensione.appuntamento.offerta.trattamento).
@Entity
public class Recensione extends EntitaBase {

	@NotBlank(message = "Compila il campo vuoto") @Size(max = 255, message = "Massimo 255 caratteri")
	private String testo;

	@Column(nullable = false)
	@NotNull(message = "Compila il campo vuoto") @Min(value = 1, message = "Il voto deve essere compreso tra 1 e 5") @Max(value = 5, message = "Il voto deve essere compreso tra 1 e 5")
	private Integer voto;

	private LocalDate data;

	// La Recensione possiede la chiave esterna. Nessuna lista inversa nell'Appuntamento: una @OneToOne inversa
	// verrebbe caricata subito per ogni appuntamento (query in piu', N+1); la recensione si cerca con il repository.
	@OneToOne
	@JoinColumn(unique = true, nullable = false)
	private Appuntamento appuntamento;

	public String getTesto() {
		return testo;
	}

	public void setTesto(String testo) {
		this.testo = testo;
	}

	public Integer getVoto() {
		return voto;
	}

	public void setVoto(Integer voto) {
		this.voto = voto;
	}

	public LocalDate getData() {
		return data;
	}

	public void setData(LocalDate data) {
		this.data = data;
	}

	public Appuntamento getAppuntamento() {
		return appuntamento;
	}

	public void setAppuntamento(Appuntamento appuntamento) {
		this.appuntamento = appuntamento;
	}

	@Override
	public int hashCode() {
		return Objects.hash(appuntamento);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null || getClass() != obj.getClass())
			return false;
		Recensione other = (Recensione) obj;
		return Objects.equals(appuntamento, other.appuntamento);
	}

}
