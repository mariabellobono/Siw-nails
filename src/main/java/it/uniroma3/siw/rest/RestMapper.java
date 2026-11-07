package it.uniroma3.siw.rest;

import java.util.List;

import it.uniroma3.siw.dto.ApiDto.AppuntamentoDto;
import it.uniroma3.siw.dto.ApiDto.EventoDettaglioDto;
import it.uniroma3.siw.dto.ApiDto.EventoDto;
import it.uniroma3.siw.dto.ApiDto.OffertaDto;
import it.uniroma3.siw.dto.ApiDto.OperatriceDto;
import it.uniroma3.siw.dto.ApiDto.RecensioneDto;
import it.uniroma3.siw.dto.ApiDto.TrattamentoDettaglioDto;
import it.uniroma3.siw.dto.ApiDto.TrattamentoDto;
import it.uniroma3.siw.model.Appuntamento;
import it.uniroma3.siw.model.Evento;
import it.uniroma3.siw.model.Offerta;
import it.uniroma3.siw.model.Operatrice;
import it.uniroma3.siw.model.Recensione;
import it.uniroma3.siw.model.Trattamento;

// Trasforma le ENTITA' in DTO (e quindi in JSON). Qui si decide cosa mostrare all'esterno.
// Regola: si leggono solo campi semplici e relazioni ManyToOne (gia' caricate); le liste LAZY
// (evento.offerte, trattamento.offerte...) le carica il service/repository nella sua transazione (EntityGraph).
final class RestMapper {

	private RestMapper() {
	}

	static EventoDto evento(Evento e) {
		return new EventoDto(e.getId(), e.getNome(), e.getAnno(), e.getCitta(), e.getDataInizio(), e.getDataFine(),
				e.getDescrizione());
	}

	// l'evento deve arrivare con le offerte gia' caricate (EventoService.findById)
	static EventoDettaglioDto eventoDettaglio(Evento e) {
		List<OffertaDto> offerte = e.getOfferte().stream().map(RestMapper::offerta).toList();
		return new EventoDettaglioDto(e.getId(), e.getNome(), e.getAnno(), e.getCitta(), e.getDataInizio(),
				e.getDataFine(), e.getDescrizione(), offerte);
	}

	static OffertaDto offerta(Offerta o) {
		return new OffertaDto(o.getId(), o.getTema(), o.getPrezzo(), o.getDescrizione(), o.getEvento().getId(),
				o.getEvento().getNome(), o.getTrattamento().getId(), o.getTrattamento().getNome(),
				o.getTrattamento().getDurata());
	}

	static TrattamentoDto trattamento(Trattamento t) {
		return new TrattamentoDto(t.getId(), t.getNome(), t.getCategoria(), t.getDurata(), t.getDescrizione());
	}

	// il trattamento deve arrivare con le offerte gia' caricate (TrattamentoService.findById)
	static TrattamentoDettaglioDto trattamentoDettaglio(Trattamento t, Double mediaVoti, long numeroRecensioni) {
		List<OffertaDto> offerte = t.getOfferte().stream().map(RestMapper::offerta).toList();
		Double media = mediaVoti == null ? null : Math.round(mediaVoti * 10.0) / 10.0; // 1 decimale
		return new TrattamentoDettaglioDto(t.getId(), t.getNome(), t.getCategoria(), t.getDurata(),
				t.getDescrizione(), media, numeroRecensioni, offerte);
	}

	static OperatriceDto operatrice(Operatrice o) {
		return new OperatriceDto(o.getId(), o.getNome(), o.getSpecializzazione(), o.getOraInizio(), o.getOraFine());
	}

	// recensione = null quando non c'e' (agenda pubblica)
	static AppuntamentoDto appuntamento(Appuntamento a, Recensione recensione) {
		Offerta o = a.getOfferta();
		return new AppuntamentoDto(a.getId(), a.getData(), a.getOra(), a.getStato() == null ? null : a.getStato().name(),
				o.getId(), o.getEvento().getId(), o.getEvento().getNome(), o.getTrattamento().getId(),
				o.getTrattamento().getNome(), o.getTema(), o.getPrezzo(), o.getTrattamento().getDurata(),
				a.getOperatrice().getId(), a.getOperatrice().getNome(), a.getNote(),
				recensione == null ? null : recensione(recensione));
	}

	static RecensioneDto recensione(Recensione r) {
		Appuntamento a = r.getAppuntamento();
		return new RecensioneDto(r.getId(), r.getTesto(), r.getVoto(), r.getData(), a.getId(),
				a.getOfferta().getTrattamento().getId(), a.getUtente().getNome() + " " + a.getUtente().getCognome());
	}
}
