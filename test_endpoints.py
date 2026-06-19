import requests
import json
import uuid

BASE_URL = "http://localhost:8080"
ORG_ID = "d3b07384-d113-4953-a5e3-0350718c962b"
CLIENT_ID = "e1a7b458-12c8-472d-bb91-030a21a92e10"
SUPPLIER_ID = "f2c8d569-34b9-482e-cc02-140b32ba3f21"

# Automatically attach organization ID header to all requests
requests = requests.Session()
requests.headers.update({"X-Organization-ID": ORG_ID})

def print_result(method, url, response):
    print(f"\n==================================================")
    print(f"{method} {url}")
    print(f"Status Code: {response.status_code}")
    try:
        print("Response Body:")
        print(json.dumps(response.json(), indent=2))
    except Exception:
        print("Response Text:", response.text)
    print(f"==================================================")

# 1. Product Controller Endpoints
print("\n--- TESTING PRODUCTS ---")
product_id = str(uuid.uuid4())
product_payload = {
    "idProduit": product_id,
    "nomProduit": "Laptop Pro",
    "typeProduit": "ARTICLE",
    "prixVente": 1200.0,
    "cout": 800.0,
    "categorie": "Electronics",
    "reference": "PROD-LPT",
    "active": True,
    "organizationId": ORG_ID
}
r = requests.post(f"{BASE_URL}/api/v1/products", json=product_payload)
print_result("POST", "/api/v1/products", r)

r = requests.get(f"{BASE_URL}/api/v1/products")
print_result("GET", "/api/v1/products", r)

r = requests.get(f"{BASE_URL}/api/v1/products/{product_id}")
print_result("GET", f"/api/v1/products/{product_id}", r)

product_payload["prixVente"] = 1250.0
r = requests.put(f"{BASE_URL}/api/v1/products/{product_id}", json=product_payload)
print_result("PUT", f"/api/v1/products/{product_id}", r)

r = requests.get(f"{BASE_URL}/api/v1/products/organization/{ORG_ID}")
print_result("GET", f"/api/v1/products/organization/{ORG_ID}", r)


# 2. Taxe Controller Endpoints
print("\n--- TESTING TAXES ---")
tax_id = None
tax_payload = {
    "nomTaxe": "VAT 20%",
    "calculTaxe": 20.0,
    "actif": True,
    "typeTaxe": "TVA",
    "porteTaxe": "VENTES",
    "montant": 240.0,
    "positionFiscale": "NATIONAL"
}
r = requests.post(f"{BASE_URL}/api/taxes", json=tax_payload)
print_result("POST", "/api/taxes", r)
if r.status_code in [200, 201]:
    tax_id = r.json().get("idTaxe")

if tax_id:
    r = requests.get(f"{BASE_URL}/api/taxes")
    print_result("GET", "/api/taxes", r)

    r = requests.get(f"{BASE_URL}/api/taxes/{tax_id}")
    print_result("GET", f"/api/taxes/{tax_id}", r)

    tax_payload["montant"] = 250.0
    r = requests.put(f"{BASE_URL}/api/taxes/{tax_id}", json=tax_payload)
    print_result("PUT", f"/api/taxes/{tax_id}", r)

    r = requests.put(f"{BASE_URL}/api/taxes/{tax_id}/desactiver")
    print_result("PUT", f"/api/taxes/{tax_id}/desactiver", r)

    r = requests.put(f"{BASE_URL}/api/taxes/{tax_id}/activer")
    print_result("PUT", f"/api/taxes/{tax_id}/activer", r)

    r = requests.get(f"{BASE_URL}/api/taxes/actives")
    print_result("GET", "/api/taxes/actives", r)

    r = requests.get(f"{BASE_URL}/api/taxes/nom/VAT 20%")
    print_result("GET", "/api/taxes/nom/VAT 20%", r)

    r = requests.get(f"{BASE_URL}/api/taxes/type/TVA")
    print_result("GET", "/api/taxes/type/TVA", r)

    r = requests.get(f"{BASE_URL}/api/taxes/type/TVA/actives")
    print_result("GET", "/api/taxes/type/TVA/actives", r)

    r = requests.get(f"{BASE_URL}/api/taxes/porte/VENTES")
    print_result("GET", "/api/taxes/porte/VENTES", r)

    r = requests.get(f"{BASE_URL}/api/taxes/position/NATIONAL")
    print_result("GET", "/api/taxes/position/NATIONAL", r)

    r = requests.get(f"{BASE_URL}/api/taxes/montant-range", params={"minMontant": 100, "maxMontant": 300})
    print_result("GET", "/api/taxes/montant-range", r)

    r = requests.get(f"{BASE_URL}/api/taxes/calcul-range", params={"minTaux": 10, "maxTaux": 25})
    print_result("GET", "/api/taxes/calcul-range", r)

    r = requests.get(f"{BASE_URL}/api/taxes/count/actives")
    print_result("GET", "/api/taxes/count/actives", r)

    r = requests.get(f"{BASE_URL}/api/taxes/count/type/TVA")
    print_result("GET", "/api/taxes/count/type/TVA", r)


