-- Database Schema Creation Script
-- Target: PostgreSQL

-- 1. Basics & Security
CREATE TABLE IF NOT EXISTS organizations (
    id UUID PRIMARY KEY,
    code VARCHAR(100) UNIQUE,
    short_name VARCHAR(100),
    long_name VARCHAR(255),
    description TEXT,
    logo VARCHAR(255),
    is_active BOOLEAN DEFAULT TRUE,
    is_individual BOOLEAN DEFAULT FALSE,
    is_public BOOLEAN DEFAULT FALSE,
    is_business BOOLEAN DEFAULT TRUE,
    country VARCHAR(100),
    city VARCHAR(100),
    address VARCHAR(255),
    localization VARCHAR(50),
    open_time TIME,
    close_time TIME,
    email VARCHAR(255),
    phone VARCHAR(50),
    whatsapp VARCHAR(50),
    social_networks TEXT,
    capital_share DOUBLE PRECISION,
    tax_number VARCHAR(100),
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    deleted_at TIMESTAMP
);

CREATE TABLE IF NOT EXISTS users (
    id UUID PRIMARY KEY,
    username VARCHAR(100) UNIQUE,
    email VARCHAR(255) UNIQUE,
    password VARCHAR(255),
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    deleted_at TIMESTAMP
);

CREATE TABLE IF NOT EXISTS user_organizations (
    id UUID PRIMARY KEY,
    user_id UUID,
    organization_id UUID,
    role VARCHAR(50),
    is_default BOOLEAN DEFAULT FALSE,
    is_active BOOLEAN DEFAULT TRUE,
    joined_at TIMESTAMP,
    left_at TIMESTAMP,
    permitted_sale_sizes VARCHAR(500),
    agency VARCHAR(100),
    sale_point VARCHAR(100)
);

CREATE TABLE IF NOT EXISTS user_organization_permissions (
    id UUID PRIMARY KEY,
    user_organization_id UUID,
    permission VARCHAR(50) NOT NULL,
    granted_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    granted_by UUID,
    expires_at TIMESTAMP,
    UNIQUE(user_organization_id, permission)
);

-- 2. Core Modules
CREATE TABLE IF NOT EXISTS agencies (
    id UUID PRIMARY KEY,
    organization_id UUID,
    code VARCHAR(100),
    name VARCHAR(255),
    location VARCHAR(255),
    description VARCHAR(500),
    timezone VARCHAR(100),
    address VARCHAR(255),
    owner_id UUID,
    manager_id UUID,
    transferable BOOLEAN DEFAULT FALSE,
    is_active BOOLEAN DEFAULT TRUE,
    logo_url VARCHAR(255),
    logo_id UUID,
    short_name VARCHAR(100),
    long_name VARCHAR(255),
    is_individual_business BOOLEAN DEFAULT FALSE,
    is_headquarter BOOLEAN DEFAULT FALSE,
    country VARCHAR(100),
    city VARCHAR(100),
    latitude DOUBLE PRECISION,
    longitude DOUBLE PRECISION,
    open_time VARCHAR(50),
    close_time VARCHAR(50),
    phone VARCHAR(50),
    email VARCHAR(255),
    whatsapp VARCHAR(50),
    greeting_message TEXT,
    average_revenue DOUBLE PRECISION,
    capital_share DOUBLE PRECISION,
    registration_number VARCHAR(100),
    social_network TEXT,
    tax_number VARCHAR(100),
    keywords VARCHAR(500),
    is_public BOOLEAN DEFAULT FALSE,
    is_business BOOLEAN DEFAULT TRUE,
    total_affiliated_customers INTEGER,
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);

CREATE TABLE IF NOT EXISTS pos_terminals (
    id UUID PRIMARY KEY,
    code VARCHAR(100),
    status VARCHAR(50),
    agency_id UUID,
    is_active BOOLEAN DEFAULT TRUE
);

-- 3. Tiers (Partners)
CREATE TABLE IF NOT EXISTS clients (
    id_client UUID PRIMARY KEY,
    organization_id UUID,
    username VARCHAR(100),
    categorie VARCHAR(100),
    site_web VARCHAR(255),
    n_tva BOOLEAN DEFAULT FALSE,
    adresse VARCHAR(500),
    telephone VARCHAR(100),
    email VARCHAR(255),
    type_client VARCHAR(50),
    raison_sociale VARCHAR(255),
    numero_tva VARCHAR(100),
    code_client VARCHAR(100),
    limite_credit DOUBLE PRECISION,
    solde_courant DOUBLE PRECISION DEFAULT 0.0,
    actif BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);

