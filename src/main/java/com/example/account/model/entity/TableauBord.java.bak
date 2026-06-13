package com.example.account.model.entity;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.MapKeyColumn;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table("tableaux_bord")
public class TableauBord {

    @Id
    @Column("id_tableau")
    private UUID idTableau;

    @NotBlank(message = "Le nom du tableau de bord est obligatoire")
    @Column("nom_tableau")
    private String nomTableau;

    @Column("description")
    private String description;

    @NotNull(message = "Le propriétaire est obligatoire")
    @Column("proprietaire")
    private UUID proprietaire;

    @Column("nom_proprietaire")
    private String nomProprietaire;

    @Column("type_tableau")
    private String typeTableau; // "EXECUTIF", "COMMERCIAL", "FINANCIER", "OPERATIONNEL"

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "tableau_bord_id")
    private List<Widget> widgets;

    @ElementCollection
    @CollectionTable(name = "tableau_bord_layout", joinColumns = @JoinColumn(name = "tableau_bord_id"))
    @MapKeyColumn(name = "layout_key")
    @Column(name = "layout_value")
    private Map<String, String> layoutConfiguration;

    @ElementCollection
    @CollectionTable(name = "tableau_bord_filtres", joinColumns = @JoinColumn(name = "tableau_bord_id"))
    @MapKeyColumn(name = "filtre_key")
    @Column(name = "filtre_value")
    private Map<String, String> filtresGlobaux;

    @Column(name = "periode_defaut")
    private String periodeDefaut; // "MOIS_COURANT", "TRIMESTRE", "ANNEE", etc.

    @Column("auto_actualisation")
    @Builder.Default
    private Boolean autoActualisation = true;

    @Column("frequence_actualisation_minutes")
    @Builder.Default
    private Integer frequenceActualisationMinutes = 15;

    @Column("partage_public")
    @Builder.Default
    private Boolean partagePublic = false;

    @ElementCollection
    @Column(name = "utilisateurs_autorises")
    private List<UUID> utilisateursAutorises;

    @ElementCollection
    @Column(name = "roles_autorises")
    private List<String> rolesAutorises;

    @Column("actif")
    @Builder.Default
    private Boolean actif = true;

    @Column("par_defaut")
    @Builder.Default
    private Boolean parDefaut = false;

    @Column("ordre_affichage")
    @Builder.Default
    private Integer ordreAffichage = 1;

    @Column("couleur_theme")
    private String couleurTheme;

    @Column("icone")
    private String icone;

    @ElementCollection
    @Column(name = "tags")
    private List<String> tags;

    @Column("derniere_consultation")
    private LocalDateTime derniereConsultation;

    @Column("nombre_consultations")
    @Builder.Default
    private Long nombreConsultations = 0L;

    @ElementCollection
    @Column(name = "favoris_utilisateurs")
    private List<UUID> favorisUtilisateurs;

    @Column("export_automatique")
    @Builder.Default
    private Boolean exportAutomatique = false;

    @Column("format_export")
    private String formatExport; // "PDF", "EXCEL", "EMAIL"

    @ElementCollection
    @Column(name = "destinataires_export")
    private List<String> destinatairesExport;

    @Column("frequence_export")
    private String frequenceExport; // "QUOTIDIEN", "HEBDOMADAIRE", "MENSUEL"

    @Column("derniere_actualisation")
    private LocalDateTime derniereActualisation;

    @ElementCollection
    @CollectionTable(name = "tableau_bord_cache", joinColumns = @JoinColumn(name = "tableau_bord_id"))
    @MapKeyColumn(name = "cache_key")
    @Column(name = "cache_value")
    private Map<String, String> cacheDonnees;

    @Column("duree_cache_minutes")
    @Builder.Default
    private Integer dureeCacheMinutes = 30;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "tableau_bord_id")
    private List<AlerteTableau> alertesConfigurees;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column("updated_at")
    private LocalDateTime updatedAt;
}