# 3. Journal Controller Endpoints
print("\n--- TESTING JOURNALS ---")
journal_id = None
journal_payload = {
    "nomJournal": "Sales Journal",
    "type": "VENTE"
}
r = requests.post(f"{BASE_URL}/api/journals", json=journal_payload)
print_result("POST", "/api/journals", r)
if r.status_code in [200, 201]:
    journal_id = r.json().get("idJournal")

if journal_id:
    r = requests.get(f"{BASE_URL}/api/journals")
    print_result("GET", "/api/journals", r)

    r = requests.get(f"{BASE_URL}/api/journals/{journal_id}")
    print_result("GET", f"/api/journals/{journal_id}", r)

    journal_payload["nomJournal"] = "Updated Sales Journal"
    r = requests.put(f"{BASE_URL}/api/journals/{journal_id}", json=journal_payload)
    print_result("PUT", f"/api/journals/{journal_id}", r)

    r = requests.get(f"{BASE_URL}/api/journals/nom/Updated Sales Journal")
    print_result("GET", "/api/journals/nom/Updated Sales Journal", r)

    r = requests.get(f"{BASE_URL}/api/journals/type/VENTE")
    print_result("GET", "/api/journals/type/VENTE", r)

    r = requests.get(f"{BASE_URL}/api/journals/search", params={"nom": "Sales"})
    print_result("GET", "/api/journals/search", r)

    r = requests.get(f"{BASE_URL}/api/journals/count/type/VENTE")
    print_result("GET", "/api/journals/count/type/VENTE", r)


# 4. Devis Controller Endpoints
print("\n--- TESTING DEVIS ---")
devis_id = None
devis_payload = {
    "numeroDevis": "DEV-2026-001",
    "idClient": CLIENT_ID,
    "nomClient": "Test Client Company",
    "montantTotal": 1200.0,
    "lignesDevis": [
        {
            "quantite": 1,
            "description": "Laptop Pro",
            "prixUnitaire": 1200.0,
            "montantTotal": 1200.0,
            "remisePourcentage": 0.0,
            "remiseMontant": 0.0
        }
    ],
    "organizationId": ORG_ID
}
r = requests.post(f"{BASE_URL}/api/devis", json=devis_payload)
print_result("POST", "/api/devis", r)
if r.status_code in [200, 201]:
    devis_id = r.json().get("idDevis")

if devis_id:
    r = requests.get(f"{BASE_URL}/api/devis")
    print_result("GET", "/api/devis", r)

    r = requests.get(f"{BASE_URL}/api/devis/{devis_id}")
    print_result("GET", f"/api/devis/{devis_id}", r)

    devis_payload["montantTotal"] = 1250.0
    r = requests.put(f"{BASE_URL}/api/devis/{devis_id}", json=devis_payload)
    print_result("PUT", f"/api/devis/{devis_id}", r)

    r = requests.put(f"{BASE_URL}/api/devis/{devis_id}/accepter")
    print_result("PUT", f"/api/devis/{devis_id}/accepter", r)

    r = requests.put(f"{BASE_URL}/api/devis/{devis_id}/refuser")
    print_result("PUT", f"/api/devis/{devis_id}/refuser", r)

    r = requests.get(f"{BASE_URL}/api/devis/numero/DEV-2026-001")
    print_result("GET", "/api/devis/numero/DEV-2026-001", r)

    r = requests.get(f"{BASE_URL}/api/devis/enriched/{ORG_ID}")
    print_result("GET", f"/api/devis/enriched/{ORG_ID}", r)