CREATE TABLE IF NOT EXISTS fournisseurs (
    id_fournisseur UUID PRIMARY KEY,
    organization_id UUID,
    username VARCHAR(100),
    categorie VARCHAR(100),
    site_web VARCHAR(255),
    n_tva BOOLEAN DEFAULT FALSE,
    adresse VARCHAR(500),
    telephone VARCHAR(100),
    email VARCHAR(255),
    type_fournisseur VARCHAR(50),
    raison_sociale VARCHAR(255),
    numero_tva VARCHAR(100),
    code_fournisseur VARCHAR(100),
    limite_credit DOUBLE PRECISION,
    solde_courant DOUBLE PRECISION DEFAULT 0.0,
    actif BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);

-- 4. Invoicing & Logistics
CREATE TABLE IF NOT EXISTS taxes (
    id_taxe UUID PRIMARY KEY,
    organization_id UUID,
    nom_taxe VARCHAR(100),
    calcul_taxe NUMERIC(15,2),
    actif BOOLEAN DEFAULT TRUE,
    type_taxe VARCHAR(50),
    porte_taxe VARCHAR(50),
    montant NUMERIC(15,2),
    position_fiscale VARCHAR(100),
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);

CREATE TABLE IF NOT EXISTS journals (
    id_journal UUID PRIMARY KEY,
    organization_id UUID,
    nom_journal VARCHAR(100),
    type VARCHAR(50),
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);

CREATE TABLE IF NOT EXISTS paiements (
    id_paiement UUID PRIMARY KEY,
    organization_id UUID,
    id_client UUID,
    montant NUMERIC(15,2),
    date DATE,
    journal VARCHAR(100),
    mode_paiement VARCHAR(50),
    compte_bancaire_f VARCHAR(100),
    memo TEXT,
    id_facture UUID,
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);

CREATE TABLE IF NOT EXISTS factures (
    id_facture UUID PRIMARY KEY,
    organization_id UUID ,
    numero_facture VARCHAR(100),
    date_facturation TIMESTAMP,
    date_echeance TIMESTAMP,
    date_systeme TIMESTAMP,
    type VARCHAR(50),
    etat VARCHAR(50),
    id_client UUID,
    nom_client VARCHAR(255),
    adresse_client VARCHAR(500),
    email_client VARCHAR(255),
    telephone_client VARCHAR(100),
    lignes_facture JSONB,
    montant_ht NUMERIC(15,2),
    montant_tva NUMERIC(15,2),
    montant_ttc NUMERIC(15,2),
    montant_total NUMERIC(15,2),
    montant_restant NUMERIC(15,2),
    final_amount NUMERIC(15,2),
    apply_vat BOOLEAN,
    devise VARCHAR(20),
    taux_change NUMERIC(15,6) DEFAULT 1.0,
    mode_reglement VARCHAR(50),
    conditions_paiement TEXT,
    nbre_echeance INTEGER,
    nos_ref VARCHAR(100),
    vos_ref VARCHAR(100),
    reference_commande VARCHAR(100),
    id_devis_origine VARCHAR(100),
    referal_client_id UUID,
    notes TEXT,
    pdf_path VARCHAR(255),
    envoye_par_email BOOLEAN DEFAULT FALSE,
    date_envoi_email TIMESTAMP,
    remise_globale_pourcentage NUMERIC(5,2) DEFAULT 0.0,
    remise_globale_montant NUMERIC(15,2) DEFAULT 0.0,
    created_by UUID,
    validated_by UUID,
    validated_at TIMESTAMP,
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    version BIGINT DEFAULT 0
);

CREATE TABLE IF NOT EXISTS devis (
    id_devis UUID PRIMARY KEY,
    organization_id UUID ,
    numero_devis VARCHAR(100),
    date_creation TIMESTAMP,
    date_validite TIMESTAMP,
    type VARCHAR(50),
    statut VARCHAR(50),
    lignes_devis JSONB,
    montant_total NUMERIC(15,2),
    id_client UUID,
    nom_client VARCHAR(255),
    adresse_client VARCHAR(500),
    email_client VARCHAR(255),
    telephone_client VARCHAR(100),
    montant_ht NUMERIC(15,2),
    montant_tva NUMERIC(15,2),
    montant_ttc NUMERIC(15,2),
    devise VARCHAR(20),
    taux_change NUMERIC(15,6) DEFAULT 1.0,
    conditions_paiement TEXT,
    notes TEXT,
    reference_externe VARCHAR(100),
    pdf_path VARCHAR(255),
    envoye_par_email BOOLEAN DEFAULT FALSE,
    date_envoi_email TIMESTAMP,
    date_acceptation TIMESTAMP,
    date_refus TIMESTAMP,
    motif_refus TEXT,
    id_facture_convertie UUID,
    remise_globale_pourcentage NUMERIC(5,2) DEFAULT 0.0,
    remise_globale_montant NUMERIC(15,2) DEFAULT 0.0,
    validite_offre_jours INTEGER DEFAULT 30,
    apply_vat BOOLEAN DEFAULT TRUE,
    date_systeme TIMESTAMP,
    mode_reglement VARCHAR(50),
    nos_ref VARCHAR(100),
    vos_ref VARCHAR(100),
    nbre_echeance INTEGER,
    referal_client_id UUID,
    final_amount NUMERIC(15,2),
    created_by UUID,
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    version BIGINT DEFAULT 0
);



