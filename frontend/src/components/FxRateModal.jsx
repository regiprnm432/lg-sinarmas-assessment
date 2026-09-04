import React, { useState, useEffect } from 'react';
import { X, Database, Clock, RefreshCw } from 'lucide-react';
import { fetchCachedRates } from '../services/quoteApi';

export default function FxRateModal({ isOpen, onClose }) {
  const [rates, setRates] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  useEffect(() => {
    if (isOpen) {
      loadRates();
    }
  }, [isOpen]);

  const loadRates = async () => {
    setLoading(true);
    setError(null);
    try {
      const data = await fetchCachedRates();
      setRates(data);
    } catch (err) {
      setError(err.message || 'Failed to load FX rate cache');
    } finally {
      setLoading(false);
    }
  };

  if (!isOpen) return null;

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-slate-900/40 backdrop-blur-xs animate-in fade-in duration-150">
      <div className="bg-white rounded-2xl shadow-xl border border-slate-200 w-full max-w-2xl overflow-hidden max-h-[85vh] flex flex-col">
        {/* Header */}
        <div className="flex items-center justify-between px-6 py-4 border-b border-slate-100 bg-slate-50/50">
          <div className="flex items-center gap-2">
            <Database className="w-5 h-5 text-blue-600" />
            <div>
              <h3 className="font-bold text-slate-800 text-base">Database FX Rate Cache (`fx_rate_cache`)</h3>
              <p className="text-xs text-slate-500">Live inspection of cached exchange rates</p>
            </div>
          </div>
          <div className="flex items-center gap-2">
            <button
              onClick={loadRates}
              className="p-1.5 text-slate-400 hover:text-slate-600 rounded-lg transition-colors"
              title="Refresh"
            >
              <RefreshCw className={`w-4 h-4 ${loading ? 'animate-spin' : ''}`} />
            </button>
            <button
              onClick={onClose}
              className="p-1.5 text-slate-400 hover:text-slate-600 rounded-lg transition-colors"
            >
              <X className="w-5 h-5" />
            </button>
          </div>
        </div>

        {/* Content */}
        <div className="p-6 overflow-y-auto flex-1">
          {error && (
            <div className="p-3 mb-4 rounded-lg bg-rose-50 border border-rose-200 text-rose-700 text-sm">
              {error}
            </div>
          )}

          {loading ? (
            <div className="text-center py-8">
              <div className="inline-block animate-spin rounded-full h-6 w-6 border-2 border-blue-500 border-t-transparent mb-2"></div>
              <p className="text-xs text-slate-500">Querying database cache...</p>
            </div>
          ) : rates.length === 0 ? (
            <div className="text-center py-8 text-slate-500 text-sm">
              No FX rates currently cached in database.
            </div>
          ) : (
            <table className="w-full text-left border-collapse text-xs">
              <thead>
                <tr className="bg-slate-50 border-b border-slate-200 text-slate-500 uppercase font-semibold">
                  <th className="py-2.5 px-3">Pair</th>
                  <th className="py-2.5 px-3 text-right">Rate</th>
                  <th className="py-2.5 px-3">Rate Date</th>
                  <th className="py-2.5 px-3">Fetched At</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-100 font-mono">
                {rates.map((rate) => (
                  <tr key={rate.id} className="hover:bg-slate-50/50">
                    <td className="py-2.5 px-3 font-semibold text-slate-800 font-sans">
                      {rate.baseCurrency} → {rate.targetCurrency}
                    </td>
                    <td className="py-2.5 px-3 text-right font-bold text-slate-900">
                      {Number(rate.rate).toFixed(6)}
                    </td>
                    <td className="py-2.5 px-3 text-slate-600">
                      {rate.rateDate}
                    </td>
                    <td className="py-2.5 px-3 text-slate-500 font-sans text-[11px]">
                      {rate.fetchedAt ? new Date(rate.fetchedAt).toLocaleString() : '-'}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}
        </div>

        {/* Footer */}
        <div className="px-6 py-3 border-t border-slate-100 bg-slate-50 flex justify-end">
          <button
            onClick={onClose}
            className="px-4 py-2 text-xs font-semibold text-slate-700 bg-white border border-slate-200 hover:bg-slate-50 rounded-lg transition-colors"
          >
            Close
          </button>
        </div>
      </div>
    </div>
  );
}