# 5. Facture Controller Endpoints
print("\n--- TESTING FACTURES ---")
facture_id = None
facture_payload = {
    "numeroFacture": "FAC-2026-001",
    "dateFacturation": "2026-06-13T12:00:00Z",
    "dateEcheance": "2026-07-13T12:00:00Z",
    "idClient": CLIENT_ID,
    "lignesFacture": [
        {
            "quantite": 1,
            "description": "Laptop Pro",
            "debit": 1200.0,
            "credit": 0.0,
            "prixUnitaire": 1200.0,
            "montantTotal": 1200.0
        }
    ],
    "organizationId": ORG_ID,
    "etat": "BROUILLON"
}
r = requests.post(f"{BASE_URL}/api/factures", json=facture_payload)
print_result("POST", "/api/factures", r)
if r.status_code in [200, 201]:
    facture_id = r.json().get("idFacture")

if facture_id:
    r = requests.get(f"{BASE_URL}/api/factures")
    print_result("GET", "/api/factures", r)

    r = requests.get(f"{BASE_URL}/api/factures/{facture_id}")
    print_result("GET", f"/api/factures/{facture_id}", r)

    facture_payload["numeroFacture"] = "FAC-2026-001-REV"
    r = requests.put(f"{BASE_URL}/api/factures/{facture_id}", json=facture_payload)
    print_result("PUT", f"/api/factures/{facture_id}", r)

    r = requests.put(f"{BASE_URL}/api/factures/{facture_id}/paiement", params={"montantPaye": 500.0})
    print_result("PUT", f"/api/factures/{facture_id}/paiement", r)

    r = requests.put(f"{BASE_URL}/api/factures/{facture_id}/marquer-paye")
    print_result("PUT", f"/api/factures/{facture_id}/marquer-paye", r)

    r = requests.get(f"{BASE_URL}/api/factures/numero/FAC-2026-001-REV")
    print_result("GET", "/api/factures/numero/FAC-2026-001-REV", r)

    r = requests.get(f"{BASE_URL}/api/factures/non-payees")
    print_result("GET", "/api/factures/non-payees", r)

    r = requests.get(f"{BASE_URL}/api/factures/retard")
    print_result("GET", "/api/factures/retard", r)

    r = requests.get(f"{BASE_URL}/api/factures/etat/PAYE")
    print_result("GET", "/api/factures/etat/PAYE", r)

    r = requests.get(f"{BASE_URL}/api/factures/count/etat/PAYE")
    print_result("GET", "/api/factures/count/etat/PAYE", r)

    r = requests.get(f"{BASE_URL}/api/factures/client/{CLIENT_ID}")
    print_result("GET", f"/api/factures/client/{CLIENT_ID}", r)

    r = requests.get(f"{BASE_URL}/api/factures/periode", params={"dateDebut": "2026-06-01", "dateFin": "2026-06-30"})
    print_result("GET", "/api/factures/periode", r)

    r = requests.get(f"{BASE_URL}/api/factures/account/{facture_id}")
    print_result("GET", f"/api/factures/account/{facture_id}", r)


# 6. Paiement Controller Endpoints
print("\n--- TESTING PAIEMENTS ---")
paiement_id = None
paiement_payload = {
    "idClient": CLIENT_ID,
    "montant": 500.0,
    "date": "2026-06-13",
    "journal": "CASH",
    "modePaiement": "ESPECES",
    "idFacture": facture_id
}
r = requests.post(f"{BASE_URL}/api/paiement", json=paiement_payload)
print_result("POST", "/api/paiement", r)
if r.status_code in [200, 201]:
    paiement_id = r.json().get("idPaiement")

