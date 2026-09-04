import React from 'react';
import { Layers, CheckCircle, AlertTriangle, HelpCircle } from 'lucide-react';

export default function QuoteStats({ quotes }) {
  const total = quotes.length;
  const withinBudget = quotes.filter(q => q.budgetFlag === 'WITHIN_BUDGET').length;
  const overBudget = quotes.filter(q => q.budgetFlag === 'OVER_BUDGET').length;
  const unknown = quotes.filter(q => q.budgetFlag === 'UNKNOWN').length;

  const withinPct = total > 0 ? Math.round((withinBudget / total) * 100) : 0;
  const overPct = total > 0 ? Math.round((overBudget / total) * 100) : 0;

  return (
    <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4 mb-6">
      <div className="bg-white p-5 rounded-xl border border-slate-200 shadow-sm flex items-center justify-between">
        <div>
          <p className="text-xs font-medium text-slate-500 uppercase tracking-wider">Total Quotes</p>
          <h3 className="text-2xl font-bold text-slate-900 mt-1">{total}</h3>
          <p className="text-xs text-slate-400 mt-0.5">Tracked suppliers</p>
        </div>
        <div className="w-12 h-12 rounded-lg bg-blue-50 text-blue-600 flex items-center justify-center">
          <Layers className="w-6 h-6" />
        </div>
      </div>

      <div className="bg-white p-5 rounded-xl border border-slate-200 shadow-sm flex items-center justify-between">
        <div>
          <p className="text-xs font-medium text-slate-500 uppercase tracking-wider">Within Budget</p>
          <h3 className="text-2xl font-bold text-emerald-600 mt-1">{withinBudget}</h3>
          <p className="text-xs text-emerald-600/80 mt-0.5">{withinPct}% of total quotes</p>
        </div>
        <div className="w-12 h-12 rounded-lg bg-emerald-50 text-emerald-600 flex items-center justify-center">
          <CheckCircle className="w-6 h-6" />
        </div>
      </div>

      <div className="bg-white p-5 rounded-xl border border-slate-200 shadow-sm flex items-center justify-between">
        <div>
          <p className="text-xs font-medium text-slate-500 uppercase tracking-wider">Over Budget</p>
          <h3 className="text-2xl font-bold text-rose-600 mt-1">{overBudget}</h3>
          <p className="text-xs text-rose-600/80 mt-0.5">{overPct}% exceeding limit</p>
        </div>
        <div className="w-12 h-12 rounded-lg bg-rose-50 text-rose-600 flex items-center justify-center">
          <AlertTriangle className="w-6 h-6" />
        </div>
      </div>

      <div className="bg-white p-5 rounded-xl border border-slate-200 shadow-sm flex items-center justify-between">
        <div>
          <p className="text-xs font-medium text-slate-500 uppercase tracking-wider">Rate Pending / Offline</p>
          <h3 className="text-2xl font-bold text-amber-600 mt-1">{unknown}</h3>
          <p className="text-xs text-slate-400 mt-0.5">FX rate unavailable</p>
        </div>
        <div className="w-12 h-12 rounded-lg bg-amber-50 text-amber-600 flex items-center justify-center">
          <HelpCircle className="w-6 h-6" />
        </div>
      </div>
    </div>
  );
}
