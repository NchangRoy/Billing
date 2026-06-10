-- Auto-generated SQL schema for facturation entities
-- Generated: 2026-02-27

-- Organizations table assumed to exist; organization_id references it.




CREATE TABLE portal_access_tokens (
    token_id BIGSERIAL PRIMARY KEY,
    token VARCHAR(255) NOT NULL UNIQUE,
    resource_type VARCHAR(50) NOT NULL,
    resource_id UUID NOT NULL,
    client_email VARCHAR(255),
    
    -- Permissions
    can_view BOOLEAN DEFAULT FALSE,
    can_modify BOOLEAN DEFAULT FALSE,
    can_accept BOOLEAN DEFAULT FALSE,
    can_reject BOOLEAN DEFAULT FALSE,
    
    -- Status & Audit
    used BOOLEAN DEFAULT FALSE,
    expires_at TIMESTAMP NOT NULL,
    used_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Index for fast lookup by token string
CREATE INDEX idx_portal_token_value ON portal_access_tokens(token);

-- Index for finding tokens by the resource they point to
CREATE INDEX idx_portal_resource_id ON portal_access_tokens(resource_id);




CREATE TABLE bons_achat (
    -- Primary Key
    id_bon_achat UUID PRIMARY KEY,
    
    -- Identification
    numero_bon_achat VARCHAR(100) UNIQUE,
    
    -- Supplier Information
    id_fournisseur UUID NOT NULL,
    nom_fournisseur VARCHAR(255),
    supplier_code VARCHAR(50),
    supplier_email VARCHAR(255),
    supplier_contact VARCHAR(100),
    supplier_address TEXT,
    
    -- Delivery Information
    delivery_name VARCHAR(255),
    delivery_address TEXT,
    delivery_email VARCHAR(255),
    delivery_contact VARCHAR(100),
    
    -- Dates & Logistics
    date_achat TIMESTAMP WITHOUT TIME ZONE,
    date_systeme TIMESTAMP WITHOUT TIME ZONE,
    date_livraison_prevue TIMESTAMP WITHOUT TIME ZONE,
    transport_method VARCHAR(100),
    instructions_livraison TEXT,
    
    -- Status & Financials
    statut VARCHAR(50), -- Enum: StatutBonAchat
    montant_ht NUMERIC(19, 4),
    montant_tva NUMERIC(19, 4),
    montant_ttc NUMERIC(19, 4),
    
    -- Nested Lines (The cause of your previous ClassCastException)
    lignes_bon_achat JSONB, 
    
    -- Audit & Remarks
    notes TEXT,
    prepared_by UUID,
    approved_by UUID,
    created_by UUID,
    organization_id UUID NOT NULL,
    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Performance Indexes
CREATE INDEX idx_ba_organization ON bons_achat(organization_id);


CREATE TABLE bons_commande (
    -- Primary Key
    id_bon_commande UUID PRIMARY KEY,
    
    -- Identification & Link to Quote
    numero_commande VARCHAR(100) UNIQUE,
    id_devis_origine UUID,
    numero_devis_origine VARCHAR(100),
    nos_ref VARCHAR(100),
    vos_ref VARCHAR(100),
    
    -- Client Information
    id_client UUID NOT NULL,
    nom_client VARCHAR(255),
    adresse_client TEXT,
    email_client VARCHAR(255),
    telephone_client VARCHAR(50),
    
    -- Recipient / Shipping Information
    recipient_name VARCHAR(255),
    recipient_phone VARCHAR(50),
    recipient_address TEXT,
    recipient_city VARCHAR(100),
    
    -- Dates & Logistics
    date_commande TIMESTAMP WITHOUT TIME ZONE,
    date_systeme TIMESTAMP WITHOUT TIME ZONE,
    date_livraison_prevue TIMESTAMP WITHOUT TIME ZONE,
    transport_method VARCHAR(100),
    id_agency UUID,
    
    -- Financials
    montant_ht NUMERIC(19, 4),
    montant_tva NUMERIC(19, 4),
    montant_ttc NUMERIC(19, 4),
    devise VARCHAR(10),
    apply_vat BOOLEAN DEFAULT TRUE,
    mode_reglement VARCHAR(50),
    
    -- Nested Items (JSONB for List<LineBonCommande>)
    lines JSONB,
    
    -- Status & Audit
    statut VARCHAR(50), -- Enum: StatusBonCommande
    notes TEXT,
    created_by UUID,
    validated_by UUID,
    validated_at TIMESTAMP WITHOUT TIME ZONE,
    
    -- Inherited from OrganizationScoped
    organization_id UUID NOT NULL,
    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Performance Indexes
CREATE INDEX idx_bc_org_id ON bons_commande(organization_id);



CREATE TABLE IF NOT EXISTS bons_reception (
    id_bon_reception UUID PRIMARY KEY,
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
    updated_at TIMESTAMP,
    date_systeme TIMESTAMP,
    numero_bon_achat VARCHAR(100),
    idBonAchat UUID,
    agence_de_transport VARCHAR(255)
);
CREATE INDEX IF NOT EXISTS idx_bons_reception_org ON bons_reception(organization_id);

CREATE TABLE bons_livraison (
    -- Primary Key
    id_bon_livraison UUID PRIMARY KEY,
    
    -- Identification
    numero_livraison VARCHAR(100) UNIQUE,
    
    -- Client Information
    id_client UUID NOT NULL,
    nom_client VARCHAR(255),
    adresse_client TEXT,
    email_client VARCHAR(255),
    telephone_client VARCHAR(50),
    
    -- Nested Items (JSONB for List<LigneBonLivraison>)
    lignes_bon_livraison JSONB,
    
    -- Financial Totals
    montant_ht NUMERIC(19, 4),
    montant_tva NUMERIC(19, 4),
    montant_ttc NUMERIC(19, 4),
    
    -- Status & Content
    statut VARCHAR(50), -- Enum: StatutBonLivraison
    notes TEXT,
    
    -- Dates
    date_livraison TIMESTAMP WITHOUT TIME ZONE,
    date_systeme TIMESTAMP WITHOUT TIME ZONE,
    
    -- Audit & Organization
    organization_id UUID NOT NULL,
    created_by UUID,
    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP
);


CREATE TABLE devis (
    id_devis UUID PRIMARY KEY,
    numero_devis VARCHAR(50),
    date_creation TIMESTAMP,
    date_validite TIMESTAMP,
    statut VARCHAR(50), -- Maps to StatutDevis Enum
    lignes_devis JSONB, -- Stores the List<LigneDevis> as a JSON array
    montant_total NUMERIC(19, 4),
    id_client UUID NOT NULL,
    nom_client VARCHAR(255),
    adresse_client TEXT,
    email_client VARCHAR(255),
    telephone_client VARCHAR(50),
    montant_ht NUMERIC(19, 4),
    montant_tva NUMERIC(19, 4),
    montant_ttc NUMERIC(19, 4),
    devise VARCHAR(10),
    taux_change NUMERIC(19, 6) DEFAULT 1.0,
    conditions_paiement TEXT,
    notes TEXT,
    reference_externe VARCHAR(100),
    envoye_par_email BOOLEAN DEFAULT FALSE,
    date_envoi_email TIMESTAMP,
    date_acceptation TIMESTAMP,
    date_refus TIMESTAMP,
    motif_refus TEXT,
    id_facture_convertie UUID,
    remise_globale_pourcentage NUMERIC(5, 2) DEFAULT 0.0,
    remise_globale_montant NUMERIC(19, 4) DEFAULT 0.0,
    validite_offre_jours INTEGER DEFAULT 30,
    apply_vat BOOLEAN DEFAULT TRUE,
    date_systeme TIMESTAMP,
    mode_reglement VARCHAR(50), -- Maps to TypePaiementDevis Enum
    nos_ref VARCHAR(100),
    vos_ref VARCHAR(100),
    nbre_echeance INTEGER,
    referal_client_id UUID,
    final_amount NUMERIC(19, 4),
    created_by UUID,
    updated_at TIMESTAMP,
    organization_id UUID,
    version BIGINT DEFAULT 0
);

-- Recommended Index for performance
CREATE INDEX idx_devis_organization ON devis(organization_id);



CREATE TABLE IF NOT EXISTS factures (
    id_facture UUID PRIMARY KEY,
    organization_id UUID,
    numero_facture VARCHAR(100),
    id_client UUID,
    nom_client VARCHAR(255),
    lines JSONB,
    montant_ht NUMERIC(15,2),
    montant_tva NUMERIC(15,2),
    montant_ttc NUMERIC(15,2),
    montant_total NUMERIC(15,2),
    remise_globale_montant NUMERIC(15,2),
    remise_globale_pourcentage NUMERIC(5,2),
    date_facture TIMESTAMP,
    date_echeance TIMESTAMP,
    statut VARCHAR(50),
    devise VARCHAR(20),
    notes TEXT,
    pdf_path VARCHAR(255),
    created_by UUID,
    validated_by UUID,
    validated_at TIMESTAMP,
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    date_systeme TIMESTAMP
);
CREATE INDEX IF NOT EXISTS idx_factures_org ON factures(organization_id);



CREATE TABLE factures_fournisseur (
    -- Primary Key
    id_facture_fournisseur UUID PRIMARY KEY,
    
    -- Identification
    numero_facture VARCHAR(100),
    numero_bon_reception VARCHAR(100), -- Maps to numeroBonReception
    id_bon_reception UUID,
    
    -- Supplier Information
    id_fournisseur UUID NOT NULL,
    nom_fournisseur VARCHAR(255),
    adresse_supplier TEXT,             -- Critical: matches @Column("adresse_supplier")
    email_fournisseur VARCHAR(255),
    telephone_fournisseur VARCHAR(50),
    
    -- Financials (Using Numeric for precision)
    montant_ht NUMERIC(19, 4),
    montant_tva NUMERIC(19, 4),
    montant_ttc NUMERIC(19, 4),
    montant_total NUMERIC(19, 4),
    montant_restant NUMERIC(19, 4),
    nbre_echeance NUMERIC(19, 4),
    
    -- Status & Config
    mode_reglement VARCHAR(50),        -- Enum mapped as String
    statut VARCHAR(50),                -- Enum mapped as String
    apply_vat BOOLEAN DEFAULT TRUE,
    devise VARCHAR(10),
    notes TEXT,
    pdf_path VARCHAR(255),
    
    -- Lines (Nested List)
    lines JSONB,                       -- Critical: stores the List<LineFactureFournisseur>
    
    -- Timestamps
    date_facture TIMESTAMP WITHOUT TIME ZONE,
    date_echeance TIMESTAMP WITHOUT TIME ZONE,
    date_systeme TIMESTAMP WITHOUT TIME ZONE,
    
    -- Audit & Organization
    organization_id UUID NOT NULL,
    created_by UUID,
    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Performance Indexes
CREATE INDEX idx_ff_org_id ON factures_fournisseur(organization_id);

CREATE TABLE factures_proforma (
    -- Primary Key
    id_facture_proforma UUID PRIMARY KEY,
    
    -- Identification & Dates
    numero_proforma_invoice VARCHAR(50),
    date_creation TIMESTAMP,
    date_systeme DATE, -- Mapped to LocalDate in Java
    
    -- Enums (Stored as Strings)
    statut VARCHAR(50), 
    mode_reglement VARCHAR(50),
    type VARCHAR(50),
    
    -- Client Information
    id_client UUID NOT NULL,
    nom_client VARCHAR(255),
    adresse_client TEXT,
    email_client VARCHAR(255),
    telephone_client VARCHAR(50),
    
    -- Financial Totals
    montant_ht NUMERIC(19, 4),
    montant_tva NUMERIC(19, 4),
    montant_ttc NUMERIC(19, 4),
    montant_total NUMERIC(19, 4),
    final_amount NUMERIC(19, 4),
    remise_globale_pourcentage NUMERIC(5, 2),
    remise_globale_montant NUMERIC(19, 4),
    
    -- Currency & Conditions
    devise VARCHAR(10),
    taux_change NUMERIC(19, 6),
    conditions_paiement TEXT,
    notes TEXT,
    validite_offre_jours INTEGER,
    
    -- Nested Data (CRITICAL: Requires JSONB)
    lignes_facture_proforma JSONB, 
    
    -- References & Logistics
    reference_externe VARCHAR(100),
    nos_ref VARCHAR(100),
    vos_ref VARCHAR(100),
    nbre_echeance INTEGER,
    referal_client_id UUID,
    id_facture_convertie UUID,
    pdf_path VARCHAR(255),
    
    -- Status Flags & Workflow
    apply_vat BOOLEAN DEFAULT TRUE,
    envoye_par_email BOOLEAN DEFAULT FALSE,
    date_envoi_email TIMESTAMP,
    date_acceptation TIMESTAMP,
    date_refus TIMESTAMP,
    motif_refus TEXT,
    
    -- Metadata (Audit)
    organization_id UUID NOT NULL,
    created_by UUID,
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);

-- Performance Indexes
CREATE INDEX idx_proforma_org ON factures_proforma(organization_id);


CREATE TABLE IF NOT EXISTS journals (
    id_journal UUID PRIMARY KEY,
    organization_id UUID,
    journal_date TIMESTAMP,
    description TEXT,
    reference VARCHAR(255),
    amount NUMERIC(15,2),
    created_at TIMESTAMP
);
CREATE INDEX IF NOT EXISTS idx_journals_org ON journals(organization_id);

-- Line items tables (if lines are stored as separate records)
CREATE TABLE IF NOT EXISTS lignes_bon_achat (
    id_ligne UUID PRIMARY KEY,
    id_bon_achat UUID,
    organization_id UUID,
    product_id UUID,
    description TEXT,
    quantite NUMERIC(15,4),
    prix_unitaire NUMERIC(15,4),
    montant_total NUMERIC(15,2),
    tva NUMERIC(15,2)
);
CREATE INDEX IF NOT EXISTS idx_lignes_bon_achat_parent ON lignes_bon_achat(id_bon_achat);

CREATE TABLE IF NOT EXISTS lignes_bon_livraison (
    id_ligne UUID PRIMARY KEY,
    id_bon_livraison UUID,
    organization_id UUID,
    product_id UUID,
    description TEXT,
    quantite NUMERIC(15,4),
    prix_unitaire NUMERIC(15,4),
    montant_total NUMERIC(15,2)
);
CREATE INDEX IF NOT EXISTS idx_lignes_bon_livraison_parent ON lignes_bon_livraison(id_bon_livraison);

CREATE TABLE IF NOT EXISTS lignes_devis (
    id_ligne UUID PRIMARY KEY,
    id_devis UUID,
    organization_id UUID,
    product_id UUID,
    description TEXT,
    quantite NUMERIC(15,4),
    prix_unitaire NUMERIC(15,4),
    montant_total NUMERIC(15,2)
);
CREATE INDEX IF NOT EXISTS idx_lignes_devis_parent ON lignes_devis(id_devis);

CREATE TABLE IF NOT EXISTS lignes_facture (
    id_ligne UUID PRIMARY KEY,
    id_facture UUID,
    organization_id UUID,
    product_id UUID,
    description TEXT,
    quantite NUMERIC(15,4),
    prix_unitaire NUMERIC(15,4),
    montant_total NUMERIC(15,2),
    tva NUMERIC(15,2)
);
CREATE INDEX IF NOT EXISTS idx_lignes_facture_parent ON lignes_facture(id_facture);

CREATE TABLE IF NOT EXISTS lignes_facture_proforma (
    id_ligne UUID PRIMARY KEY,
    id_facture_proforma UUID,
    organization_id UUID,
    product_id UUID,
    description TEXT,
    quantite NUMERIC(15,4),
    prix_unitaire NUMERIC(15,4),
    montant_total NUMERIC(15,2)
);
CREATE INDEX IF NOT EXISTS idx_lignes_facture_proforma_parent ON lignes_facture_proforma(id_facture_proforma);

CREATE TABLE IF NOT EXISTS lignes_note_credit (
    id_ligne UUID PRIMARY KEY,
    id_note_credit UUID,
    organization_id UUID,
    product_id UUID,
    description TEXT,
    quantite NUMERIC(15,4),
    prix_unitaire NUMERIC(15,4),
    montant_total NUMERIC(15,2)
);
CREATE INDEX IF NOT EXISTS idx_lignes_note_credit_parent ON lignes_note_credit(id_note_credit);
CREATE TABLE notes_credit (
    -- Primary Key
    id_note_credit UUID PRIMARY KEY,
    
    -- Document Identification
    numero_note_credit VARCHAR(100) UNIQUE,
    id_facture_origine UUID,
    numero_facture_origine VARCHAR(100),
    
    -- Client Information
    id_client UUID NOT NULL,
    nom_client VARCHAR(255),
    adresse_client TEXT,
    email_client VARCHAR(255),
    telephone_client VARCHAR(50),
    
    -- Financial Totals
    montant_ht NUMERIC(19, 4),
    montant_tva NUMERIC(19, 4),
    montant_ttc NUMERIC(19, 4),
    montant_total NUMERIC(19, 4),
    
    -- Nested Line Items
    lignes_note_credit JSONB, -- Stores List<LigneNoteCredit>
    
    -- Status and Configuration
    mode_reglement VARCHAR(50), -- Enum: ModeReglementNoteCredit
    statut VARCHAR(50),         -- Enum: StatutNoteCredit
    devise VARCHAR(10),
    motif TEXT,
    notes TEXT,
    pdf_path VARCHAR(255),
    
    -- Date Information
    date_emission TIMESTAMP WITHOUT TIME ZONE,
    date_systeme TIMESTAMP WITHOUT TIME ZONE,
    validated_at TIMESTAMP WITHOUT TIME ZONE,
    
    -- Audit & Organization (Inherited)
    organization_id UUID NOT NULL,
    created_by UUID,
    validated_by UUID,
    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Indexes for performance
CREATE INDEX idx_note_credit_org ON notes_credit(organization_id);


CREATE TABLE IF NOT EXISTS paiements (
    id_paiement UUID PRIMARY KEY,
    organization_id UUID,
    id_facture UUID,
    montant NUMERIC(15,2),
    date_paiement TIMESTAMP,
    methode VARCHAR(100),
    reference VARCHAR(255),
    created_by UUID,
    created_at TIMESTAMP
);
CREATE INDEX IF NOT EXISTS idx_paiements_org ON paiements(organization_id);

CREATE TABLE IF NOT EXISTS taxes (
    id_taxe UUID PRIMARY KEY,
    organization_id UUID,
    code VARCHAR(50),
    libelle VARCHAR(255),
    taux NUMERIC(8,4),
    created_at TIMESTAMP
);
CREATE INDEX IF NOT EXISTS idx_taxes_org ON taxes(organization_id);




DROP TABLE IF EXISTS products;

CREATE TABLE products (
    -- Primary Key (line_id)
    line_id BIGSERIAL PRIMARY KEY,
    
    -- Business ID (UUID)
    id UUID NOT NULL,
    
    -- Basic Product Info
    name VARCHAR(255),
    product_type VARCHAR(100), -- Maps to @Column("product_type")
    sale_price NUMERIC(19, 2), -- Using NUMERIC for precision with BigDecimal
    cost NUMERIC(19, 2),
    category VARCHAR(100),
    reference VARCHAR(100),
    barcode VARCHAR(100),
    photo TEXT,
    active BOOLEAN DEFAULT TRUE,
    
    -- Audit Dates
    created_at DATE,
    updated_at DATE,
    
    -- Logistics & Units
    uom VARCHAR(50),
    stock_quantity DOUBLE PRECISION,
    available_quantity DOUBLE PRECISION,
    reserved_quantity DOUBLE PRECISION,
    
    -- Organization context
    organization_id UUID,

    -- Complex JSON Fields (The lists of ClientSaleSize and SaleSizePromotion)
    -- This requires the R2DBC Converters we discussed earlier
    allowed_sale_sizes JSONB,
    active_promotions JSONB
);

-- Optimization: Index the UUID and Organization ID for faster lookups
CREATE INDEX idx_products_uuid ON products(id);
CREATE INDEX idx_products_org_id ON products(organization_id);
-- Add foreign key examples (uncomment/adjust if referenced parent tables exist):
-- ALTER TABLE bons_reception ADD CONSTRAINT fk_bons_reception_org FOREIGN KEY (organization_id) REFERENCES organizations(id) ON DELETE CASCADE;
-- ALTER TABLE factures_fournisseur ADD CONSTRAINT fk_factures_fournisseur_org FOREIGN KEY (organization_id) REFERENCES organizations(id) ON DELETE CASCADE;

-- End of generated schema
