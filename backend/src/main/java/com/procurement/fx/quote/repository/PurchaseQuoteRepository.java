package com.procurement.fx.quote.repository;

import com.procurement.fx.quote.model.BudgetFlag;
import com.procurement.fx.quote.model.PurchaseQuote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PurchaseQuoteRepository extends JpaRepository<PurchaseQuote, Long> {

    List<PurchaseQuote> findAllByOrderByCreatedAtDesc();

    List<PurchaseQuote> findByItemCode(String itemCode);

    @Query("SELECT q FROM PurchaseQuote q WHERE " +
            "(:supplier IS NULL OR LOWER(q.supplierName) LIKE LOWER(CONCAT('%', :supplier, '%'))) AND " +
            "(:currency IS NULL OR UPPER(q.quoteCurrency) = UPPER(:currency)) AND " +
            "(:budgetFlag IS NULL OR q.budgetFlag = :budgetFlag) AND " +
            "(:itemCode IS NULL OR LOWER(q.itemCode) LIKE LOWER(CONCAT('%', :itemCode, '%'))) " +
            "ORDER BY q.createdAt DESC")
    List<PurchaseQuote> filterQuotes(
            @Param("supplier") String supplier,
            @Param("currency") String currency,
            @Param("budgetFlag") BudgetFlag budgetFlag,
            @Param("itemCode") String itemCode
    );
}
