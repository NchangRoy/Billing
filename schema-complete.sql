-- ============================================================================
-- BILLING SYSTEM - COMPLETE DATABASE SCHEMA
-- Target: PostgreSQL
-- ============================================================================
-- This schema includes all tables for the billing module, including:
-- - Sales Documents (Devis, Facture, NoteCredit, FactureProforma)
-- - Purchase Documents (BonAchat, BonCommande, FactureFournisseur)
-- - Logistics (BonLivraison, BonReception)
-- - Supporting tables (Taxes, Paiements, Journals, etc.)
-- ============================================================================



--customer platform access table 
CREATE TABLE portal_access_tokens (
    token_id BIGSERIAL PRIMARY KEY,

    token TEXT NOT NULL UNIQUE,

    resource_type TEXT NOT NULL, 
    -- e.g. 'quotation', 'proforma', 'invoice'

    resource_id BIGINT NOT NULL,

    client_email TEXT NOT NULL,

    permissions JSONB NOT NULL DEFAULT '{}'::jsonb,
    -- Example: {"accept": true, "reject": true, "modify": true}

    expires_at TIMESTAMP WITH TIME ZONE,

    used BOOLEAN NOT NULL DEFAULT FALSE,

    used_at TIMESTAMP WITH TIME ZONE,

    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    canAccept BOOLEAN NOT NULL DEFAULT FALSE,
    canReject BOOLEAN NOT NULL DEFAULT FALSE,
    canModify BOOLEAN NOT NULL DEFAULT FALSE,
    canView BOOLEAN NOT NULL DEFAULT FALSE
);

-- ============================================================================
-- 1. TAX CONFIGURATION
-- ============================================================================

