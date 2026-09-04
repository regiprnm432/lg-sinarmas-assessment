import React from 'react';
import { CheckCircle2, AlertCircle, HelpCircle } from 'lucide-react';

export default function StatusBadge({ flag }) {
  if (flag === 'WITHIN_BUDGET') {
    return (
      <span className="inline-flex items-center gap-1.5 px-2.5 py-1 rounded-full text-xs font-semibold bg-emerald-50 text-emerald-700 border border-emerald-200">
        <CheckCircle2 className="w-3.5 h-3.5" />
        WITHIN BUDGET
      </span>
    );
  }

  if (flag === 'OVER_BUDGET') {
    return (
      <span className="inline-flex items-center gap-1.5 px-2.5 py-1 rounded-full text-xs font-semibold bg-rose-50 text-rose-700 border border-rose-200">
        <AlertCircle className="w-3.5 h-3.5" />
        OVER BUDGET
      </span>
    );
  }

  return (
    <span className="inline-flex items-center gap-1.5 px-2.5 py-1 rounded-full text-xs font-semibold bg-amber-50 text-amber-700 border border-amber-200">
      <HelpCircle className="w-3.5 h-3.5" />
      UNKNOWN
    </span>
  );
}
