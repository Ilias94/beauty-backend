-- ================================================================
-- SEED DATA: clear all courses and fill with 15 sample courses
-- Run against your PostgreSQL database (schema: beautypg)
-- ================================================================

-- 1. Delete dependent tables first (respecting FK order)
DELETE FROM beautypg.exam;
DELETE FROM beautypg.rating;
DELETE FROM beautypg.comment;
DELETE FROM beautypg.course_participants;

DO $$
BEGIN
    DELETE FROM beautypg.payment;
EXCEPTION WHEN undefined_table THEN
    BEGIN DELETE FROM payment; EXCEPTION WHEN OTHERS THEN NULL; END;
END $$;

DELETE FROM beautypg.course;
DELETE FROM beautypg.address;

-- 2. Insert addresses for IN_PERSON / HYBRID courses
INSERT INTO beautypg.address (city, district, street, street_number, postal_code) VALUES
  ('Warszawa',  'Śródmieście',  'Marszałkowska',          '10',  '00-001'),
  ('Kraków',    'Stare Miasto', 'Floriańska',              '5',   '31-019'),
  ('Wrocław',   'Stare Miasto', 'Świdnicka',               '3',   '50-067'),
  ('Gdańsk',    'Śródmieście',  'Długa',                   '2',   '80-827'),
  ('Poznań',    'Centrum',      'Półwiejska',              '15',  '61-888'),
  ('Łódź',      'Centrum',      'Piotrkowska',             '200', '90-369'),
  ('Katowice',  'Centrum',      'Stawowa',                 '7',   '40-095'),
  ('Lublin',    'Centrum',      'Krakowskie Przedmieście', '3',   '20-002'),
  ('Wrocław',   'Krzyki',       'Borowska',                '11',  '50-556'),
  ('Warszawa',  'Mokotów',      'Puławska',                '43',  '02-508'),
  ('Gdańsk',    'Wrzeszcz',     'Grunwaldzka',             '82',  '80-244'),
  ('Katowice',  'Ligota',       'Kościuszki',              '6',   '40-048'),
  ('Warszawa',  'Wola',         'Jana Pawła II',           '22',  '00-133');

-- 3. Insert 15 sample courses
DO $$
DECLARE
  v_uid BIGINT;
