package com.example.account.modules.facturation.service;

import reactor.core.publisher.Mono;

import com.example.account.modules.facturation.dto.response.TableauDeBordResponse;
import com.example.account.modules.facturation.model.enums.StatutFacture;
import com.example.account.modules.facturation.domain.port.output.DevisRepositoryPort;
import com.example.account.modules.facturation.domain.port.output.FactureRepositoryPort;
import com.example.account.modules.tiers.domain.port.output.ClientRepositoryPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;

@Service
@RequiredArgsConstructor
@Slf4j
public class TableauDeBordService {

    private final FactureRepositoryPort factureRepository;
    private final DevisRepositoryPort devisRepository;
    private final ClientRepositoryPort clientRepository;

    @Transactional(readOnly = true)
    public Mono<TableauDeBordResponse> getTableauDeBord() {
        log.info("Calcul des indicateurs du tableau de bord");

        LocalDate now = LocalDate.now();
        LocalDate debutMois = now.withDayOfMonth(1);
        LocalDate debutAnnee = now.withDayOfYear(1);

        return Mono.zip(
                factureRepository.sumMontantByDateBetween(debutMois, now).defaultIfEmpty(BigDecimal.ZERO),
                factureRepository.sumMontantByDateBetween(debutAnnee, now).defaultIfEmpty(BigDecimal.ZERO),
                factureRepository.count(),
                factureRepository.countByEtat(StatutFacture.PAYE),
                factureRepository.countByEtat(StatutFacture.EN_ATTENTE),
                clientRepository.count()
        ).map(tuple -> TableauDeBordResponse.builder()
                .chiffreAffairesMois(tuple.getT1())
                .chiffreAffairesAnnee(tuple.getT2())
                .nombreFacturesEmises(tuple.getT3())
                .nombreFacturesPayees(tuple.getT4())
                .nombreFacturesEnAttente(tuple.getT5())
                .nombreClients(tuple.getT6())
                .topProduits(new ArrayList<>())
                .topClients(new ArrayList<>())
                .evolutionCA12Mois(new ArrayList<>())
                .dateGeneration(now)
                .build());
    }
}