CREATE TABLE IF NOT EXISTS bons_commande (
    id_bon_commande UUID PRIMARY KEY,
    organization_id UUID ,
    numero_commande VARCHAR(100),
    id_client UUID ,
    nom_client VARCHAR(255),
    adresse_client VARCHAR(500),
    email_client VARCHAR(255),
    telephone_client VARCHAR(100),
    recipient_name VARCHAR(255),
    recipient_phone VARCHAR(50),
    recipient_address VARCHAR(500),
    recipient_city VARCHAR(100),
    id_devis_origine UUID,
    numero_devis_origine VARCHAR(100),
    nos_ref VARCHAR(100),
    vos_ref VARCHAR(100),
    date_commande TIMESTAMP,
    date_systeme TIMESTAMP,
    date_livraison_prevue TIMESTAMP,
    lines JSONB,
    montant_ht NUMERIC(15,2),
    montant_tva NUMERIC(15,2),
    montant_ttc NUMERIC(15,2),
    devise VARCHAR(20),
    apply_vat BOOLEAN,
    transport_method VARCHAR(100),
    id_agency UUID,
    mode_reglement VARCHAR(50),
    statut VARCHAR(50),
    notes TEXT,
    created_by UUID,
    validated_by UUID,
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    validated_at TIMESTAMP
);