BEGIN
  SELECT id INTO v_uid FROM beautypg.users WHERE email = 'ilbelg3@gmail.com';
  IF v_uid IS NULL THEN
    RAISE EXCEPTION 'User ilbelg3@gmail.com not found – update the email in this script';
  END IF;

  INSERT INTO beautypg.course
    (title, description, start_date, end_date, max_participants, price, rating,
     course_type, created_by_user_id, address_id, category_id)
  VALUES

  -- ── IN_PERSON ───────────────────────────────────────────────────────
  ('Kurs Wizażu Artystycznego',
   'Profesjonalne techniki makijażu dziennego, wieczorowego i artystycznego. Praca z różnymi typami urody.',
   '2026-06-10 09:00', '2026-06-12 17:00', 12, 1800.00, 4.8, 'IN_PERSON', v_uid,
   (SELECT id FROM beautypg.address WHERE city='Warszawa' AND street='Marszałkowska'),
   (SELECT id FROM beautypg.category WHERE label='Makeup Artist')),

  ('Profesjonalne Strzyżenie i Stylizacja',
   'Techniki cięcia damskiego i męskiego, stylizacja na co dzień i na specjalne okazje.',
   '2026-06-15 09:00', '2026-06-17 17:00', 10, 1500.00, 4.6, 'IN_PERSON', v_uid,
   (SELECT id FROM beautypg.address WHERE city='Kraków' AND street='Floriańska'),
   (SELECT id FROM beautypg.category WHERE label='Hairdresser')),

  ('Stylizacja Paznokci – Żel i Hybryda',
   'Od podstaw po zaawansowane techniki przedłużania i zdobienia paznokci metodą żelową i hybrydową.',
   '2026-06-20 09:00', '2026-06-22 17:00', 8, 1200.00, 4.7, 'IN_PERSON', v_uid,
   (SELECT id FROM beautypg.address WHERE city='Wrocław' AND street='Świdnicka'),
   (SELECT id FROM beautypg.category WHERE label='Nail Technician')),

  ('Zaawansowana Stylizacja Rzęs',
   'Metody 1:1, 2D i objętościowe. Dobór rzęs do kształtu oka, pielęgnacja i retusz.',
   '2026-07-01 09:00', '2026-07-03 17:00', 8, 1400.00, 4.9, 'IN_PERSON', v_uid,
   (SELECT id FROM beautypg.address WHERE city='Gdańsk' AND street='Długa'),
   (SELECT id FROM beautypg.category WHERE label='Lash Technician')),

  ('Barber – Techniki Klasyczne i Nowoczesne',
   'Strzyżenie i golenie brzytwą, zarost, linia, fade. Kurs dla przyszłych profesjonalnych barberów.',
   '2026-07-08 09:00', '2026-07-10 17:00', 10, 1600.00, 4.7, 'IN_PERSON', v_uid,
   (SELECT id FROM beautypg.address WHERE city='Poznań' AND street='Półwiejska'),
   (SELECT id FROM beautypg.category WHERE label='Barber')),

  ('Masaż Relaksacyjny i Terapeutyczny',
   'Techniki masażu klasycznego i terapeutycznego. Anatomia, przeciwwskazania i praca z klientem.',
   '2026-07-15 09:00', '2026-07-17 17:00', 6, 2000.00, 4.8, 'IN_PERSON', v_uid,
   (SELECT id FROM beautypg.address WHERE city='Łódź' AND street='Piotrkowska'),
   (SELECT id FROM beautypg.category WHERE label='Massage Therapist')),

  ('Zaawansowana Pielęgnacja Skóry',
   'Analiza skóry, zabiegi oczyszczające, peelingi chemiczne i mezoterapia. Kurs dla kosmetologów.',
   '2026-07-22 09:00', '2026-07-24 17:00', 8, 2200.00, 4.5, 'IN_PERSON', v_uid,
   (SELECT id FROM beautypg.address WHERE city='Katowice' AND street='Stawowa'),
   (SELECT id FROM beautypg.category WHERE label='Skin Care Specialist')),

  ('Makijaż Permanentny – Brwi i Usta',
   'Microblading, ombre brwi i linia ust. Dobór pigmentów, sterylizacja i bezpieczeństwo zabiegu.',
   '2026-08-01 09:00', '2026-08-03 17:00', 6, 2800.00, 4.9, 'IN_PERSON', v_uid,
   (SELECT id FROM beautypg.address WHERE city='Lublin' AND street='Krakowskie Przedmieście'),
   (SELECT id FROM beautypg.category WHERE label='Permanent Makeup Artist')),

  ('Estetyka Skóry – Zabiegi Twarzy',
   'Oczyszczanie manualne i ultradźwiękowe, kwasy, maski. Budowanie profesjonalnej oferty gabinetu.',
   '2026-08-10 09:00', '2026-08-12 17:00', 10, 1900.00, 4.7, 'IN_PERSON', v_uid,
   (SELECT id FROM beautypg.address WHERE city='Warszawa' AND street='Puławska'),
   (SELECT id FROM beautypg.category WHERE label='Esthetician')),

  ('Makijaż Ślubny i Okolicznościowy',
   'Makijaż weselny i eventowy – trwałość, dobór produktów, praca z klientem w dniu ślubu.',
   '2026-07-05 09:00', '2026-07-07 17:00', 8, 1600.00, 4.8, 'IN_PERSON', v_uid,
   (SELECT id FROM beautypg.address WHERE city='Gdańsk' AND street='Grunwaldzka'),
   (SELECT id FROM beautypg.category WHERE label='Makeup Artist')),

  ('Stylizacja i Koloryzacja Włosów',
   'Strzyżenie i koloryzacja w jednym kursie – balayage, glossing, dobór produktów pielęgnacyjnych.',
   '2026-08-15 09:00', '2026-08-17 17:00', 10, 1700.00, NULL, 'IN_PERSON', v_uid,
   (SELECT id FROM beautypg.address WHERE city='Katowice' AND street='Kościuszki'),
   (SELECT id FROM beautypg.category WHERE label='Hairdresser')),

  ('Nail Art – Techniki Zdobień',
   'Wzory geometryczne, marmur, kwiaty, glitter. Zdobienia ręczne, stemple i folie transferowe.',
   '2026-09-01 09:00', '2026-09-03 17:00', 8, 1100.00, NULL, 'IN_PERSON', v_uid,
   (SELECT id FROM beautypg.address WHERE city='Warszawa' AND street='Jana Pawła II'),
   (SELECT id FROM beautypg.category WHERE label='Nail Technician')),

  -- ── HYBRID ──────────────────────────────────────────────────────────
  ('Depilacja Woskiem – Kurs Profesjonalny',
   'Techniki depilacji gorącym i chłodnym woskiem, pielęgnacja po zabiegu. Moduł online + praktyki.',
   '2026-06-18 09:00', '2026-06-20 17:00', 10, 900.00, 4.4, 'HYBRID', v_uid,
   (SELECT id FROM beautypg.address WHERE city='Wrocław' AND street='Borowska'),
   (SELECT id FROM beautypg.category WHERE label='Wax Specialist')),

  -- ── ONLINE ──────────────────────────────────────────────────────────
  ('Koloryzacja Włosów – Balejaż i Ombre',
   'Teoria koloru, techniki rozjaśniania, balejaż, ombre i kolory fantasy. Materiały wideo + PDF.',
   '2026-06-05 10:00', '2026-07-05 20:00', 30, 499.00, 4.6, 'ONLINE', v_uid,
   NULL,
   (SELECT id FROM beautypg.category WHERE label='Hair Colorist')),

  ('Barber Skills Online – Teoria i Techniki',
   'Teoria strzyżenia, historia barbershopów, dobór narzędzi i pielęgnacja. Idealne uzupełnienie kursu.',
   '2026-06-01 10:00', '2026-07-01 20:00', 50, 299.00, 4.3, 'ONLINE', v_uid,
   NULL,
   (SELECT id FROM beautypg.category WHERE label='Barber'));

  RAISE NOTICE 'Done – 15 courses inserted for user ID %', v_uid;
END $$;
