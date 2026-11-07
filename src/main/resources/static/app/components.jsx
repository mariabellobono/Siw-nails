// COMPONENTI REACT. Un componente e' una funzione che restituisce un pezzo di pagina (JSX = HTML dentro JavaScript).
// useState = una variabile che, quando cambia, ridisegna il componente.
// useEffect = codice che parte quando il componente compare o quando cambia qualcosa (qui: le chiamate all'API).
const { useState, useEffect } = React;

// cambia pagina: il percorso sta dopo il # dell'indirizzo (es. #/eventi/3)
function vaiA(percorso) {
  window.location.hash = '#' + percorso;
}

function Errore({ messaggio }) {
  return messaggio ? <div className="errore">{messaggio}</div> : null;
}

function ErroreCampo({ campi, nome }) {
  return campi && campi[nome] ? <span className="campo-errore">{campi[nome]}</span> : null;
}

function euro(n) {
  return Number(n).toFixed(2) + ' €';
}

// ---------- LOGIN ----------
function LoginBox({ utente, setUtente }) {
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [errore, setErrore] = useState(null);

  function accedi(e) {
    e.preventDefault();
    api.post('/api/auth/login', { username: username, password: password })
      .then((u) => {
        setUtente(u);
        setPassword('');
        setErrore(null);
      })
      .catch((err) => setErrore(err.message));
  }

  function esci() {
    api.post('/api/auth/logout')
      .then(() => { setUtente({ authenticated: false, admin: false }); vaiA('/'); })
      .catch((err) => setErrore(err.message));
  }

  if (utente && utente.authenticated) {
    return (
      <div>
        Ciao <b>{utente.nome || utente.username}</b>{utente.admin ? ' (admin)' : ''}{' '}
        <button className="secondario" onClick={esci}>Esci</button>
      </div>
    );
  }
  return (
    <form className="riga" onSubmit={accedi}>
      <input placeholder="Username" value={username} onChange={(e) => setUsername(e.target.value)} />
      <input type="password" placeholder="Password" value={password} onChange={(e) => setPassword(e.target.value)} />
      <button type="submit">Accedi</button>
      <a href="/register" style={{ color: '#fff' }}>Registrati</a>
      {errore && <span style={{ color: '#ffb4ab' }}>{errore}</span>}
    </form>
  );
}

// ---------- EVENTI ----------
function ListaEventi() {
  const [eventi, setEventi] = useState(null);
  const [errore, setErrore] = useState(null);

  useEffect(() => {
    api.get('/api/eventi').then(setEventi).catch((e) => setErrore(e.message));
  }, []);

  return (
    <div>
      <h1>Eventi</h1>
      <Errore messaggio={errore} />
      {!eventi && !errore && <p>Caricamento...</p>}
      {eventi && eventi.map((e) => (
        <div className="card evento-riga" key={e.id}>
          <img className="simbolo" src={e.immagine} alt="" />
          <div>
            <a href={'#/eventi/' + e.id}><b>{e.nome}</b> ({e.anno})</a>
            <div className="piccolo">{e.citta} &middot; dal {e.dataInizio} al {e.dataFine}</div>
          </div>
        </div>
      ))}
    </div>
  );
}

function ElencoOfferte({ offerte, mostra }) {
  if (offerte.length === 0) return <p>Nessun trattamento previsto.</p>;
  return (
    <table>
      <thead>
        <tr><th>{mostra === 'evento' ? 'Evento' : 'Trattamento'}</th><th>Tema</th><th>Durata</th><th>Prezzo</th><th></th></tr>
      </thead>
      <tbody>
        {offerte.map((o) => (
          <tr key={o.id}>
            <td>
              {mostra === 'evento'
                ? <a href={'#/eventi/' + o.eventoId}>{o.eventoNome}</a>
                : <a href={'#/trattamenti/' + o.trattamentoId}>{o.trattamentoNome}</a>}
            </td>
            <td><span className="badge">{o.tema}</span></td>
            <td>{o.durata} min</td>
            <td className="prezzo">{euro(o.prezzo)}</td>
            <td><button onClick={() => vaiA('/prenota/' + o.trattamentoId)}>Prenota</button></td>
          </tr>
        ))}
      </tbody>
    </table>
  );
}

function DettaglioEvento({ id }) {
  const [evento, setEvento] = useState(null);
  const [errore, setErrore] = useState(null);

  useEffect(() => {
    api.get('/api/eventi/' + id).then(setEvento).catch((e) => setErrore(e.message));
  }, [id]);

  if (!evento) {
    return errore ? <Errore messaggio={errore} /> : <p>Caricamento...</p>;
  }
  return (
    <div>
      <p><a href="#/">&larr; Tutti gli eventi</a></p>
      <div className="card">
        <img className="simbolo grande" src={evento.immagine} alt="" />
        <h1>{evento.nome} <span className="piccolo">({evento.anno})</span></h1>
        <p>{evento.citta} &middot; dal {evento.dataInizio} al {evento.dataFine}</p>
        <p>{evento.descrizione}</p>
      </div>
      <h2>Trattamenti in questo evento</h2>
      <ElencoOfferte offerte={evento.offerte} mostra="trattamento" />
    </div>
  );
}