CREATE TABLE IF NOT EXISTS bons_achat (
    -- Primary and Foreign Keys
    id_bon_achat UUID PRIMARY KEY,
    organization_id UUID NOT NULL,
    
    -- Document Reference
    numero_bon_achat VARCHAR(100),
    
    -- Supplier Information (Fournisseur)
    id_fournisseur UUID,
    nom_fournisseur VARCHAR(255),
    supplier_code VARCHAR(100),
    supplier_email VARCHAR(255),
    supplier_contact VARCHAR(100),
    supplier_address TEXT,

    -- Delivery Information (Livraison)
    delivery_name VARCHAR(255),
    delivery_address TEXT,
    delivery_email VARCHAR(255),
    delivery_contact VARCHAR(100),

    -- Logistics and Dates
    date_achat TIMESTAMP,
    date_systeme TIMESTAMP,
    date_livraison_prevue TIMESTAMP,
    transport_method VARCHAR(100),
    instructions_livraison TEXT,

    -- Items and Financials
    lignes_bon_achat JSONB, -- Stores the List<LigneBonAchat>
    montant_ht NUMERIC(15, 2),
    montant_tva NUMERIC(15, 2),
    montant_ttc NUMERIC(15, 2),

    -- Workflow and Audit
    statut VARCHAR(50),
    notes TEXT, -- Maps to 'remarks' in DTO
    prepared_by UUID,
    approved_by UUID,
    created_by UUID,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Index for performance on organization lookups
CREATE INDEX IF NOT EXISTS idx_bons_achat_org ON bons_achat(organization_id);

CREATE TABLE IF NOT EXISTS bons_livraison (
    -- Primary Key
    id_bon_livraison UUID PRIMARY KEY ,
    
    -- Business Information
    numero_livraison VARCHAR(50) NOT NULL UNIQUE,
    date_livraison TIMESTAMP WITHOUT TIME ZONE,
    statut VARCHAR(30) NOT NULL, -- Corresponds to StatutBonLivraison enum
    
    -- Client Information
    id_client UUID NOT NULL,
    nom_client VARCHAR(255),
    adresse_client TEXT,
    email_client VARCHAR(255),
    telephone_client VARCHAR(50),
    
    -- Financials
    montant_ht DECIMAL(19, 4) DEFAULT 0.0000,
    montant_tva DECIMAL(19, 4) DEFAULT 0.0000,
    montant_ttc DECIMAL(19, 4) DEFAULT 0.0000,
    
    -- Complex Data (Stored as JSONB for the List<LigneBonLivraison>)
    lignes_bon_livraison JSONB,
    
    -- Metadata & Auditing
    notes TEXT,
    organization_id UUID NOT NULL,
    created_by UUID,
    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    date_systeme TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Indexes for performance
CREATE INDEX idx_bl_organization ON bons_livraison(organization_id);



CREATE TABLE IF NOT EXISTS bons_reception (
    id_grn UUID PRIMARY KEY,
    organization_id UUID,
    numero_reception VARCHAR(100),
    id_fournisseur UUID,
    nom_fournisseur VARCHAR(255),
    lines JSONB,
    montant_ht NUMERIC(15,2),
    montant_tva NUMERIC(15,2),
    montant_ttc NUMERIC(15,2),
    date_reception TIMESTAMP,
    statut VARCHAR(50),
    notes TEXT,
    created_by UUID,
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);

CREATE TABLE IF NOT EXISTS factures_fournisseur (
    id_facture_fournisseur UUID PRIMARY KEY,
    organization_id UUID,
    numero_facture VARCHAR(100),
    id_fournisseur UUID,
    nom_fournisseur VARCHAR(255),
    adresse_fournisseur VARCHAR(500),
    email_fournisseur VARCHAR(255),
    telephone_fournisseur VARCHAR(100),
    lines JSONB,
    montant_ht NUMERIC(15,2),
    montant_tva NUMERIC(15,2),
    montant_ttc NUMERIC(15,2),
    montant_total NUMERIC(15,2),
    montant_restant NUMERIC(15,2),
    date_facture TIMESTAMP,
    date_echeance TIMESTAMP,
    statut VARCHAR(50),
    devise VARCHAR(20),
    notes TEXT,
    pdf_path VARCHAR(255),
    created_by UUID,
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);

CREATE TABLE IF NOT EXISTS notes_credit (
    id_note_credit UUID PRIMARY KEY,
    organization_id UUID,
    id_client UUID,
    nom_client VARCHAR(255),
    adresse_client VARCHAR(500),
    email_client VARCHAR(255),
    telephone_client VARCHAR(100),
    id_facture_origine UUID,
    numero_facture_origine VARCHAR(100),
    lignes_note_credit JSONB,
    montant_ht NUMERIC(15,2),
    montant_tva NUMERIC(15,2),
    montant_ttc NUMERIC(15,2),
    montant_total NUMERIC(15,2),
    date_emission TIMESTAMP,
    statut VARCHAR(50),
    motif TEXT,
    notes TEXT,
    devise VARCHAR(20),
    pdf_path VARCHAR(255),
    created_by UUID,
    validated_by UUID,
    validated_at TIMESTAMP,
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);

CREATE TABLE IF NOT EXISTS factures_proforma (
    id_facture_proforma UUID PRIMARY KEY,
    organization_id UUID,
    numero_proforma_invoice VARCHAR(100),
    date_creation TIMESTAMP,
    type VARCHAR(50),
    statut VARCHAR(50),
    montant_total NUMERIC(15,2),
    id_client UUID,
    nom_client VARCHAR(255),
    adresse_client VARCHAR(500),
    email_client VARCHAR(255),
    telephone_client VARCHAR(100),
    lignes_facture_proforma JSONB,
    montant_ht NUMERIC(15,2),
    montant_tva NUMERIC(15,2),
    montant_ttc NUMERIC(15,2),
    devise VARCHAR(20),
    taux_change NUMERIC(15,6),
    conditions_paiement TEXT,
    notes TEXT,
    reference_externe VARCHAR(100),
    pdf_path VARCHAR(255),
    envoye_par_email BOOLEAN DEFAULT FALSE,
    date_envoi_email TIMESTAMP,
    date_acceptation TIMESTAMP,
    date_refus TIMESTAMP,
    motif_refus TEXT,
    id_facture_convertie UUID,
    remise_globale_pourcentage NUMERIC(5,2),
    remise_globale_montant NUMERIC(15,2),
    validite_offre_jours INTEGER,
    apply_vat BOOLEAN DEFAULT TRUE,
    date_systeme DATE,
    mode_reglement VARCHAR(50),
    nos_ref VARCHAR(100),
    vos_ref VARCHAR(100),
    nbre_echeance INTEGER,
    referal_client_id UUID,
    final_amount NUMERIC(15,2),
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);

-- Indexes for performance
CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);
CREATE INDEX IF NOT EXISTS idx_organizations_code ON organizations(code);
CREATE INDEX IF NOT EXISTS idx_clients_org ON clients(organization_id);
CREATE INDEX IF NOT EXISTS idx_fournisseurs_org ON fournisseurs(organization_id);
CREATE INDEX IF NOT EXISTS idx_factures_org ON factures(organization_id);
CREATE INDEX IF NOT EXISTS idx_devis_org ON devis(organization_id);
CREATE INDEX IF NOT EXISTS idx_paiements_org ON paiements(organization_id);
CREATE INDEX IF NOT EXISTS idx_factures_customer ON factures(id_client);
CREATE INDEX IF NOT EXISTS idx_paiements_customer ON paiements(id_client);
CREATE INDEX IF NOT EXISTS idx_user_org_perm_user_org ON user_organization_permissions(user_organization_id);
