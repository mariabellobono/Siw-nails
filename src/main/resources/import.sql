-- ==========================================================================
-- DATI DI PROVA - Siw-nails (PostgreSQL)
-- Eseguito da Hibernate all'avvio perche' spring.jpa.hibernate.ddl-auto=create.
-- REGOLE: una istruzione per riga; commenti con '--'. SQL diretto: le validazioni Java non si applicano.
-- Dati INVENTATI, solo per test. Riferimento temporale: oggi = 2026-11-07.
-- Ordine: 1 eventi, 2 trattamenti, 3 offerte, 4 operatrici, 5 utenti/credenziali, 6 appuntamenti, 7 recensioni.
-- ==========================================================================

-- 1. EVENTI (1 passato, 2 in corso, 1 futuro)
insert into evento (id, nome, anno, citta, data_inizio, data_fine, descrizione, immagine) values (nextval('evento_seq'), 'Nail Week Roma', 2026, 'Roma', '2026-09-14', '2026-09-20', 'Una settimana di nail art tra i toni neutri e il bianco', 'roma.svg');
insert into evento (id, nome, anno, citta, data_inizio, data_fine, descrizione, immagine) values (nextval('evento_seq'), 'Natale in Rosso', 2026, 'Milano', '2026-11-02', '2026-12-24', 'Colori, glitter e decorazioni natalizie', 'natale.svg');
insert into evento (id, nome, anno, citta, data_inizio, data_fine, descrizione, immagine) values (nextval('evento_seq'), 'Autunno in Gel', 2026, 'Torino', '2026-10-26', '2026-11-30', 'Le tinte calde della stagione in versione gel', 'autunno.svg');
insert into evento (id, nome, anno, citta, data_inizio, data_fine, descrizione, immagine) values (nextval('evento_seq'), 'Primavera in Fiore', 2027, 'Firenze', '2027-03-15', '2027-03-21', 'Fiori, pastelli e french colorate', 'primavera.svg');

-- 2. TRATTAMENTI (nome, categoria, durata in minuti)
insert into trattamento (id, nome, categoria, durata, descrizione) values (nextval('trattamento_seq'), 'Manicure classica', 'Manicure', 45, 'Cura delle mani e smalto tradizionale');
insert into trattamento (id, nome, categoria, durata, descrizione) values (nextval('trattamento_seq'), 'Semipermanente', 'Manicure', 60, 'Smalto semipermanente con asciugatura in lampada');
insert into trattamento (id, nome, categoria, durata, descrizione) values (nextval('trattamento_seq'), 'Ricostruzione gel', 'Ricostruzione', 120, 'Ricostruzione unghie con gel');
insert into trattamento (id, nome, categoria, durata, descrizione) values (nextval('trattamento_seq'), 'Nail art', 'Decorazione', 30, 'Decorazioni a mano libera');
insert into trattamento (id, nome, categoria, durata, descrizione) values (nextval('trattamento_seq'), 'Pedicure curativo', 'Pedicure', 60, 'Cura e trattamento dei piedi');
insert into trattamento (id, nome, categoria, durata, descrizione) values (nextval('trattamento_seq'), 'French', 'Manicure', 50, 'Manicure french bianca o colorata');

-- 3. OFFERTE: lo stesso trattamento ha prezzo e tema diversi in eventi diversi (evento, trattamento, prezzo, tema)
insert into offerta (id, prezzo, tema, evento_id, trattamento_id) select nextval('offerta_seq'), 35.00, 'Bianco', e.id, t.id from evento e, trattamento t where e.nome = 'Nail Week Roma' and t.nome = 'Semipermanente';
insert into offerta (id, prezzo, tema, evento_id, trattamento_id) select nextval('offerta_seq'), 25.00, 'Nude', e.id, t.id from evento e, trattamento t where e.nome = 'Nail Week Roma' and t.nome = 'Manicure classica';
insert into offerta (id, prezzo, tema, evento_id, trattamento_id) select nextval('offerta_seq'), 32.00, 'Bianco', e.id, t.id from evento e, trattamento t where e.nome = 'Nail Week Roma' and t.nome = 'French';
insert into offerta (id, prezzo, tema, evento_id, trattamento_id) select nextval('offerta_seq'), 45.00, 'Natale', e.id, t.id from evento e, trattamento t where e.nome = 'Natale in Rosso' and t.nome = 'Semipermanente';
insert into offerta (id, prezzo, tema, evento_id, trattamento_id) select nextval('offerta_seq'), 20.00, 'Glitter', e.id, t.id from evento e, trattamento t where e.nome = 'Natale in Rosso' and t.nome = 'Nail art';
insert into offerta (id, prezzo, tema, evento_id, trattamento_id) select nextval('offerta_seq'), 85.00, 'Rosso', e.id, t.id from evento e, trattamento t where e.nome = 'Natale in Rosso' and t.nome = 'Ricostruzione gel';
insert into offerta (id, prezzo, tema, evento_id, trattamento_id) select nextval('offerta_seq'), 40.00, 'Terra', e.id, t.id from evento e, trattamento t where e.nome = 'Autunno in Gel' and t.nome = 'Semipermanente';
insert into offerta (id, prezzo, tema, evento_id, trattamento_id) select nextval('offerta_seq'), 80.00, 'Bordeaux', e.id, t.id from evento e, trattamento t where e.nome = 'Autunno in Gel' and t.nome = 'Ricostruzione gel';
insert into offerta (id, prezzo, tema, evento_id, trattamento_id) select nextval('offerta_seq'), 38.00, 'Spezie', e.id, t.id from evento e, trattamento t where e.nome = 'Autunno in Gel' and t.nome = 'Pedicure curativo';
insert into offerta (id, prezzo, tema, evento_id, trattamento_id) select nextval('offerta_seq'), 28.00, 'Cannella', e.id, t.id from evento e, trattamento t where e.nome = 'Autunno in Gel' and t.nome = 'Manicure classica';
insert into offerta (id, prezzo, tema, evento_id, trattamento_id) select nextval('offerta_seq'), 36.00, 'Pastello', e.id, t.id from evento e, trattamento t where e.nome = 'Primavera in Fiore' and t.nome = 'French';
insert into offerta (id, prezzo, tema, evento_id, trattamento_id) select nextval('offerta_seq'), 22.00, 'Fiori', e.id, t.id from evento e, trattamento t where e.nome = 'Primavera in Fiore' and t.nome = 'Nail art';

