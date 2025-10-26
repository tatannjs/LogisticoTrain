-- Script d'initialisation de la base de données de production
CREATE DATABASE IF NOT EXISTS logistico_production;
USE logistico_production;

-- Table des voies
CREATE TABLE IF NOT EXISTS voies (
    id INT AUTO_INCREMENT PRIMARY KEY,
    numero INT NOT NULL UNIQUE,
    status VARCHAR(50) DEFAULT 'libre',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Table des rames
CREATE TABLE IF NOT EXISTS rames (
    id INT AUTO_INCREMENT PRIMARY KEY,
    numero VARCHAR(50) NOT NULL UNIQUE,
    type VARCHAR(100),
    status VARCHAR(50) DEFAULT 'en_circulation',
    voie_id INT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (voie_id) REFERENCES voies(id)
);

-- Table des tâches
CREATE TABLE IF NOT EXISTS taches (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nom VARCHAR(200) NOT NULL,
    description TEXT,
    status VARCHAR(50) DEFAULT 'en_attente',
    rame_id INT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (rame_id) REFERENCES rames(id)
);

-- Données de test
INSERT IGNORE INTO voies (numero, status) VALUES 
(1, 'libre'),
(2, 'occupee'),
(3, 'libre'),
(4, 'maintenance');

INSERT IGNORE INTO rames (numero, type, status) VALUES 
('TGV001', 'TGV', 'en_circulation'),
('TER002', 'TER', 'maintenance'),
('ICE003', 'ICE', 'en_circulation');