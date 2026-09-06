import React from 'react';
import { Search, RotateCcw, Download, Loader2 } from 'lucide-react';

export default function QuoteFilters({ filters, setFilters, onReset, onExport, exporting }) {
  const currencies = ['USD', 'KRW', 'EUR', 'IDR', 'JPY', 'GBP', 'CNY', 'SGD'];

  return (
    <div className="bg-white p-4 rounded-xl border border-slate-200 shadow-sm mb-6">
      <div className="flex flex-col md:flex-row gap-3 items-center justify-between">
        <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-4 gap-3 w-full">
          {/* Supplier Search */}
          <div className="relative">
            <Search className="w-4 h-4 text-slate-400 absolute left-3 top-1/2 -translate-y-1/2" />
            <input
              type="text"
              placeholder="Filter by supplier..."
              value={filters.supplier || ''}
              onChange={(e) => setFilters(prev => ({ ...prev, supplier: e.target.value }))}
              className="w-full pl-9 pr-3 py-2 text-sm border border-slate-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent"
            />
          </div>

          {/* Item Code Search */}
          <div className="relative">
            <Search className="w-4 h-4 text-slate-400 absolute left-3 top-1/2 -translate-y-1/2" />
            <input
              type="text"
              placeholder="Filter by item code..."
              value={filters.itemCode || ''}
              onChange={(e) => setFilters(prev => ({ ...prev, itemCode: e.target.value }))}
              className="w-full pl-9 pr-3 py-2 text-sm border border-slate-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent"
            />
          </div>

          {/* Currency Filter */}
          <div>
            <select
              value={filters.currency || ''}
              onChange={(e) => setFilters(prev => ({ ...prev, currency: e.target.value }))}
              className="w-full px-3 py-2 text-sm border border-slate-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 bg-white"
            >
              <option value="">All Currencies</option>
              {currencies.map(c => (
                <option key={c} value={c}>{c}</option>
              ))}
            </select>
          </div>

          {/* Budget Flag Filter */}
          <div>
            <select
              value={filters.budgetFlag || ''}
              onChange={(e) => setFilters(prev => ({ ...prev, budgetFlag: e.target.value }))}
              className="w-full px-3 py-2 text-sm border border-slate-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 bg-white"
            >
              <option value="">All Budget Statuses</option>
              <option value="WITHIN_BUDGET">Within Budget</option>
              <option value="OVER_BUDGET">Over Budget</option>
              <option value="UNKNOWN">Unknown</option>
            </select>
          </div>
        </div>

        {/* Action Buttons (Reset & Export) */}
        <div className="flex items-center gap-2 self-stretch md:self-auto justify-end">
          {(filters.supplier || filters.itemCode || filters.currency || filters.budgetFlag) && (
            <button
              onClick={onReset}
              className="flex items-center gap-1.5 px-3 py-2 text-sm text-slate-600 hover:text-slate-900 bg-slate-100 hover:bg-slate-200 rounded-lg transition-colors whitespace-nowrap"
              title="Reset filters"
            >
              <RotateCcw className="w-3.5 h-3.5" />
              Clear
            </button>
          )}

          <button
            onClick={onExport}
            disabled={exporting}
            className="flex items-center gap-1.5 px-3.5 py-2 text-sm font-medium text-emerald-700 hover:text-emerald-800 bg-emerald-50 hover:bg-emerald-100 border border-emerald-200 rounded-lg transition-colors shadow-2xs whitespace-nowrap disabled:opacity-50"
            title="Export quotes comparison to Excel (.xlsx)"
          >
            {exporting ? (
              <Loader2 className="w-4 h-4 animate-spin text-emerald-600" />
            ) : (
              <Download className="w-4 h-4 text-emerald-600" />
            )}
            <span>{exporting ? 'Exporting...' : 'Export Excel'}</span>
          </button>
        </div>
      </div>
    </div>
  );
}