-- 4. OPERATRICI (con orario di lavoro giornaliero)
insert into operatrice (id, nome, specializzazione, ora_inizio, ora_fine) values (nextval('operatrice_seq'), 'Giulia Neri', 'Ricostruzione e gel', '09:00', '17:00');
insert into operatrice (id, nome, specializzazione, ora_inizio, ora_fine) values (nextval('operatrice_seq'), 'Sara Bianchi', 'Nail art', '10:00', '18:00');
insert into operatrice (id, nome, specializzazione, ora_inizio, ora_fine) values (nextval('operatrice_seq'), 'Elena Conti', 'Manicure e pedicure', '09:00', '13:00');

-- 5. UTENTI E CREDENZIALI (password: admin / utente1 / utente2, cifrate con BCrypt)
insert into utente (id, nome, cognome, email) values (nextval('utente_seq'), 'Admin', 'Sistema', 'admin@nails.it');
insert into utente (id, nome, cognome, email) values (nextval('utente_seq'), 'Mario', 'Rossi', 'mario.rossi@nails.it');
insert into utente (id, nome, cognome, email) values (nextval('utente_seq'), 'Anna', 'Verdi', 'anna.verdi@nails.it');
insert into credenziali (id, username, password, ruolo, utente_id) select nextval('credenziali_seq'), 'admin', '$2b$10$BT78ZlJ2AQkzxOcn1TLNUeaMzM4IjDmo8I/OzcYZZn0pHhhMRjp6W', 'ADMIN', u.id from utente u where u.email = 'admin@nails.it';
insert into credenziali (id, username, password, ruolo, utente_id) select nextval('credenziali_seq'), 'utente1', '$2b$10$/.knpi7r/Ox4zdNfX20dDej5upc9YkPUPKB.gVnjQvgRYtTlq/TYK', 'USER', u.id from utente u where u.email = 'mario.rossi@nails.it';
-- utente2 usa lo stesso hash di utente1, quindi la sua password e' 'utente1'
insert into credenziali (id, username, password, ruolo, utente_id) select nextval('credenziali_seq'), 'utente2', '$2b$10$/.knpi7r/Ox4zdNfX20dDej5upc9YkPUPKB.gVnjQvgRYtTlq/TYK', 'USER', u.id from utente u where u.email = 'anna.verdi@nails.it';

