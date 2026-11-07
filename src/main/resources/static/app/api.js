// CHIAMATE ALL'API REST (/api/...). Usa fetch, gia' presente nel browser.
// Il login e' una sessione (cookie JSESSIONID): il browser lo rimanda da solo.
// Per le richieste che MODIFICANO (POST, PUT, DELETE) Spring Security pretende il token CSRF:
// lo legge dal cookie XSRF-TOKEN e lo rimanda nell'header X-XSRF-TOKEN.

function leggiCookie(nome) {
  const trovato = document.cookie.match(new RegExp('(?:^|; )' + nome + '=([^;]*)'));
  return trovato ? decodeURIComponent(trovato[1]) : null;
}

async function chiama(metodo, url, corpo, giaRiprovato) {
  const opzioni = { method: metodo, headers: { Accept: 'application/json' }, credentials: 'same-origin' };
  if (corpo !== undefined) {
    opzioni.headers['Content-Type'] = 'application/json';
    opzioni.body = JSON.stringify(corpo);
  }
  if (metodo !== 'GET') {
    const token = leggiCookie('XSRF-TOKEN');
    if (token) {
      opzioni.headers['X-XSRF-TOKEN'] = token;
    }
  }
  const risposta = await fetch(url, opzioni);
  // 403 su una richiesta che MODIFICA: spesso e' un token CSRF vecchio o sbagliato (es. un altro programma su
  // localhost ha lasciato un cookie con lo stesso nome). Si chiede un token nuovo al server e si riprova UNA volta.
  // Se era un vero "non hai il permesso", il secondo tentativo da' di nuovo 403 e l'errore arriva all'utente.
  if (risposta.status === 403 && metodo !== 'GET' && !giaRiprovato) {
    await fetch('/api/auth/me', { credentials: 'same-origin' });
    return chiama(metodo, url, corpo, true);
  }
  if (risposta.status === 204) {
    return null; // nessun corpo (DELETE, logout)
  }
  let dati = null;
  try {
    dati = await risposta.json();
  } catch (e) {
    dati = null;
  }
  if (!risposta.ok) {
    // l'API risponde sempre {status, errore, messaggio, campi}: lo trasformiamo in un errore JavaScript
    const errore = new Error((dati && dati.messaggio) || 'Errore ' + risposta.status);
    errore.status = risposta.status;
    errore.campi = (dati && dati.campi) || {};
    throw errore;
  }
  return dati;
}

const api = {
  get: (url) => chiama('GET', url),
  post: (url, corpo) => chiama('POST', url, corpo === undefined ? {} : corpo),
  put: (url, corpo) => chiama('PUT', url, corpo),
  del: (url) => chiama('DELETE', url),
};
