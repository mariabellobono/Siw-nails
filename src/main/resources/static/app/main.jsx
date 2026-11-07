// PUNTO DI INGRESSO: sceglie la pagina in base all'indirizzo (#/...) e la disegna dentro <div id="root">.
// Gli indirizzi con # si chiamano "hash routing": il server non e' coinvolto, cambia solo la pagina React.

function usePercorso() {
  const [hash, setHash] = useState(window.location.hash || '#/');
  useEffect(() => {
    const aggiorna = () => setHash(window.location.hash || '#/');
    window.addEventListener('hashchange', aggiorna);
    return () => window.removeEventListener('hashchange', aggiorna);
  }, []);
  return hash.replace(/^#/, '');
}

function App() {
  const [utente, setUtente] = useState(null);   // null = non ancora caricato
  const percorso = usePercorso();

  // all'avvio si chiede al server chi e' collegato (e intanto arriva il cookie CSRF)
  useEffect(() => {
    api.get('/api/auth/me')
      .then(setUtente)
      .catch(() => setUtente({ authenticated: false, admin: false }));
  }, []);

  const p = percorso.split('/').filter(Boolean);   // '/eventi/3' -> ['eventi', '3']
  let pagina;
  if (p.length === 0) {
    pagina = <ListaEventi />;
  } else if (p[0] === 'eventi' && p[1]) {
    pagina = <DettaglioEvento key={p[1]} id={p[1]} />;
  } else if (p[0] === 'trattamenti' && p[1]) {
    pagina = <DettaglioTrattamento key={p[1]} id={p[1]} />;
  } else if (p[0] === 'trattamenti') {
    pagina = <ListaTrattamenti />;
  } else if (p[0] === 'operatrici') {
    pagina = <ListaOperatrici />;
  } else if (p[0] === 'agenda') {
    pagina = <Agenda />;
  } else if (p[0] === 'prenota') {
    pagina = <FormPrenotazione key={'n' + (p[1] || '')} trattamentoIniziale={p[1]} utente={utente} />;
  } else if (p[0] === 'sposta' && p[1]) {
    pagina = <FormPrenotazione key={'s' + p[1]} appuntamentoId={p[1]} utente={utente} />;
  } else if (p[0] === 'miei') {
    pagina = <MieiAppuntamenti utente={utente} />;
  } else {
    pagina = <p>Pagina non trovata.</p>;
  }

  return (
    <div>
      <header>
        <nav>
          <a href="#/">Eventi</a>
          <a href="#/trattamenti">Trattamenti</a>
          <a href="#/operatrici">Operatrici</a>
          <a href="#/agenda">Agenda</a>
          <a href="#/prenota">Prenota</a>
          {utente && utente.authenticated && <a href="#/miei">I miei appuntamenti</a>}
          <a href="/">Sito principale</a>
        </nav>
        <LoginBox utente={utente} setUtente={setUtente} />
      </header>
      <main>{pagina}</main>
    </div>
  );
}

ReactDOM.createRoot(document.getElementById('root')).render(<App />);
