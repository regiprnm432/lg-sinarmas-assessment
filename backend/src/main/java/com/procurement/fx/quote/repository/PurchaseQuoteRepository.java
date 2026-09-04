package com.procurement.fx.quote.repository;

import com.procurement.fx.quote.model.PurchaseQuote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PurchaseQuoteRepository extends JpaRepository<PurchaseQuote, Long>, JpaSpecificationExecutor<PurchaseQuote> {

    List<PurchaseQuote> findAllByOrderByCreatedAtDesc();

    List<PurchaseQuote> findByItemCode(String itemCode);
}
