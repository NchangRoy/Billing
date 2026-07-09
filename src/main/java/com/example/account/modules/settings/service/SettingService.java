package com.example.account.modules.settings.service;

import com.example.account.modules.facturation.model.enums.TypeNumerotation;
import com.example.account.modules.settings.domain.Setting;
import com.example.account.modules.settings.dto.SettingResponse;
import com.example.account.modules.settings.dto.UpdateSequenceSettingRequest;
import com.example.account.modules.settings.repository.SettingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
@Slf4j
public class SettingService {

    private final SettingRepository settingRepository;

    /** The exact set of sales/purchasing documents shown in the sidebar — matches "Sales
     * Management" (Quotations, Proforma Invoice, Sales Orders, Invoices, Delivery Note,
     * Credit Notes, Back Orders) and "Purchasing & Logistics" (Purchase Order, Goods Receipt
     * Note, Supplier Invoice). Master-data entities (CLIENT, FOURNISSEUR, PRODUIT, ...) are not
     * numbered documents and are excluded. */
    private static final Set<TypeNumerotation> DOCUMENT_TYPES = EnumSet.of(
            TypeNumerotation.DEVIS,
            TypeNumerotation.PROFORMA,
            TypeNumerotation.SALES_ORDER,
            TypeNumerotation.FACTURE,
            TypeNumerotation.BON_LIVRAISON,
            TypeNumerotation.AVOIR,
            TypeNumerotation.BACK_ORDER,
            TypeNumerotation.PURCHASE_ORDER,
            TypeNumerotation.GOODS_RECEIPT,
            TypeNumerotation.SUPPLIER_INVOICE
    );

    private static final Map<TypeNumerotation, String> DOC_TYPE_CODES = new EnumMap<>(TypeNumerotation.class);
    static {
        DOC_TYPE_CODES.put(TypeNumerotation.DEVIS, "QUO");
        DOC_TYPE_CODES.put(TypeNumerotation.PROFORMA, "PRO");
        DOC_TYPE_CODES.put(TypeNumerotation.SALES_ORDER, "SOR");
        DOC_TYPE_CODES.put(TypeNumerotation.FACTURE, "INV");
        DOC_TYPE_CODES.put(TypeNumerotation.BON_LIVRAISON, "DLV");
        DOC_TYPE_CODES.put(TypeNumerotation.AVOIR, "CRN");
        DOC_TYPE_CODES.put(TypeNumerotation.BACK_ORDER, "BKO");
        DOC_TYPE_CODES.put(TypeNumerotation.PURCHASE_ORDER, "PUR");
        DOC_TYPE_CODES.put(TypeNumerotation.GOODS_RECEIPT, "GRN");
        DOC_TYPE_CODES.put(TypeNumerotation.SUPPLIER_INVOICE, "SIN");
    }

    // === Sequence settings (one row per document type) ===

    public Flux<SettingResponse> listSequenceSettings(UUID organizationId) {
        return Flux.fromIterable(DOCUMENT_TYPES)
                .flatMap(type -> getOrCreateSequenceSetting(organizationId, type))
                .map(this::toResponse);
    }

    public Mono<SettingResponse> updateSequenceSetting(UUID organizationId, TypeNumerotation type, UpdateSequenceSettingRequest request) {
        return getOrCreateSequenceSetting(organizationId, type)
                .flatMap(setting -> {
                    if (request.getIncludeOrgCode() != null) setting.setIncludeOrgCode(request.getIncludeOrgCode());
                    if (request.getOrgCode() != null) setting.setOrgCode(request.getOrgCode());
                    if (request.getIncludeBranchCode() != null) setting.setIncludeBranchCode(request.getIncludeBranchCode());
                    if (request.getBranchCode() != null) setting.setBranchCode(request.getBranchCode());
                    if (request.getIncludeTva() != null) setting.setIncludeTva(request.getIncludeTva());
                    if (request.getIncludeDate() != null) setting.setIncludeDate(request.getIncludeDate());
                    if (request.getRandomSeq4() != null) setting.setRandomSeq4(request.getRandomSeq4());
                    setting.setUpdatedAt(LocalDateTime.now());
                    return settingRepository.save(setting);
                })
                .map(this::toResponse);
    }