if paiement_id:
    r = requests.get(f"{BASE_URL}/api/paiement")
    print_result("GET", "/api/paiement", r)

    r = requests.get(f"{BASE_URL}/api/paiement/{paiement_id}")
    print_result("GET", f"/api/paiement/{paiement_id}", r)

    paiement_payload["montant"] = 600.0
    r = requests.put(f"{BASE_URL}/api/paiement/{paiement_id}", json=paiement_payload)
    print_result("PUT", f"/api/paiement/{paiement_id}", r)

    if facture_id:
        r = requests.get(f"{BASE_URL}/api/paiement/facture/{facture_id}")
        print_result("GET", f"/api/paiement/facture/{facture_id}", r)

    r = requests.get(f"{BASE_URL}/api/paiement/client/{CLIENT_ID}")
    print_result("GET", f"/api/paiement/client/{CLIENT_ID}", r)


# 7. Facture Proforma Endpoints
print("\n--- TESTING FACTURES PROFORMA ---")
proforma_id = None
proforma_payload = {
    "idClient": CLIENT_ID,
    "numeroProformaInvoice": "PROF-2026-001",
    "lignes": [
        {
            "idProduit": product_id,
            "nomProduit": "Laptop Pro",
            "quantite": 1,
            "prixUnitaire": 1200.0,
            "montantTotal": 1200.0
        }
    ],
    "organizationId": ORG_ID
}
r = requests.post(f"{BASE_URL}/api/factures-proforma", json=proforma_payload)
print_result("POST", "/api/factures-proforma", r)
if r.status_code in [200, 201]:
    proforma_id = r.json().get("idFactureProforma")

if proforma_id:
    r = requests.get(f"{BASE_URL}/api/factures-proforma")
    print_result("GET", "/api/factures-proforma", r)

    r = requests.get(f"{BASE_URL}/api/factures-proforma/{proforma_id}")
    print_result("GET", f"/api/factures-proforma/{proforma_id}", r)

    proforma_payload["numeroProformaInvoice"] = "PROF-2026-001-REV"
    r = requests.put(f"{BASE_URL}/api/factures-proforma/{proforma_id}", json=proforma_payload)
    print_result("PUT", f"/api/factures-proforma/{proforma_id}", r)

    r = requests.patch(f"{BASE_URL}/api/factures-proforma/{proforma_id}/statut", params={"statut": "ACCEPTE"})
    print_result("PATCH", f"/api/factures-proforma/{proforma_id}/statut", r)

    r = requests.get(f"{BASE_URL}/api/factures-proforma/client/{CLIENT_ID}")
    print_result("GET", f"/api/factures-proforma/client/{CLIENT_ID}", r)


# 8. Bon de livraison (Delivery Notes) Endpoints
print("\n--- TESTING BONS DE LIVRAISON ---")
delivery_id = None
delivery_payload = {
    "numeroBonLivraison": "DLV-2026-001",
    "idClient": CLIENT_ID,
    "dateLivraison": "2026-06-13T12:00:00Z",
    "lignes": [
        {
            "idProduit": product_id,
            "description": "Delivery of Laptop Pro",
            "quantite": 1,
            "prixUnitaire": 1200.0,
            "montant": 1200.0
        }
    ],
    "organizationId": ORG_ID
}
r = requests.post(f"{BASE_URL}/api/bons-livraison", json=delivery_payload)
print_result("POST", "/api/bons-livraison", r)
if r.status_code in [200, 201]:
    delivery_id = r.json().get("idBonLivraison")

if delivery_id:
    r = requests.get(f"{BASE_URL}/api/bons-livraison")
    print_result("GET", "/api/bons-livraison", r)

    r = requests.get(f"{BASE_URL}/api/bons-livraison/{delivery_id}")
    print_result("GET", f"/api/bons-livraison/{delivery_id}", r)

    delivery_payload["numeroBonLivraison"] = "DLV-2026-001-REV"
    r = requests.put(f"{BASE_URL}/api/bons-livraison/{delivery_id}", json=delivery_payload)
    print_result("PUT", f"/api/bons-livraison/{delivery_id}", r)

    r = requests.patch(f"{BASE_URL}/api/bons-livraison/{delivery_id}/statut", params={"statut": "EXPEDIE"})
    print_result("PATCH", f"/api/bons-livraison/{delivery_id}/statut", r)

    r = requests.post(f"{BASE_URL}/api/bons-livraison/{delivery_id}/effectuer")
    print_result("POST", f"/api/bons-livraison/{delivery_id}/effectuer", r)

    r = requests.get(f"{BASE_URL}/api/bons-livraison/client/{CLIENT_ID}")
    print_result("GET", f"/api/bons-livraison/client/{CLIENT_ID}", r)