CREATE TABLE IF NOT EXISTS taxes (
    id_taxe UUID PRIMARY KEY,
    organization_id UUID NOT NULL,
    nom_taxe VARCHAR(100) NOT NULL,
    calcul_taxe NUMERIC(5,2) NOT NULL,
    type_taxe VARCHAR(50),
    porte_taxe VARCHAR(50),
    montant NUMERIC(15,2),
    position_fiscale VARCHAR(100),
    actif BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_taxes_organization FOREIGN KEY (organization_id) REFERENCES organizations(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_taxes_org ON taxes(organization_id);
CREATE INDEX IF NOT EXISTS idx_taxes_active ON taxes(actif, organization_id);

-- ============================================================================
-- 2. JOURNALS (ACCOUNTING RECORDS)
-- ============================================================================

CREATE TABLE IF NOT EXISTS journals (
    id_journal UUID PRIMARY KEY,
    organization_id UUID NOT NULL,
    nom_journal VARCHAR(100) NOT NULL,
    type VARCHAR(50) NOT NULL,
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_journals_organization FOREIGN KEY (organization_id) REFERENCES organizations(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_journals_org ON journals(organization_id);
CREATE INDEX IF NOT EXISTS idx_journals_type ON journals(type, organization_id);

-- ============================================================================
-- 3. PAYMENT RECORDS
-- ============================================================================

CREATE TABLE IF NOT EXISTS paiements (
    id_paiement UUID PRIMARY KEY,
    organization_id UUID NOT NULL,
    id_client UUID,
    montant NUMERIC(15,2) NOT NULL,
    date DATE NOT NULL,
    journal VARCHAR(100),
    mode_paiement VARCHAR(50),
    compte_bancaire_f VARCHAR(100),
    memo TEXT,
    id_facture UUID,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_paiements_organization FOREIGN KEY (organization_id) REFERENCES organizations(id) ON DELETE CASCADE,
    CONSTRAINT fk_paiements_client FOREIGN KEY (id_client) REFERENCES clients(id_client) ON DELETE SET NULL,
    CONSTRAINT fk_paiements_facture FOREIGN KEY (id_facture) REFERENCES factures(id_facture) ON DELETE SET NULL
);

CREATE INDEX IF NOT EXISTS idx_paiements_org ON paiements(organization_id);
CREATE INDEX IF NOT EXISTS idx_paiements_client ON paiements(id_client);
CREATE INDEX IF NOT EXISTS idx_paiements_facture ON paiements(id_facture);
CREATE INDEX IF NOT EXISTS idx_paiements_date ON paiements(date);

-- ============================================================================
-- 4. SALES DOCUMENTS
-- ============================================================================

-- 4.1 QUOTATIONS (DEVIS)
CREATE TABLE IF NOT EXISTS devis (
    id_devis UUID PRIMARY KEY,
    organization_id UUID NOT NULL,
    numero_devis VARCHAR(100) NOT NULL UNIQUE,
    date_creation TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    date_validite TIMESTAMP,
    date_systeme TIMESTAMP,
    type VARCHAR(50),
    statut VARCHAR(50) DEFAULT 'DRAFT',
    id_client UUID NOT NULL,
    nom_client VARCHAR(255),
    adresse_client VARCHAR(500),
    email_client VARCHAR(255),
    telephone_client VARCHAR(100),
    lignes_devis JSONB,
    montant_ht NUMERIC(15,2),
    montant_tva NUMERIC(15,2),
    montant_ttc NUMERIC(15,2),
    montant_total NUMERIC(15,2),
    devise VARCHAR(20) DEFAULT 'XOF',
    taux_change NUMERIC(15,6) DEFAULT 1.0,
    apply_vat BOOLEAN DEFAULT TRUE,
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
    mode_reglement VARCHAR(50),
    nos_ref VARCHAR(100),
    vos_ref VARCHAR(100),
    nbre_echeance INTEGER,
    referal_client_id UUID,
    final_amount NUMERIC(15,2),
    created_by UUID,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    version BIGINT DEFAULT 0,
    
);

CREATE INDEX IF NOT EXISTS idx_devis_org ON devis(organization_id);
CREATE INDEX IF NOT EXISTS idx_devis_numero ON devis(numero_devis);
CREATE INDEX IF NOT EXISTS idx_devis_client ON devis(id_client);
CREATE INDEX IF NOT EXISTS idx_devis_statut ON devis(statut, organization_id);
CREATE INDEX IF NOT EXISTS idx_devis_date ON devis(date_creation);

-- 4.2 INVOICES (FACTURES)
CREATE TABLE IF NOT EXISTS factures (
    id_facture UUID PRIMARY KEY,
    organization_id UUID NOT NULL,
    numero_facture VARCHAR(100) NOT NULL UNIQUE,
    date_facturation TIMESTAMP,
    date_echeance TIMESTAMP,
    date_systeme TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    type VARCHAR(50),
    etat VARCHAR(50) DEFAULT 'DRAFT',
    id_client UUID NOT NULL,
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
    apply_vat BOOLEAN DEFAULT TRUE,
    devise VARCHAR(20) DEFAULT 'XOF',
    taux_change NUMERIC(15,6) DEFAULT 1.0,
    mode_reglement VARCHAR(50),
    conditions_paiement TEXT,
    nbre_echeance INTEGER,
    nos_ref VARCHAR(100),
    vos_ref VARCHAR(100),
    reference_commande VARCHAR(100),
    id_devis_origine UUID,
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
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    version BIGINT DEFAULT 0,

);

CREATE INDEX IF NOT EXISTS idx_factures_org ON factures(organization_id);
CREATE INDEX IF NOT EXISTS idx_factures_numero ON factures(numero_facture);
CREATE INDEX IF NOT EXISTS idx_factures_client ON factures(id_client);
CREATE INDEX IF NOT EXISTS idx_factures_etat ON factures(etat, organization_id);
CREATE INDEX IF NOT EXISTS idx_factures_date ON factures(date_facturation);
CREATE INDEX IF NOT EXISTS idx_factures_devis_origine ON factures(id_devis_origine);

-- 4.3 PROFORMA INVOICES (FACTURES PROFORMA)
CREATE TABLE IF NOT EXISTS factures_proforma (
    id_facture_proforma UUID PRIMARY KEY,
    organization_id UUID NOT NULL,
    numero_proforma_invoice VARCHAR(100) NOT NULL UNIQUE,
    date_creation TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    type VARCHAR(50),
    statut VARCHAR(50) DEFAULT 'DRAFT',
    id_client UUID NOT NULL,
    nom_client VARCHAR(255),
    adresse_client VARCHAR(500),
    email_client VARCHAR(255),
    telephone_client VARCHAR(100),
    lignes_facture_proforma JSONB,
    montant_ht NUMERIC(15,2),
    montant_tva NUMERIC(15,2),
    montant_ttc NUMERIC(15,2),
    montant_total NUMERIC(15,2),
    devise VARCHAR(20) DEFAULT 'XOF',
    taux_change NUMERIC(15,6) DEFAULT 1.0,
    apply_vat BOOLEAN DEFAULT TRUE,
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
    date_systeme DATE,
    mode_reglement VARCHAR(50),
    nos_ref VARCHAR(100),
    vos_ref VARCHAR(100),
    nbre_echeance INTEGER,
    referal_client_id UUID,
    final_amount NUMERIC(15,2),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
   
);

CREATE INDEX IF NOT EXISTS idx_factures_proforma_org ON factures_proforma(organization_id);
CREATE INDEX IF NOT EXISTS idx_factures_proforma_numero ON factures_proforma(numero_proforma_invoice);
CREATE INDEX IF NOT EXISTS idx_factures_proforma_client ON factures_proforma(id_client);
CREATE INDEX IF NOT EXISTS idx_factures_proforma_statut ON factures_proforma(statut, organization_id);

-- 4.4 CREDIT NOTES (NOTES DE CRÉDIT)
CREATE TABLE IF NOT EXISTS notes_credit (
    id_note_credit UUID PRIMARY KEY,
    organization_id UUID NOT NULL,
    numero_note_credit VARCHAR(100) NOT NULL UNIQUE,
    id_client UUID NOT NULL,
    nom_client VARCHAR(255),
    adresse_client VARCHAR(500),
    email_client VARCHAR(255),
    telephone_client VARCHAR(100),
    id_facture_origine UUID NOT NULL,
    numero_facture_origine VARCHAR(100),
    lignes_note_credit JSONB,
    montant_ht NUMERIC(15,2),
    montant_tva NUMERIC(15,2),
    montant_ttc NUMERIC(15,2),
    montant_total NUMERIC(15,2),
    date_emission TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    statut VARCHAR(50) DEFAULT 'DRAFT',
    motif TEXT,
    notes TEXT,
    devise VARCHAR(20) DEFAULT 'XOF',
    pdf_path VARCHAR(255),
    created_by UUID,
    validated_by UUID,
    validated_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
);

CREATE INDEX IF NOT EXISTS idx_notes_credit_org ON notes_credit(organization_id);
CREATE INDEX IF NOT EXISTS idx_notes_credit_numero ON notes_credit(numero_note_credit);
CREATE INDEX IF NOT EXISTS idx_notes_credit_client ON notes_credit(id_client);
CREATE INDEX IF NOT EXISTS idx_notes_credit_facture ON notes_credit(id_facture_origine);
CREATE INDEX IF NOT EXISTS idx_notes_credit_statut ON notes_credit(statut, organization_id);

-- ============================================================================
-- 5. PURCHASE ORDERS & DOCUMENTS
-- ============================================================================

-- 5.1 PURCHASE ORDERS (BONS DE COMMANDE)
CREATE TABLE IF NOT EXISTS bons_commande (
    id_bon_commande UUID PRIMARY KEY,
    organization_id UUID NOT NULL,
    numero_commande VARCHAR(100) NOT NULL UNIQUE,
    id_client UUID NOT NULL,
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
    date_commande TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    date_systeme TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    date_livraison_prevue TIMESTAMP,
    lines JSONB,
    montant_ht NUMERIC(15,2),
    montant_tva NUMERIC(15,2),
    montant_ttc NUMERIC(15,2),
    devise VARCHAR(20) DEFAULT 'XOF',
    apply_vat BOOLEAN DEFAULT TRUE,
    transport_method VARCHAR(100),
    id_agency UUID,
    mode_reglement VARCHAR(50),
    statut VARCHAR(50) DEFAULT 'DRAFT',
    notes TEXT,
    created_by UUID,
    validated_by UUID,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    validated_at TIMESTAMP,
   
);

CREATE INDEX IF NOT EXISTS idx_bons_commande_org ON bons_commande(organization_id);
CREATE INDEX IF NOT EXISTS idx_bons_commande_numero ON bons_commande(numero_commande);
CREATE INDEX IF NOT EXISTS idx_bons_commande_client ON bons_commande(id_client);
CREATE INDEX IF NOT EXISTS idx_bons_commande_statut ON bons_commande(statut, organization_id);

-- 5.2 PURCHASE ORDERS WITH SUPPLIERS (BONS D'ACHAT)
CREATE TABLE IF NOT EXISTS bons_achat (
    id_bon_achat UUID PRIMARY KEY,
    organization_id UUID NOT NULL,
    numero_bon_achat VARCHAR(100) NOT NULL UNIQUE,
    id_fournisseur UUID,
    nom_fournisseur VARCHAR(255),
    lignes_bon_achat JSONB,
    montant_ht NUMERIC(15,2),
    montant_tva NUMERIC(15,2),
    montant_ttc NUMERIC(15,2),
    date_achat TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    statut VARCHAR(50) DEFAULT 'DRAFT',
    notes TEXT,
    created_by UUID,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_bons_achat_organization FOREIGN KEY (organization_id) REFERENCES organizations(id) ON DELETE CASCADE,
    CONSTRAINT fk_bons_achat_fournisseur FOREIGN KEY (id_fournisseur) REFERENCES fournisseurs(id_fournisseur) ON DELETE SET NULL
);

CREATE INDEX IF NOT EXISTS idx_bons_achat_org ON bons_achat(organization_id);
CREATE INDEX IF NOT EXISTS idx_bons_achat_numero ON bons_achat(numero_bon_achat);
CREATE INDEX IF NOT EXISTS idx_bons_achat_fournisseur ON bons_achat(id_fournisseur);
CREATE INDEX IF NOT EXISTS idx_bons_achat_statut ON bons_achat(statut, organization_id);

-- 5.3 SUPPLIER INVOICES (FACTURES FOURNISSEUR)
CREATE TABLE IF NOT EXISTS factures_fournisseur (
    id_facture_fournisseur UUID PRIMARY KEY,
    organization_id UUID NOT NULL,
    numero_facture VARCHAR(100) NOT NULL UNIQUE,
    id_fournisseur UUID NOT NULL,
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
    statut VARCHAR(50) DEFAULT 'DRAFT',
    devise VARCHAR(20) DEFAULT 'XOF',
    notes TEXT,
    pdf_path VARCHAR(255),
    created_by UUID,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_factures_fournisseur_organization FOREIGN KEY (organization_id) REFERENCES organizations(id) ON DELETE CASCADE,
    CONSTRAINT fk_factures_fournisseur_supplier FOREIGN KEY (id_fournisseur) REFERENCES fournisseurs(id_fournisseur) ON DELETE RESTRICT
);

CREATE INDEX IF NOT EXISTS idx_factures_fournisseur_org ON factures_fournisseur(organization_id);
CREATE INDEX IF NOT EXISTS idx_factures_fournisseur_numero ON factures_fournisseur(numero_facture);
CREATE INDEX IF NOT EXISTS idx_factures_fournisseur_supplier ON factures_fournisseur(id_fournisseur);
CREATE INDEX IF NOT EXISTS idx_factures_fournisseur_statut ON factures_fournisseur(statut, organization_id);

-- ============================================================================
-- 6. LOGISTICS DOCUMENTS
-- ============================================================================

-- 6.1 DELIVERY NOTES (BONS DE LIVRAISON)
CREATE TABLE IF NOT EXISTS bons_livraison (
    id_bon_livraison UUID PRIMARY KEY,
    organization_id UUID NOT NULL,
    numero_livraison VARCHAR(100) NOT NULL UNIQUE,
    id_client UUID NOT NULL,
    nom_client VARCHAR(255),
    adresse_client VARCHAR(500),
    email_client VARCHAR(255),
    telephone_client VARCHAR(100),
    lignes_bon_livraison JSONB,
    montant_ht NUMERIC(15,2),
    montant_tva NUMERIC(15,2),
    montant_ttc NUMERIC(15,2),
    date_livraison TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    livraison_effectuee BOOLEAN DEFAULT FALSE,
    date_livraison_effective TIMESTAMP,
    statut VARCHAR(50) DEFAULT 'DRAFT',
    notes TEXT,
    created_by UUID,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_bons_livraison_organization FOREIGN KEY (organization_id) REFERENCES organizations(id) ON DELETE CASCADE,
    CONSTRAINT fk_bons_livraison_client FOREIGN KEY (id_client) REFERENCES clients(id_client) ON DELETE RESTRICT
);

CREATE INDEX IF NOT EXISTS idx_bons_livraison_org ON bons_livraison(organization_id);
CREATE INDEX IF NOT EXISTS idx_bons_livraison_numero ON bons_livraison(numero_livraison);
CREATE INDEX IF NOT EXISTS idx_bons_livraison_client ON bons_livraison(id_client);
CREATE INDEX IF NOT EXISTS idx_bons_livraison_statut ON bons_livraison(statut, organization_id);

-- 6.2 GOODS RECEIVED NOTES (BONS DE RÉCEPTION)
CREATE TABLE IF NOT EXISTS bons_reception (
    id_grn UUID PRIMARY KEY,
    organization_id UUID NOT NULL,
    numero_reception VARCHAR(100) NOT NULL UNIQUE,
    id_fournisseur UUID NOT NULL,
    nom_fournisseur VARCHAR(255),
    lines JSONB,
    montant_ht NUMERIC(15,2),
    montant_tva NUMERIC(15,2),
    montant_ttc NUMERIC(15,2),
    date_reception TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    statut VARCHAR(50) DEFAULT 'DRAFT',
    notes TEXT,
    created_by UUID,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_bons_reception_organization FOREIGN KEY (organization_id) REFERENCES organizations(id) ON DELETE CASCADE,
    CONSTRAINT fk_bons_reception_supplier FOREIGN KEY (id_fournisseur) REFERENCES fournisseurs(id_fournisseur) ON DELETE RESTRICT
);

CREATE INDEX IF NOT EXISTS idx_bons_reception_org ON bons_reception(organization_id);
CREATE INDEX IF NOT EXISTS idx_bons_reception_numero ON bons_reception(numero_reception);
CREATE INDEX IF NOT EXISTS idx_bons_reception_supplier ON bons_reception(id_fournisseur);
CREATE INDEX IF NOT EXISTS idx_bons_reception_statut ON bons_reception(statut, organization_id);

-- ============================================================================
-- 7. SEQUENCE TABLES FOR DOCUMENT NUMBERING (Optional)
-- ============================================================================
-- These tables track the next document number for each type
CREATE TABLE IF NOT EXISTS document_sequences (
    id UUID PRIMARY KEY,
    organization_id UUID NOT NULL,
    document_type VARCHAR(50) NOT NULL,
    next_sequence BIGINT DEFAULT 1,
    prefix VARCHAR(20),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_document_seq_organization FOREIGN KEY (organization_id) REFERENCES organizations(id) ON DELETE CASCADE,
    CONSTRAINT uk_document_seq UNIQUE(organization_id, document_type)
);

CREATE INDEX IF NOT EXISTS idx_document_sequences_org ON document_sequences(organization_id);

-- ============================================================================
-- 8. AUDIT & TRACKING
-- ============================================================================
-- Optional: Document audit trail
CREATE TABLE IF NOT EXISTS document_audit_logs (
    id UUID PRIMARY KEY,
    organization_id UUID NOT NULL,
    document_type VARCHAR(50) NOT NULL,
    document_id UUID NOT NULL,
    action VARCHAR(50) NOT NULL,
    old_status VARCHAR(50),
    new_status VARCHAR(50),
    changed_by UUID,
    changed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    notes TEXT,
    CONSTRAINT fk_audit_organization FOREIGN KEY (organization_id) REFERENCES organizations(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_audit_org ON document_audit_logs(organization_id);
CREATE INDEX IF NOT EXISTS idx_audit_document ON document_audit_logs(document_type, document_id);
CREATE INDEX IF NOT EXISTS idx_audit_date ON document_audit_logs(changed_at);

-- ============================================================================
-- 9. GRANTS & PERMISSIONS
-- ============================================================================
-- Ensure proper permissions for application user
-- GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA public TO billing_user;
-- GRANT USAGE, SELECT ON ALL SEQUENCES IN SCHEMA public TO billing_user;