    private Mono<Setting> getOrCreateSequenceSetting(UUID organizationId, TypeNumerotation type) {
        return settingRepository.findByOrganizationIdAndTypeNumerotation(organizationId, type)
                .switchIfEmpty(Mono.defer(() -> {
                    LocalDateTime now = LocalDateTime.now();
                    Setting defaults = Setting.builder()
                            .id(UUID.randomUUID())
                            .organizationId(organizationId)
                            .typeNumerotation(type)
                            .includeOrgCode(false)
                            .includeBranchCode(false)
                            .includeTva(false)
                            .includeDate(true)
                            .randomSeq4(true)
                            .createdAt(now)
                            .updatedAt(now)
                            .build();
                    return settingRepository.save(defaults);
                }));
    }

    // === Organization-wide settings (logo) ===

    public Mono<SettingResponse> getOrganizationSettings(UUID organizationId) {
        return getOrCreateLogoSetting(organizationId).map(this::toResponse);
    }

    public Mono<SettingResponse> updateLogo(UUID organizationId, String uri) {
        return getOrCreateLogoSetting(organizationId)
                .flatMap(setting -> {
                    setting.setUri(uri);
                    setting.setUpdatedAt(LocalDateTime.now());
                    return settingRepository.save(setting);
                })
                .map(this::toResponse);
    }

    private Mono<Setting> getOrCreateLogoSetting(UUID organizationId) {
        return settingRepository.findByOrganizationIdAndTypeNumerotationIsNull(organizationId)
                .switchIfEmpty(Mono.defer(() -> {
                    LocalDateTime now = LocalDateTime.now();
                    Setting defaults = Setting.builder()
                            .id(UUID.randomUUID())
                            .organizationId(organizationId)
                            .typeNumerotation(null)
                            .createdAt(now)
                            .updatedAt(now)
                            .build();
                    return settingRepository.save(defaults);
                }));
    }

    // === Number generation ===

    /**
     * Composes a document number from the org's configured segments for that type, e.g.
     * "EDB-QUO-NT-20260307-0080" (orgCode-docTypeCode-tva-date-randomSeq).
     */
    public Mono<String> generateNumber(UUID organizationId, TypeNumerotation type, boolean hasTva) {
        return getOrCreateSequenceSetting(organizationId, type)
                .map(setting -> composeNumber(setting, type, hasTva));
    }

    private String composeNumber(Setting setting, TypeNumerotation type, boolean hasTva) {
        StringBuilder sb = new StringBuilder();
        if (Boolean.TRUE.equals(setting.getIncludeOrgCode()) && setting.getOrgCode() != null && !setting.getOrgCode().isBlank()) {
            appendSegment(sb, setting.getOrgCode().toUpperCase());
        }
        appendSegment(sb, DOC_TYPE_CODES.getOrDefault(type, "DOC"));
        if (Boolean.TRUE.equals(setting.getIncludeBranchCode()) && setting.getBranchCode() != null && !setting.getBranchCode().isBlank()) {
            appendSegment(sb, setting.getBranchCode().toUpperCase());
        }
        if (Boolean.TRUE.equals(setting.getIncludeTva())) {
            appendSegment(sb, hasTva ? "T" : "NT");
        }
        if (Boolean.TRUE.equals(setting.getIncludeDate())) {
            appendSegment(sb, LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")));
        }
        if (Boolean.TRUE.equals(setting.getRandomSeq4())) {
            appendSegment(sb, String.format("%04d", ThreadLocalRandom.current().nextInt(0, 10000)));
        }
        return sb.toString();
    }

    private void appendSegment(StringBuilder sb, String segment) {
        if (sb.length() > 0) sb.append('-');
        sb.append(segment);
    }

    private SettingResponse toResponse(Setting s) {
        String preview = s.getTypeNumerotation() != null ? composeNumber(s, s.getTypeNumerotation(), false) : null;
        return SettingResponse.builder()
                .id(s.getId())
                .organizationId(s.getOrganizationId())
                .typeNumerotation(s.getTypeNumerotation())
                .uri(s.getUri())
                .includeOrgCode(s.getIncludeOrgCode())
                .orgCode(s.getOrgCode())
                .includeBranchCode(s.getIncludeBranchCode())
                .branchCode(s.getBranchCode())
                .includeTva(s.getIncludeTva())
                .includeDate(s.getIncludeDate())
                .randomSeq4(s.getRandomSeq4())
                .preview(preview)
                .build();
    }
}