# 9. Bon d'achat Endpoints
print("\n--- TESTING BONS D'ACHAT ---")
purchase_id = None
purchase_payload = {
    "numeroBonAchat": "PO-2026-001",
    "supplierId": SUPPLIER_ID,
    "status": "BROUILLON",
    "lines": [
        {
            "productId": product_id,
            "orderedQuantity": 5,
            "unitPrice": 800.0,
            "totalAmount": 4000.0
        }
    ],
    "organizationId": ORG_ID
}
r = requests.post(f"{BASE_URL}/api/bons-achat", json=purchase_payload)
print_result("POST", "/api/bons-achat", r)
if r.status_code in [200, 201]:
    purchase_id = r.json().get("idBonAchat")

if purchase_id:
    r = requests.get(f"{BASE_URL}/api/bons-achat")
    print_result("GET", "/api/bons-achat", r)

    r = requests.get(f"{BASE_URL}/api/bons-achat/{purchase_id}")
    print_result("GET", f"/api/bons-achat/{purchase_id}", r)

    purchase_payload["numeroBonAchat"] = "PO-2026-001-REV"
    r = requests.put(f"{BASE_URL}/api/bons-achat/{purchase_id}", json=purchase_payload)
    print_result("PUT", f"/api/bons-achat/{purchase_id}", r)


# 10. Note de crédit Endpoints
print("\n--- TESTING NOTES DE CREDIT ---")
credit_id = None
credit_payload = {
    "numeroNoteCredit": "CN-2026-001",
    "idClient": CLIENT_ID,
    "lignesNoteCredit": [
        {
            "quantite": 1,
            "description": "Credit for Laptop Pro",
            "debit": 0.0,
            "credit": 1200.0,
            "prixUnitaire": 1200.0,
            "montantTotal": 1200.0
        }
    ],
    "organizationId": ORG_ID
}
r = requests.post(f"{BASE_URL}/api/v1/facturation/note-credits", json=credit_payload)
print_result("POST", "/api/v1/facturation/note-credits", r)
if r.status_code in [200, 201]:
    credit_id = r.json().get("idNoteCredit")

if credit_id:
    r = requests.get(f"{BASE_URL}/api/v1/facturation/note-credits")
    print_result("GET", "/api/v1/facturation/note-credits", r)

    r = requests.get(f"{BASE_URL}/api/v1/facturation/note-credits/{credit_id}")
    print_result("GET", f"/api/v1/facturation/note-credits/{credit_id}", r)

    credit_payload["numeroNoteCredit"] = "CN-2026-001-REV"
    r = requests.put(f"{BASE_URL}/api/v1/facturation/note-credits/{credit_id}", json=credit_payload)
    print_result("PUT", f"/api/v1/facturation/note-credits/{credit_id}", r)


# 11. Bon de réception Endpoints
print("\n--- TESTING BONS DE RECEPTION ---")
reception_id = None
reception_payload = {
    "numeroReception": "GRN-2026-001",
    "idFournisseur": SUPPLIER_ID,
    "lines": [
        {
            "productId": product_id,
            "orderedQuantity": 5.0,
            "receivedQuantity": 5.0,
            "acceptedQuantity": 5.0
        }
    ],
    "organizationId": ORG_ID
}
r = requests.post(f"{BASE_URL}/api/v1/facturation/bon-receptions", json=reception_payload)
print_result("POST", "/api/v1/facturation/bon-receptions", r)
if r.status_code in [200, 201]:
    reception_id = r.json().get("idBonReception")