-- 6. APPUNTAMENTI (data, ora, stato, evento, trattamento, operatrice, cliente): tutti dentro il periodo dell'evento e l'orario dell'operatrice
insert into appuntamento (id, data, ora, stato, offerta_id, operatrice_id, utente_id) select nextval('appuntamento_seq'), '2026-09-15', '10:00', 'COMPLETATO', f.id, p.id, u.id from offerta f, evento e, trattamento t, operatrice p, utente u where f.evento_id = e.id and f.trattamento_id = t.id and e.nome = 'Nail Week Roma' and t.nome = 'Semipermanente' and p.nome = 'Giulia Neri' and u.email = 'mario.rossi@nails.it';
insert into appuntamento (id, data, ora, stato, offerta_id, operatrice_id, utente_id) select nextval('appuntamento_seq'), '2026-09-16', '11:00', 'COMPLETATO', f.id, p.id, u.id from offerta f, evento e, trattamento t, operatrice p, utente u where f.evento_id = e.id and f.trattamento_id = t.id and e.nome = 'Nail Week Roma' and t.nome = 'French' and p.nome = 'Sara Bianchi' and u.email = 'anna.verdi@nails.it';
insert into appuntamento (id, data, ora, stato, offerta_id, operatrice_id, utente_id) select nextval('appuntamento_seq'), '2026-11-10', '10:00', 'PRENOTATO', f.id, p.id, u.id from offerta f, evento e, trattamento t, operatrice p, utente u where f.evento_id = e.id and f.trattamento_id = t.id and e.nome = 'Natale in Rosso' and t.nome = 'Semipermanente' and p.nome = 'Giulia Neri' and u.email = 'mario.rossi@nails.it';
insert into appuntamento (id, data, ora, stato, offerta_id, operatrice_id, utente_id) select nextval('appuntamento_seq'), '2026-11-10', '14:00', 'PRENOTATO', f.id, p.id, u.id from offerta f, evento e, trattamento t, operatrice p, utente u where f.evento_id = e.id and f.trattamento_id = t.id and e.nome = 'Natale in Rosso' and t.nome = 'Nail art' and p.nome = 'Sara Bianchi' and u.email = 'anna.verdi@nails.it';
insert into appuntamento (id, data, ora, stato, offerta_id, operatrice_id, utente_id) select nextval('appuntamento_seq'), '2026-11-12', '09:00', 'PRENOTATO', f.id, p.id, u.id from offerta f, evento e, trattamento t, operatrice p, utente u where f.evento_id = e.id and f.trattamento_id = t.id and e.nome = 'Natale in Rosso' and t.nome = 'Ricostruzione gel' and p.nome = 'Giulia Neri' and u.email = 'anna.verdi@nails.it';
insert into appuntamento (id, data, ora, stato, offerta_id, operatrice_id, utente_id) select nextval('appuntamento_seq'), '2026-11-11', '10:30', 'PRENOTATO', f.id, p.id, u.id from offerta f, evento e, trattamento t, operatrice p, utente u where f.evento_id = e.id and f.trattamento_id = t.id and e.nome = 'Autunno in Gel' and t.nome = 'Pedicure curativo' and p.nome = 'Elena Conti' and u.email = 'mario.rossi@nails.it';
insert into appuntamento (id, data, ora, stato, offerta_id, operatrice_id, utente_id) select nextval('appuntamento_seq'), '2026-11-13', '11:00', 'ANNULLATO', f.id, p.id, u.id from offerta f, evento e, trattamento t, operatrice p, utente u where f.evento_id = e.id and f.trattamento_id = t.id and e.nome = 'Autunno in Gel' and t.nome = 'Manicure classica' and p.nome = 'Elena Conti' and u.email = 'anna.verdi@nails.it';
insert into appuntamento (id, data, ora, stato, offerta_id, operatrice_id, utente_id) select nextval('appuntamento_seq'), '2026-11-14', '15:00', 'PRENOTATO', f.id, p.id, u.id from offerta f, evento e, trattamento t, operatrice p, utente u where f.evento_id = e.id and f.trattamento_id = t.id and e.nome = 'Autunno in Gel' and t.nome = 'Semipermanente' and p.nome = 'Sara Bianchi' and u.email = 'mario.rossi@nails.it';

-- Appuntamento in piu' completato (Anna, Semipermanente) per avere tre appuntamenti recensibili
insert into appuntamento (id, data, ora, stato, offerta_id, operatrice_id, utente_id) select nextval('appuntamento_seq'), '2026-09-17', '10:00', 'COMPLETATO', f.id, p.id, u.id from offerta f, evento e, trattamento t, operatrice p, utente u where f.evento_id = e.id and f.trattamento_id = t.id and e.nome = 'Nail Week Roma' and t.nome = 'Semipermanente' and p.nome = 'Giulia Neri' and u.email = 'anna.verdi@nails.it';

-- 7. RECENSIONI: una per appuntamento COMPLETATO (cliente, trattamento, data, voto, testo)
insert into recensione (id, testo, voto, data, appuntamento_id) select nextval('recensione_seq'), 'Tenuta perfetta, molto soddisfatto', 5, '2026-09-15', a.id from appuntamento a, offerta f, trattamento t, utente u where a.offerta_id = f.id and f.trattamento_id = t.id and a.utente_id = u.id and t.nome = 'Semipermanente' and u.email = 'mario.rossi@nails.it' and a.data = '2026-09-15';
insert into recensione (id, testo, voto, data, appuntamento_id) select nextval('recensione_seq'), 'Bel risultato, un po'' di attesa', 4, '2026-09-16', a.id from appuntamento a, offerta f, trattamento t, utente u where a.offerta_id = f.id and f.trattamento_id = t.id and a.utente_id = u.id and t.nome = 'French' and u.email = 'anna.verdi@nails.it' and a.data = '2026-09-16';
insert into recensione (id, testo, voto, data, appuntamento_id) select nextval('recensione_seq'), 'Colore bellissimo', 5, '2026-09-17', a.id from appuntamento a, offerta f, trattamento t, utente u where a.offerta_id = f.id and f.trattamento_id = t.id and a.utente_id = u.id and t.nome = 'Semipermanente' and u.email = 'anna.verdi@nails.it' and a.data = '2026-09-17';