// ---------- TRATTAMENTI ----------
function ListaTrattamenti() {
  const [trattamenti, setTrattamenti] = useState(null);
  const [errore, setErrore] = useState(null);

  useEffect(() => {
    api.get('/api/trattamenti').then(setTrattamenti).catch((e) => setErrore(e.message));
  }, []);

  return (
    <div>
      <h1>Trattamenti</h1>
      <Errore messaggio={errore} />
      {!trattamenti && !errore && <p>Caricamento...</p>}
      {trattamenti && trattamenti.map((t) => (
        <div className="card" key={t.id}>
          <a href={'#/trattamenti/' + t.id}><b>{t.nome}</b></a>
          <div className="piccolo">{t.categoria} &middot; {t.durata} min</div>
        </div>
      ))}
    </div>
  );
}

function DettaglioTrattamento({ id }) {
  const [trattamento, setTrattamento] = useState(null);
  const [recensioni, setRecensioni] = useState([]);
  const [errore, setErrore] = useState(null);

  useEffect(() => {
    api.get('/api/trattamenti/' + id).then(setTrattamento).catch((e) => setErrore(e.message));
    api.get('/api/trattamenti/' + id + '/recensioni').then(setRecensioni).catch(() => {});
  }, [id]);

  if (!trattamento) {
    return errore ? <Errore messaggio={errore} /> : <p>Caricamento...</p>;
  }
  return (
    <div>
      <p><a href="#/trattamenti">&larr; Tutti i trattamenti</a></p>
      <div className="card">
        <h1>{trattamento.nome}</h1>
        <p>{trattamento.categoria} &middot; {trattamento.durata} min</p>
        <p>{trattamento.descrizione}</p>
        <p>
          {trattamento.mediaVoti !== null ? <span className="voto">&#9733; {trattamento.mediaVoti}/5</span> : 'Nessun voto'}{' '}
          <span className="piccolo">({trattamento.numeroRecensioni} recensioni)</span>
        </p>
      </div>
      <h2>Dove e a che prezzo</h2>
      <ElencoOfferte offerte={trattamento.offerte} mostra="evento" />
      <h2>Recensioni</h2>
      {recensioni.length === 0 && <p>Ancora nessuna recensione.</p>}
      {recensioni.map((r) => (
        <div className="card" key={r.id}>
          <span className="voto">&#9733; {r.voto}/5</span> &middot; <b>{r.autore}</b> <span className="piccolo">{r.data}</span>
          <p>{r.testo}</p>
        </div>
      ))}
    </div>
  );
}

// ---------- OPERATRICI E AGENDA ----------
function ListaOperatrici() {
  const [operatrici, setOperatrici] = useState(null);
  const [errore, setErrore] = useState(null);

  useEffect(() => {
    api.get('/api/operatrici').then(setOperatrici).catch((e) => setErrore(e.message));
  }, []);

  return (
    <div>
      <h1>Operatrici</h1>
      <Errore messaggio={errore} />
      {!operatrici && !errore && <p>Caricamento...</p>}
      {operatrici && operatrici.map((o) => (
        <div className="card" key={o.id}>
          <b>{o.nome}</b>
          <div className="piccolo">{o.specializzazione} &middot; dalle {o.oraInizio} alle {o.oraFine}</div>
        </div>
      ))}
    </div>
  );
}

function Agenda() {
  const [appuntamenti, setAppuntamenti] = useState(null);
  const [errore, setErrore] = useState(null);

  useEffect(() => {
    api.get('/api/appuntamenti').then(setAppuntamenti).catch((e) => setErrore(e.message));
  }, []);

  return (
    <div>
      <h1>Agenda</h1>
      <Errore messaggio={errore} />
      {!appuntamenti && !errore && <p>Caricamento...</p>}
      {appuntamenti && appuntamenti.length === 0 && <p>Nessun appuntamento.</p>}
      {appuntamenti && appuntamenti.length > 0 && (
        <table>
          <thead>
            <tr><th>Data</th><th>Ora</th><th>Trattamento</th><th>Operatrice</th><th>Evento</th><th>Stato</th></tr>
          </thead>
          <tbody>
            {appuntamenti.map((a) => (
              <tr key={a.id} className={a.stato === 'ANNULLATO' ? 'annullato' : ''}>
                <td>{a.data}</td>
                <td>{a.ora}</td>
                <td><a href={'#/trattamenti/' + a.trattamentoId}>{a.trattamentoNome}</a></td>
                <td>{a.operatriceNome}</td>
                <td><a href={'#/eventi/' + a.eventoId}>{a.eventoNome}</a></td>
                <td>{a.stato}</td>
              </tr>
            ))}
          </tbody>
        </table>
      )}
    </div>
  );
}

// ---------- PRENOTAZIONE E SPOSTAMENTO (stesso modulo) ----------
function FormPrenotazione({ trattamentoIniziale, appuntamentoId, utente }) {
  const [trattamenti, setTrattamenti] = useState([]);
  const [operatrici, setOperatrici] = useState([]);
  const [dati, setDati] = useState({ trattamentoId: trattamentoIniziale || '', operatriceId: '', data: '', ora: '', note: '' });
  const [errore, setErrore] = useState(null);
  const [campi, setCampi] = useState({});

  useEffect(() => {
    api.get('/api/trattamenti').then(setTrattamenti).catch((e) => setErrore(e.message));
    api.get('/api/operatrici').then(setOperatrici).catch((e) => setErrore(e.message));
    if (appuntamentoId) {
      // spostamento: il modulo parte con i dati attuali dell'appuntamento
      api.get('/api/appuntamenti/' + appuntamentoId)
        .then((a) => setDati({ trattamentoId: a.trattamentoId, operatriceId: a.operatriceId, data: a.data,
          ora: a.ora.substring(0, 5), note: a.note || '' }))
        .catch((e) => setErrore(e.message));
    }
  }, [appuntamentoId]);

  if (!utente) return <p>Caricamento...</p>;
  if (!utente.authenticated) {
    return <div className="errore">Per prenotare devi accedere: usa il modulo in alto.</div>;
  }

  function cambia(e) {
    setDati({ ...dati, [e.target.name]: e.target.value });
  }

  function invia(e) {
    e.preventDefault();
    const corpo = {
      trattamentoId: dati.trattamentoId === '' ? null : Number(dati.trattamentoId),
      operatriceId: dati.operatriceId === '' ? null : Number(dati.operatriceId),
      data: dati.data === '' ? null : dati.data,
      ora: dati.ora === '' ? null : dati.ora,
      note: dati.note,
    };
    const richiesta = appuntamentoId
      ? api.put('/api/appuntamenti/' + appuntamentoId, corpo)   // sposta
      : api.post('/api/appuntamenti', corpo);                    // prenota
    richiesta.then(() => vaiA('/miei')).catch((err) => { setErrore(err.message); setCampi(err.campi); });
  }

  return (
    <form className="card" onSubmit={invia}>
      <h1>{appuntamentoId ? 'Sposta appuntamento' : 'Prenota un appuntamento'}</h1>
      <Errore messaggio={errore} />
      <label>Trattamento
        <select name="trattamentoId" value={dati.trattamentoId} onChange={cambia}>
          <option value="">-- scegli un trattamento --</option>
          {trattamenti.map((t) => (
            <option key={t.id} value={t.id}>{t.nome} ({t.durata} min)</option>
          ))}
        </select>
        <ErroreCampo campi={campi} nome="trattamentoId" />
        <small>L'evento e il prezzo dipendono dalla data scelta.</small>
      </label>
      <label>Operatrice
        <select name="operatriceId" value={dati.operatriceId} onChange={cambia}>
          <option value="">-- scegli un'operatrice --</option>
          {operatrici.map((o) => (
            <option key={o.id} value={o.id}>{o.nome} ({o.oraInizio} - {o.oraFine})</option>
          ))}
        </select>
        <ErroreCampo campi={campi} nome="operatriceId" />
      </label>
      <div className="riga">
        <label>Data
          <input type="date" name="data" value={dati.data} onChange={cambia} />
          <ErroreCampo campi={campi} nome="data" />
        </label>
        <label>Ora
          <input type="time" name="ora" value={dati.ora} onChange={cambia} />
          <ErroreCampo campi={campi} nome="ora" />
        </label>
      </div>
      <label>Note
        <input name="note" value={dati.note} onChange={cambia} />
        <ErroreCampo campi={campi} nome="note" />
      </label>
      <div className="riga">
        <button type="submit">Salva</button>
        <button type="button" className="secondario" onClick={() => vaiA(appuntamentoId ? '/miei' : '/')}>Annulla</button>
      </div>
    </form>
  );
}

// ---------- I MIEI APPUNTAMENTI (sposta, annulla, recensisci) ----------
function MieiAppuntamenti({ utente }) {
  const [appuntamenti, setAppuntamenti] = useState(null);
  const [errore, setErrore] = useState(null);
  const [inRecensione, setInRecensione] = useState(null);   // id dell'appuntamento di cui si scrive/modifica la recensione

  function carica() {
    api.get('/api/miei-appuntamenti').then(setAppuntamenti).catch((e) => setErrore(e.message));
  }

  useEffect(() => {
    if (utente && utente.authenticated) carica();
  }, [utente]);

  if (!utente) return <p>Caricamento...</p>;
  if (!utente.authenticated) {
    return <div className="errore">Accedi per vedere i tuoi appuntamenti.</div>;
  }

  function annulla(id) {
    if (!window.confirm('Annullare questo appuntamento?')) return;
    api.post('/api/appuntamenti/' + id + '/annulla')
      .then(() => { setErrore(null); carica(); })
      .catch((e) => setErrore(e.message));
  }

  function eliminaRecensione(idRecensione) {
    if (!window.confirm('Eliminare la tua recensione?')) return;
    api.del('/api/recensioni/' + idRecensione)
      .then(() => { setErrore(null); carica(); })
      .catch((e) => setErrore(e.message));
  }

  return (
    <div>
      <h1>I miei appuntamenti</h1>
      <Errore messaggio={errore} />
      {!appuntamenti && !errore && <p>Caricamento...</p>}
      {appuntamenti && appuntamenti.length === 0 && <p>Non hai ancora appuntamenti.</p>}
      {appuntamenti && appuntamenti.map((a) => (
        <div className={'card' + (a.stato === 'ANNULLATO' ? ' annullato' : '')} key={a.id}>
          <b>{a.trattamentoNome}</b> <span className="badge">{a.stato}</span>
          <div className="piccolo">
            {a.data} alle {a.ora} &middot; {a.operatriceNome} &middot; {a.eventoNome} ({a.tema}) &middot; {euro(a.prezzo)}
          </div>
          {a.stato === 'PRENOTATO' && (
            <div className="riga" style={{ marginTop: 8 }}>
              <button onClick={() => vaiA('/sposta/' + a.id)}>Sposta</button>
              <button className="pericolo" onClick={() => annulla(a.id)}>Annulla</button>
            </div>
          )}
          {a.stato === 'COMPLETATO' && a.recensione && inRecensione !== a.id && (
            <div>
              <p><span className="voto">&#9733; {a.recensione.voto}/5</span> {a.recensione.testo}</p>
              <div className="riga">
                <button onClick={() => setInRecensione(a.id)}>Modifica recensione</button>
                <button className="pericolo" onClick={() => eliminaRecensione(a.recensione.id)}>Elimina recensione</button>
              </div>
            </div>
          )}
          {a.stato === 'COMPLETATO' && !a.recensione && inRecensione !== a.id && (
            <div className="riga" style={{ marginTop: 8 }}>
              <button onClick={() => setInRecensione(a.id)}>Scrivi una recensione</button>
            </div>
          )}
          {inRecensione === a.id && (
            <FormRecensione
              appuntamentoId={a.id}
              esistente={a.recensione}
              onFatto={() => { setInRecensione(null); setErrore(null); carica(); }}
              onAnnulla={() => setInRecensione(null)}
            />
          )}
        </div>
      ))}
    </div>
  );
}

function FormRecensione({ appuntamentoId, esistente, onFatto, onAnnulla }) {
  const [voto, setVoto] = useState(esistente ? esistente.voto : 5);
  const [testo, setTesto] = useState(esistente ? esistente.testo : '');
  const [errore, setErrore] = useState(null);
  const [campi, setCampi] = useState({});

  function invia(e) {
    e.preventDefault();
    const corpo = { voto: Number(voto), testo: testo };
    const richiesta = esistente
      ? api.put('/api/recensioni/' + esistente.id, corpo)                          // modifica
      : api.post('/api/appuntamenti/' + appuntamentoId + '/recensione', corpo);    // nuova
    richiesta.then(() => onFatto()).catch((err) => { setErrore(err.message); setCampi(err.campi); });
  }

  return (
    <form onSubmit={invia} style={{ marginTop: 8 }}>
      <Errore messaggio={errore} />
      <label>Voto
        <select value={voto} onChange={(e) => setVoto(e.target.value)}>
          {[1, 2, 3, 4, 5].map((v) => <option key={v} value={v}>{v}</option>)}
        </select>
        <ErroreCampo campi={campi} nome="voto" />
      </label>
      <label>Testo
        <textarea value={testo} onChange={(e) => setTesto(e.target.value)} />
        <ErroreCampo campi={campi} nome="testo" />
      </label>
      <div className="riga">
        <button type="submit">Salva</button>
        <button type="button" className="secondario" onClick={onAnnulla}>Annulla</button>
      </div>
    </form>
  );
}
