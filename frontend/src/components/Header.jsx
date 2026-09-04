import React from 'react';
import { Plus, RefreshCw, Layers, Database } from 'lucide-react';

export default function Header({ onNewQuote, onRecalculate, onViewRates, recalculating }) {
  return (
    <header className="bg-white border-b border-slate-200 sticky top-0 z-30 shadow-xs">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-4">
        <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
          <div>
            <div className="flex items-center gap-2.5">
              <div className="w-9 h-9 rounded-lg bg-blue-600 text-white flex items-center justify-center font-bold shadow-xs">
                <Layers className="w-5 h-5" />
              </div>
              <div>
                <div className="flex items-center gap-2">
                  <h1 className="text-xl font-bold text-slate-900 leading-tight">Procurement FX</h1>
                  <span className="px-2 py-0.5 text-[11px] font-semibold bg-slate-100 text-slate-700 rounded-full border border-slate-200">
                    Case A-002
                  </span>
                </div>
                <p className="text-xs text-slate-500">
                  Foreign Supplier Quote Comparison with Live ECB Exchange Rates
                </p>
              </div>
            </div>
          </div>

          {/* Action Buttons */}
          <div className="flex items-center gap-2.5">
            <button
              onClick={onViewRates}
              className="flex items-center gap-1.5 px-3 py-2 text-xs font-semibold text-slate-700 hover:text-slate-900 bg-slate-100 hover:bg-slate-200 rounded-lg transition-colors border border-slate-200"
              title="View Cached Rates in Database"
            >
              <Database className="w-3.5 h-3.5 text-slate-500" />
              <span>FX Cache</span>
            </button>

            <button
              onClick={onRecalculate}
              disabled={recalculating}
              className="flex items-center gap-1.5 px-3 py-2 text-xs font-semibold text-slate-700 hover:text-slate-900 bg-white hover:bg-slate-50 border border-slate-300 rounded-lg transition-colors shadow-2xs disabled:opacity-50"
              title="Fetch latest rates and re-convert quotes"
            >
              <RefreshCw className={`w-3.5 h-3.5 text-blue-600 ${recalculating ? 'animate-spin' : ''}`} />
              <span>{recalculating ? 'Updating...' : 'Sync Rates'}</span>
            </button>

            <button
              onClick={onNewQuote}
              className="flex items-center gap-1.5 px-3.5 py-2 text-xs font-semibold text-white bg-blue-600 hover:bg-blue-700 rounded-lg shadow-sm transition-colors"
            >
              <Plus className="w-4 h-4" />
              <span>Add Quote</span>
            </button>
          </div>
        </div>
      </div>
    </header>
  );
}
