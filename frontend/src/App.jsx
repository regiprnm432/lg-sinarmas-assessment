import React, { useState, useEffect, useCallback } from 'react';
import Header from './components/Header';
import QuoteStats from './components/QuoteStats';
import QuoteFilters from './components/QuoteFilters';
import QuoteTable from './components/QuoteTable';
import QuoteModal from './components/QuoteModal';
import FxRateModal from './components/FxRateModal';
import DeleteConfirmModal from './components/DeleteConfirmModal';
import { fetchQuotes, createQuote, updateQuote, deleteQuote, recalculateQuotes } from './services/quoteApi';
import { CheckCircle, AlertCircle, Info } from 'lucide-react';

export default function App() {
  const [quotes, setQuotes] = useState([]);
  const [loading, setLoading] = useState(true);
  const [filters, setFilters] = useState({ supplier: '', currency: '', budgetFlag: '', itemCode: '' });
  const [modalOpen, setModalOpen] = useState(false);
  const [editingQuote, setEditingQuote] = useState(null);
  const [deleteTarget, setDeleteTarget] = useState(null);
  const [deleting, setDeleting] = useState(false);
  const [ratesModalOpen, setRatesModalOpen] = useState(false);
  const [recalculating, setRecalculating] = useState(false);
  const [toast, setToast] = useState(null);

  const showToast = (message, type = 'success') => {
    setToast({ message, type });
    setTimeout(() => setToast(null), 4000);
  };

  const loadQuotes = useCallback(async () => {
    try {
      setLoading(true);
      const data = await fetchQuotes(filters);
      setQuotes(data);
    } catch (err) {
      showToast(err.message || 'Error connecting to Quote Service', 'error');
    } finally {
      setLoading(false);
    }
  }, [filters]);

  useEffect(() => {
    loadQuotes();
  }, [loadQuotes]);

  const handleSaveQuote = async (payload) => {
    if (editingQuote) {
      await updateQuote(editingQuote.id, payload);
      showToast(`Quote from "${payload.supplierName}" updated successfully!`);
    } else {
      await createQuote(payload);
      showToast(`Quote for item "${payload.itemCode}" added and converted!`);
    }
    await loadQuotes();
  };

  const handleConfirmDelete = async () => {
    if (!deleteTarget) return;
    setDeleting(true);
    try {
      await deleteQuote(deleteTarget.id);
      showToast(`Quote from "${deleteTarget.supplierName}" deleted successfully.`);
      setDeleteTarget(null);
      await loadQuotes();
    } catch (err) {
      showToast(err.message || 'Failed to delete quote', 'error');
    } finally {
      setDeleting(false);
    }
  };

  const handleRecalculate = async () => {
    setRecalculating(true);
    try {
      const updated = await recalculateQuotes();
      setQuotes(updated);
      showToast('All quotes re-converted using latest live FX rates.');
    } catch (err) {
      showToast(err.message || 'Recalculation failed', 'error');
    } finally {
      setRecalculating(false);
    }
  };

  return (
    <div className="min-h-screen bg-slate-50 flex flex-col font-['Inter',sans-serif]">
      {/* Toast Notification */}
      {toast && (
        <div className="fixed bottom-5 right-5 z-50 animate-in slide-in-from-bottom-5">
          <div className={`flex items-center gap-2.5 px-4 py-3 rounded-xl shadow-lg text-sm font-medium border ${
            toast.type === 'error'
              ? 'bg-rose-50 border-rose-200 text-rose-800'
              : 'bg-emerald-50 border-emerald-200 text-emerald-800'
          }`}>
            {toast.type === 'error' ? <AlertCircle className="w-4 h-4" /> : <CheckCircle className="w-4 h-4" />}
            <span>{toast.message}</span>
          </div>
        </div>
      )}

      {/* Navigation Header */}
      <Header
        onNewQuote={() => { setEditingQuote(null); setModalOpen(true); }}
        onRecalculate={handleRecalculate}
        onViewRates={() => setRatesModalOpen(true)}
        recalculating={recalculating}
      />

      {/* Main Container */}
      <main className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8 w-full flex-1">
        {/* Architecture & Formula Reminder Info Callout */}
        <div className="mb-6 p-4 rounded-xl bg-blue-50/60 border border-blue-100 flex items-start gap-3 text-xs text-blue-900">
          <Info className="w-4 h-4 text-blue-600 flex-shrink-0 mt-0.5" />
          <div className="leading-relaxed">
            <span className="font-bold">Automated Procurement Rule: </span>
            <code className="bg-blue-100/70 px-1 py-0.5 rounded text-blue-800 font-mono">
              converted_amount = quote_amount × rate(quote_currency → USD)
            </code>.
            Quotes exceeding the budget limit are flagged as <span className="font-semibold text-rose-700">OVER_BUDGET</span>, otherwise <span className="font-semibold text-emerald-700">WITHIN_BUDGET</span>.
            Exchange rates are fetched via Frankfurter API and cached in database table <code className="bg-blue-100/70 px-1 py-0.5 rounded text-blue-800 font-mono">fx_rate_cache</code> with automatic offline resilience.
          </div>
        </div>

        {/* KPI Summary Cards */}
        <QuoteStats quotes={quotes} />

        {/* Filter Toolbar */}
        <QuoteFilters
          filters={filters}
          setFilters={setFilters}
          onReset={() => setFilters({ supplier: '', currency: '', budgetFlag: '', itemCode: '' })}
        />

        {/* Quotes Data Table */}
        <QuoteTable
          quotes={quotes}
          loading={loading}
          onEdit={(q) => { setEditingQuote(q); setModalOpen(true); }}
          onDelete={(quote) => setDeleteTarget(quote)}
        />
      </main>

      {/* Create / Edit Modal */}
      <QuoteModal
        isOpen={modalOpen}
        onClose={() => setModalOpen(false)}
        onSave={handleSaveQuote}
        editingQuote={editingQuote}
      />

      {/* Delete Confirmation Modal */}
      <DeleteConfirmModal
        isOpen={!!deleteTarget}
        onClose={() => setDeleteTarget(null)}
        onConfirm={handleConfirmDelete}
        quote={deleteTarget}
        deleting={deleting}
      />

      {/* FX Rate Cache Inspector Modal */}
      <FxRateModal
        isOpen={ratesModalOpen}
        onClose={() => setRatesModalOpen(false)}
      />

      {/* Footer */}
      <footer className="border-t border-slate-200 bg-white py-4 text-center text-xs text-slate-400">
        Procurement FX System — Case Study A-002 • Java 17 + Spring Boot 3 + React + H2/PostgreSQL
      </footer>
    </div>
  );
}
