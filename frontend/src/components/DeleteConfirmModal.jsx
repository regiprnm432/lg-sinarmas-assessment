import React from 'react';
import { X, AlertTriangle, Trash2 } from 'lucide-react';

export default function DeleteConfirmModal({ isOpen, onClose, onConfirm, quote, deleting }) {
  if (!isOpen || !quote) return null;

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-slate-900/40 backdrop-blur-xs animate-in fade-in duration-150">
      <div className="bg-white rounded-2xl shadow-xl border border-slate-200 w-full max-w-md overflow-hidden animate-in zoom-in-95 duration-150">
        {/* Modal Header */}
        <div className="flex items-center justify-between px-6 py-4 border-b border-slate-100 bg-rose-50/50">
          <div className="flex items-center gap-2.5">
            <div className="w-8 h-8 rounded-full bg-rose-100 text-rose-600 flex items-center justify-center">
              <AlertTriangle className="w-4 h-4" />
            </div>
            <h3 className="font-bold text-slate-800 text-base">
              Delete Purchase Quote
            </h3>
          </div>
          <button
            onClick={onClose}
            disabled={deleting}
            className="text-slate-400 hover:text-slate-600 rounded-lg p-1 transition-colors disabled:opacity-50"
          >
            <X className="w-5 h-5" />
          </button>
        </div>

        {/* Modal Body */}
        <div className="p-6">
          <p className="text-sm text-slate-600 mb-4">
            Are you sure you want to delete this quote? This action cannot be undone.
          </p>

          {/* Quote Details Preview */}
          <div className="bg-slate-50 border border-slate-200 rounded-xl p-3.5 space-y-2 text-xs">
            <div className="flex justify-between items-center">
              <span className="text-slate-500 font-medium">Supplier:</span>
              <span className="font-semibold text-slate-800">{quote.supplierName}</span>
            </div>
            <div className="flex justify-between items-center">
              <span className="text-slate-500 font-medium">Item Code:</span>
              <span className="font-semibold font-mono text-slate-800">{quote.itemCode}</span>
            </div>
            <div className="flex justify-between items-center">
              <span className="text-slate-500 font-medium">Original Quote:</span>
              <span className="font-mono text-slate-800">
                {Number(quote.quoteAmount).toLocaleString()} {quote.quoteCurrency}
              </span>
            </div>
            {quote.convertedAmount !== null && (
              <div className="flex justify-between items-center pt-1 border-t border-slate-200">
                <span className="text-slate-500 font-medium">Converted (USD):</span>
                <span className="font-bold font-mono text-slate-900">
                  ${Number(quote.convertedAmount).toFixed(2)} USD
                </span>
              </div>
            )}
          </div>
        </div>

        {/* Modal Actions */}
        <div className="px-6 py-3.5 bg-slate-50 border-t border-slate-100 flex items-center justify-end gap-2.5">
          <button
            type="button"
            onClick={onClose}
            disabled={deleting}
            className="px-4 py-2 text-xs font-semibold text-slate-600 hover:text-slate-800 hover:bg-slate-200 rounded-lg transition-colors disabled:opacity-50"
          >
            Cancel
          </button>
          <button
            type="button"
            onClick={onConfirm}
            disabled={deleting}
            className="flex items-center gap-1.5 px-4 py-2 text-xs font-semibold text-white bg-rose-600 hover:bg-rose-700 rounded-lg shadow-sm transition-colors disabled:opacity-50"
          >
            <Trash2 className="w-3.5 h-3.5" />
            <span>{deleting ? 'Deleting...' : 'Delete Quote'}</span>
          </button>
        </div>
      </div>
    </div>
  );
}
