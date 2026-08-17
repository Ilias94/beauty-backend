INSERT INTO beautypg.category (label) VALUES
  ('Barber'),
  ('Hairdresser'),
  ('Makeup Artist'),
  ('Nail Technician'),
  ('Esthetician'),
  ('Massage Therapist'),
  ('Skin Care Specialist'),
  ('Lash Technician'),
  ('Permanent Makeup Artist'),
  ('Hair Colorist'),
  ('Wax Specialist')
ON CONFLICT (label) DO NOTHING;