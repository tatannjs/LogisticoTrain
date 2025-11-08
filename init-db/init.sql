-- Script d'initialisation de la base de données de production
CREATE DATABASE IF NOT EXISTS logistico_production;
USE logistico_production;

-- Table des voies (correspondant à l'entité Voie)
CREATE TABLE IF NOT EXISTS voies (
    num_voie INT(11) NOT NULL PRIMARY KEY,
    interdite TINYINT(1) NOT NULL
);

-- Table des rames (correspondant à l'entité Rame)
CREATE TABLE IF NOT EXISTS rames (
    num_serie VARCHAR(12) NOT NULL PRIMARY KEY,
    type_rame VARCHAR(50) NOT NULL,
    voie INT(11) UNIQUE,
    conducteur_entrant VARCHAR(50) NOT NULL,
    FOREIGN KEY (voie) REFERENCES voies(num_voie)
);

-- Table des tâches (correspondant à l'entité Tache avec clé primaire composite)
CREATE TABLE IF NOT EXISTS taches (
    num_serie VARCHAR(12) NOT NULL,
    num_tache INT(11) NOT NULL,
    tache TEXT NOT NULL,
    PRIMARY KEY (num_serie, num_tache),
    FOREIGN KEY (num_serie) REFERENCES rames(num_serie)
);

-- Données de test
INSERT IGNORE INTO voies (num_voie, interdite) VALUES 
(1, 0),
(2, 0),
(3, 0),
(4, 1);

INSERT IGNORE INTO rames (num_serie, type_rame, conducteur_entrant) VALUES 
('TGV001', 'TGV', 'Jean Dupont'),
('TER002', 'TER', 'Marie Martin'),
('ICE003', 'ICE', 'Pierre Durand');

INSERT IGNORE INTO taches (num_serie, num_tache, tache) VALUES
('TGV001', 1, 'Vérification des freins'),
('TGV001', 2, 'Contrôle de sécurité'),
('TER002', 1, 'Maintenance moteur'),
('ICE003', 1, 'Nettoyage intérieur');