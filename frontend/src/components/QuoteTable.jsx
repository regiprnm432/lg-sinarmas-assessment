import React, { useState, useEffect } from 'react';
import StatusBadge from './StatusBadge';
import { Award, Edit3, Trash2, Clock, DollarSign, ChevronLeft, ChevronRight } from 'lucide-react';

export default function QuoteTable({ quotes, loading, onEdit, onDelete }) {
  const [currentPage, setCurrentPage] = useState(1);
  const [pageSize, setPageSize] = useState(10);

  useEffect(() => {
    setCurrentPage(1);
  }, [quotes.length, pageSize]);

  const totalPages = Math.max(1, Math.ceil(quotes.length / pageSize));
  const startIndex = (currentPage - 1) * pageSize;
  const endIndex = Math.min(startIndex + pageSize, quotes.length);
  const paginatedQuotes = quotes.slice(startIndex, endIndex);

  const getPageNumbers = () => {
    const pages = [];
    if (totalPages <= 5) {
      for (let i = 1; i <= totalPages; i++) pages.push(i);
    } else {
      if (currentPage <= 3) {
        pages.push(1, 2, 3, 4, '...', totalPages);
      } else if (currentPage >= totalPages - 2) {
        pages.push(1, '...', totalPages - 3, totalPages - 2, totalPages - 1, totalPages);
      } else {
        pages.push(1, '...', currentPage - 1, currentPage, currentPage + 1, '...', totalPages);
      }
    }
    return pages;
  };

  const formatNumber = (val, decimals = 2) => {
    if (val === null || val === undefined) return '-';
    return Number(val).toLocaleString(undefined, {
      minimumFractionDigits: decimals,
      maximumFractionDigits: decimals,
    });
  };

  const formatDate = (isoString) => {
    if (!isoString) return null;
    try {
      const d = new Date(isoString);
      return d.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }) + ', ' + d.toLocaleDateString();
    } catch {
      return isoString;
    }
  };

  if (loading) {
    return (
      <div className="bg-white rounded-xl border border-slate-200 p-12 text-center shadow-sm">
        <div className="inline-block animate-spin rounded-full h-8 w-8 border-4 border-blue-500 border-t-transparent mb-3"></div>
        <p className="text-slate-500 font-medium">Loading purchase quotes...</p>
      </div>
    );
  }

  if (quotes.length === 0) {
    return (
      <div className="bg-white rounded-xl border border-slate-200 p-12 text-center shadow-sm">
        <div className="w-14 h-14 bg-slate-100 text-slate-400 rounded-full flex items-center justify-center mx-auto mb-3">
          <DollarSign className="w-7 h-7" />
        </div>
        <h4 className="text-base font-semibold text-slate-800">No quotes found</h4>
        <p className="text-sm text-slate-500 mt-1 max-w-sm mx-auto">
          No supplier quotes matched your current filter criteria or none have been submitted yet.
        </p>
      </div>
    );
  }

  return (
    <div className="bg-white rounded-xl border border-slate-200 shadow-sm overflow-hidden">
      <div className="overflow-x-auto">
        <table className="w-full text-left border-collapse">
          <thead>
            <tr className="bg-slate-50/80 border-b border-slate-200 text-xs font-semibold text-slate-500 uppercase tracking-wider">
              <th className="py-3.5 px-4">Item Code</th>
              <th className="py-3.5 px-4">Supplier</th>
              <th className="py-3.5 px-4 text-right">Original Quote</th>
              <th className="py-3.5 px-4 text-right">Converted (USD)</th>
              <th className="py-3.5 px-4 text-right">Budget Limit</th>
              <th className="py-3.5 px-4 text-center">Status</th>
              <th className="py-3.5 px-4">FX Rate Info</th>
              <th className="py-3.5 px-4 text-right">Actions</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-slate-100 text-sm">
            {paginatedQuotes.map((quote) => {
              const variance = (quote.convertedAmount !== null && quote.budgetAmount !== null)
                ? (quote.convertedAmount - quote.budgetAmount)
                : null;

              return (
                <tr
                  key={quote.id}
                  className={`hover:bg-slate-50/70 transition-colors ${
                    quote.cheapest ? 'bg-emerald-50/20' : ''
                  }`}
                >
                  {/* Item Code & Cheapest Badge */}
                  <td className="py-4 px-4 font-semibold text-slate-800">
                    <div className="flex items-center gap-1.5 flex-wrap">
                      <span>{quote.itemCode}</span>
                      {quote.cheapest && (
                        <span className="inline-flex items-center gap-1 px-2 py-0.5 rounded-md text-[11px] font-bold bg-amber-100 text-amber-800 border border-amber-300 shadow-xs" title="Lowest quote in base currency for this item">
                          <Award className="w-3 h-3 text-amber-600" />
                          BEST VALUE
                        </span>
                      )}
                    </div>
                  </td>

                  {/* Supplier */}
                  <td className="py-4 px-4 font-medium text-slate-700">
                    {quote.supplierName}
                  </td>

                  {/* Original Quote */}
                  <td className="py-4 px-4 text-right font-medium text-slate-900 font-mono">
                    {formatNumber(quote.quoteAmount)} <span className="text-xs font-sans text-slate-500 font-semibold">{quote.quoteCurrency}</span>
                  </td>

                  {/* Converted Amount */}
                  <td className="py-4 px-4 text-right font-bold text-slate-900 font-mono">
                    {quote.convertedAmount !== null ? (
                      <span>${formatNumber(quote.convertedAmount)} <span className="text-xs font-sans text-slate-500 font-normal">USD</span></span>
                    ) : (
                      <span className="text-slate-400 font-sans italic font-normal">Rate unavailable</span>
                    )}
                  </td>

                  {/* Budget Limit */}
                  <td className="py-4 px-4 text-right text-slate-600 font-mono">
                    ${formatNumber(quote.budgetAmount)}
                  </td>

                  {/* Status Flag */}
                  <td className="py-4 px-4 text-center whitespace-nowrap">
                    <StatusBadge flag={quote.budgetFlag} />
                    {variance !== null && (
                      <p className={`text-[11px] font-mono mt-0.5 ${variance > 0 ? 'text-rose-600' : 'text-emerald-600'}`}>
                        {variance > 0 ? `+$${formatNumber(variance)}` : `-$${formatNumber(Math.abs(variance))}`}
                      </p>
                    )}
                  </td>

                  {/* FX Rate Info */}
                  <td className="py-4 px-4 text-xs text-slate-500">
                    {quote.rateUsed ? (
                      <div>
                        <div className="font-mono text-slate-700">
                          1 {quote.quoteCurrency} = {formatNumber(quote.rateUsed, 6)} USD
                        </div>
                        {quote.rateFetchedAt && (
                          <div className="flex items-center gap-1 text-[10px] text-slate-400 mt-0.5">
                            <Clock className="w-2.5 h-2.5" />
                            {formatDate(quote.rateFetchedAt)}
                          </div>
                        )}
                      </div>
                    ) : (
                      <span className="text-amber-600 text-xs italic">Live rate pending</span>
                    )}
                  </td>

                  {/* Actions */}
                  <td className="py-4 px-4 text-right whitespace-nowrap">
                    <div className="flex items-center justify-end gap-1.5">
                      <button
                        onClick={() => onEdit(quote)}
                        className="p-1.5 text-slate-500 hover:text-blue-600 hover:bg-blue-50 rounded-lg transition-colors"
                        title="Edit quote"
                      >
                        <Edit3 className="w-4 h-4" />
                      </button>
                      <button
                        onClick={() => onDelete(quote)}
                        className="p-1.5 text-slate-500 hover:text-rose-600 hover:bg-rose-50 rounded-lg transition-colors"
                        title="Delete quote"
                      >
                        <Trash2 className="w-4 h-4" />
                      </button>
                    </div>
                  </td>
                </tr>
              );
            })}
          </tbody>
        </table>
      </div>

      {/* Pagination Toolbar */}
      <div className="px-4 py-3 bg-slate-50/60 border-t border-slate-200 flex flex-col sm:flex-row items-center justify-between gap-3 text-xs text-slate-600">
        <div className="flex items-center gap-4 flex-wrap">
          <span>
            Showing <span className="font-semibold text-slate-800">{startIndex + 1}</span> to{' '}
            <span className="font-semibold text-slate-800">{endIndex}</span> of{' '}
            <span className="font-semibold text-slate-800">{quotes.length}</span> quotes
          </span>

          <div className="flex items-center gap-1.5">
            <span className="text-slate-500">Rows per page:</span>
            <select
              value={pageSize}
              onChange={(e) => setPageSize(Number(e.target.value))}
              className="px-2 py-1 bg-white border border-slate-200 rounded-md text-slate-700 font-medium focus:outline-none focus:ring-1 focus:ring-blue-500"
            >
              <option value={10}>10</option>
              <option value={25}>25</option>
              <option value={50}>50</option>
              <option value={100}>100</option>
            </select>
          </div>
        </div>

        {/* Page Navigation */}
        <div className="flex items-center gap-1">
          <button
            onClick={() => setCurrentPage(prev => Math.max(1, prev - 1))}
            disabled={currentPage === 1}
            className="flex items-center gap-1 px-2.5 py-1.5 rounded-lg border border-slate-200 bg-white hover:bg-slate-100 disabled:opacity-40 disabled:cursor-not-allowed transition-colors text-slate-700"
            title="Previous Page"
          >
            <ChevronLeft className="w-3.5 h-3.5" />
            <span>Prev</span>
          </button>

          <div className="flex items-center gap-1">
            {getPageNumbers().map((p, idx) => (
              p === '...' ? (
                <span key={`dots-${idx}`} className="px-1 text-slate-400">...</span>
              ) : (
                <button
                  key={p}
                  onClick={() => setCurrentPage(p)}
                  className={`w-7 h-7 rounded-lg font-medium text-xs transition-colors ${
                    currentPage === p
                      ? 'bg-blue-600 text-white shadow-2xs font-bold'
                      : 'border border-slate-200 bg-white hover:bg-slate-100 text-slate-700'
                  }`}
                >
                  {p}
                </button>
              )
            ))}
          </div>

          <button
            onClick={() => setCurrentPage(prev => Math.min(totalPages, prev + 1))}
            disabled={currentPage === totalPages}
            className="flex items-center gap-1 px-2.5 py-1.5 rounded-lg border border-slate-200 bg-white hover:bg-slate-100 disabled:opacity-40 disabled:cursor-not-allowed transition-colors text-slate-700"
            title="Next Page"
          >
            <span>Next</span>
            <ChevronRight className="w-3.5 h-3.5" />
          </button>
        </div>
      </div>
    </div>
  );
}