if reception_id:
    r = requests.get(f"{BASE_URL}/api/v1/facturation/bon-receptions")
    print_result("GET", "/api/v1/facturation/bon-receptions", r)

    r = requests.get(f"{BASE_URL}/api/v1/facturation/bon-receptions/{reception_id}")
    print_result("GET", f"/api/v1/facturation/bon-receptions/{reception_id}", r)

    reception_payload["numeroReception"] = "GRN-2026-001-REV"
    r = requests.put(f"{BASE_URL}/api/v1/facturation/bon-receptions/{reception_id}", json=reception_payload)
    print_result("PUT", f"/api/v1/facturation/bon-receptions/{reception_id}", r)


# 12. Analytics & Tableau de bord
print("\n--- TESTING ANALYTICS & DASHBOARD ---")
r = requests.get(f"{BASE_URL}/api/analytics/ventes/periode", params={"startDate": "2026-06-01", "endDate": "2026-06-30"})
print_result("GET", "/api/analytics/ventes/periode", r)

r = requests.get(f"{BASE_URL}/api/analytics/clients/top", params={"limit": 5})
print_result("GET", "/api/analytics/clients/top", r)

r = requests.get(f"{BASE_URL}/api/tableau-de-bord")
print_result("GET", "/api/tableau-de-bord", r)


# 13. Portal Access Tokens Endpoints
print("\n--- TESTING PORTAL ACCESS TOKENS ---")
token = None
token_payload = {
    "resourceId": devis_id or str(uuid.uuid4()),
    "resourceType": "QUOTATION",
    "clientEmail": "client@test.com"
}
r = requests.post(f"{BASE_URL}/api/v1/portal-tokens/generate", params=token_payload)
print_result("POST", "/api/v1/portal-tokens/generate", r)
if r.status_code in [200, 201]:
    token = r.json().get("token")

if token:
    r = requests.get(f"{BASE_URL}/api/v1/portal-tokens")
    print_result("GET", "/api/v1/portal-tokens", r)

    r = requests.get(f"{BASE_URL}/api/v1/portal-tokens/validate/{token}")
    print_result("GET", f"/api/v1/portal-tokens/validate/{token}", r)

    r = requests.get(f"{BASE_URL}/portal-access/quotation/{token}")
    print_result("GET", f"/portal-access/quotation/{token}", r)

    r = requests.get(f"{BASE_URL}/portal-access/{token}", params={"action": "ACCEPT"})
    print_result("GET", f"/portal-access/{token}", r)


# Clean up / Delete operations
print("\n--- TESTING DELETIONS ---")
if reception_id:
    r = requests.delete(f"{BASE_URL}/api/v1/facturation/bon-receptions/{reception_id}")
    print_result("DELETE", f"/api/v1/facturation/bon-receptions/{reception_id}", r)

if credit_id:
    r = requests.delete(f"{BASE_URL}/api/v1/facturation/note-credits/{credit_id}")
    print_result("DELETE", f"/api/v1/facturation/note-credits/{credit_id}", r)

if purchase_id:
    r = requests.delete(f"{BASE_URL}/api/bons-achat/{purchase_id}")
    print_result("DELETE", f"/api/bons-achat/{purchase_id}", r)

if delivery_id:
    r = requests.delete(f"{BASE_URL}/api/bons-livraison/{delivery_id}")
    print_result("DELETE", f"/api/bons-livraison/{delivery_id}", r)

if proforma_id:
    r = requests.delete(f"{BASE_URL}/api/factures-proforma/{proforma_id}")
    print_result("DELETE", f"/api/factures-proforma/{proforma_id}", r)

if paiement_id:
    r = requests.delete(f"{BASE_URL}/api/paiement/{paiement_id}")
    print_result("DELETE", f"/api/paiement/{paiement_id}", r)

if facture_id:
    r = requests.delete(f"{BASE_URL}/api/factures/{facture_id}")
    print_result("DELETE", f"/api/factures/{facture_id}", r)

if devis_id:
    r = requests.delete(f"{BASE_URL}/api/devis/{devis_id}")
    print_result("DELETE", f"/api/devis/{devis_id}", r)

if journal_id:
    r = requests.delete(f"{BASE_URL}/api/journals/{journal_id}")
    print_result("DELETE", f"/api/journals/{journal_id}", r)

if tax_id:
    r = requests.delete(f"{BASE_URL}/api/taxes/{tax_id}")
    print_result("DELETE", f"/api/taxes/{tax_id}", r)
