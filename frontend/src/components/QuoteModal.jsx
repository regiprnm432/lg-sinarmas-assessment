import React, { useState, useEffect } from 'react';
import { X, Save, AlertCircle } from 'lucide-react';

const COMMON_CURRENCIES = ['KRW', 'EUR', 'IDR', 'JPY', 'USD', 'GBP', 'CNY', 'SGD', 'THB', 'VND'];

export default function QuoteModal({ isOpen, onClose, onSave, editingQuote }) {
  const [formData, setFormData] = useState({
    supplierName: '',
    itemCode: '',
    quoteAmount: '',
    quoteCurrency: 'KRW',
    baseCurrency: 'USD',
    budgetAmount: ''
  });
  const [errors, setErrors] = useState({});
  const [submitting, setSubmitting] = useState(false);

  useEffect(() => {
    if (editingQuote) {
      setFormData({
        supplierName: editingQuote.supplierName || '',
        itemCode: editingQuote.itemCode || '',
        quoteAmount: editingQuote.quoteAmount || '',
        quoteCurrency: editingQuote.quoteCurrency || 'KRW',
        baseCurrency: editingQuote.baseCurrency || 'USD',
        budgetAmount: editingQuote.budgetAmount || ''
      });
    } else {
      setFormData({
        supplierName: '',
        itemCode: '',
        quoteAmount: '',
        quoteCurrency: 'KRW',
        baseCurrency: 'USD',
        budgetAmount: ''
      });
    }
    setErrors({});
  }, [editingQuote, isOpen]);

  if (!isOpen) return null;

  const validate = () => {
    const errs = {};
    if (!formData.supplierName.trim()) errs.supplierName = 'Supplier name is required';
    if (!formData.itemCode.trim()) errs.itemCode = 'Item code is required';
    if (!formData.quoteAmount || Number(formData.quoteAmount) <= 0) {
      errs.quoteAmount = 'Quote amount must be greater than 0';
    }
    if (!formData.quoteCurrency || formData.quoteCurrency.length !== 3) {
      errs.quoteCurrency = 'Currency must be a 3-letter code';
    }
    if (!formData.budgetAmount || Number(formData.budgetAmount) <= 0) {
      errs.budgetAmount = 'Budget amount must be greater than 0';
    }
    setErrors(errs);
    return Object.keys(errs).length === 0;
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!validate()) return;

    setSubmitting(true);
    try {
      await onSave({
        ...formData,
        quoteAmount: parseFloat(formData.quoteAmount),
        budgetAmount: parseFloat(formData.budgetAmount),
        quoteCurrency: formData.quoteCurrency.toUpperCase(),
        itemCode: formData.itemCode.toUpperCase()
      });
      onClose();
    } catch (err) {
      if (err.validationErrors) {
        setErrors(err.validationErrors);
      } else {
        setErrors({ general: err.message || 'Failed to save quote' });
      }
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-slate-900/40 backdrop-blur-xs animate-in fade-in duration-150">
      <div className="bg-white rounded-2xl shadow-xl border border-slate-200 w-full max-w-md overflow-hidden">
        {/* Modal Header */}
        <div className="flex items-center justify-between px-6 py-4 border-b border-slate-100 bg-slate-50/50">
          <h3 className="font-bold text-slate-800 text-lg">
            {editingQuote ? 'Edit Supplier Quote' : 'New Supplier Quote'}
          </h3>
          <button
            onClick={onClose}
            className="text-slate-400 hover:text-slate-600 rounded-lg p-1 transition-colors"
          >
            <X className="w-5 h-5" />
          </button>
        </div>

        {/* Modal Body */}
        <form onSubmit={handleSubmit} className="p-6 space-y-4">
          {errors.general && (
            <div className="p-3 rounded-lg bg-rose-50 border border-rose-200 text-rose-700 text-sm flex items-center gap-2">
              <AlertCircle className="w-4 h-4 flex-shrink-0" />
              <span>{errors.general}</span>
            </div>
          )}

          {/* Supplier Name */}
          <div>
            <label className="block text-xs font-semibold text-slate-700 uppercase tracking-wider mb-1">
              Supplier Name *
            </label>
            <input
              type="text"
              placeholder="e.g. LG Display, Bosch, Samsung"
              value={formData.supplierName}
              onChange={(e) => setFormData({ ...formData, supplierName: e.target.value })}
              className={`w-full px-3 py-2 text-sm border rounded-lg focus:outline-none focus:ring-2 ${
                errors.supplierName ? 'border-rose-400 focus:ring-rose-300' : 'border-slate-300 focus:ring-blue-500'
              }`}
            />
            {errors.supplierName && <p className="text-xs text-rose-600 mt-1">{errors.supplierName}</p>}
          </div>

          {/* Item Code */}
          <div>
            <label className="block text-xs font-semibold text-slate-700 uppercase tracking-wider mb-1">
              Item Code *
            </label>
            <input
              type="text"
              placeholder="e.g. OLED-PANEL-55, SMT-CAP-100UF"
              value={formData.itemCode}
              onChange={(e) => setFormData({ ...formData, itemCode: e.target.value })}
              className={`w-full px-3 py-2 text-sm border rounded-lg focus:outline-none focus:ring-2 uppercase ${
                errors.itemCode ? 'border-rose-400 focus:ring-rose-300' : 'border-slate-300 focus:ring-blue-500'
              }`}
            />
            {errors.itemCode && <p className="text-xs text-rose-600 mt-1">{errors.itemCode}</p>}
          </div>

          {/* Quote Amount & Currency */}
          <div className="grid grid-cols-2 gap-3">
            <div>
              <label className="block text-xs font-semibold text-slate-700 uppercase tracking-wider mb-1">
                Quote Amount *
              </label>
              <input
                type="number"
                step="0.01"
                min="0.01"
                placeholder="0.00"
                value={formData.quoteAmount}
                onChange={(e) => setFormData({ ...formData, quoteAmount: e.target.value })}
                className={`w-full px-3 py-2 text-sm border rounded-lg focus:outline-none focus:ring-2 font-mono ${
                  errors.quoteAmount ? 'border-rose-400 focus:ring-rose-300' : 'border-slate-300 focus:ring-blue-500'
                }`}
              />
              {errors.quoteAmount && <p className="text-xs text-rose-600 mt-1">{errors.quoteAmount}</p>}
            </div>

            <div>
              <label className="block text-xs font-semibold text-slate-700 uppercase tracking-wider mb-1">
                Currency *
              </label>
              <select
                value={formData.quoteCurrency}
                onChange={(e) => setFormData({ ...formData, quoteCurrency: e.target.value })}
                className="w-full px-3 py-2 text-sm border border-slate-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 bg-white font-semibold"
              >
                {COMMON_CURRENCIES.map(c => (
                  <option key={c} value={c}>{c}</option>
                ))}
              </select>
            </div>
          </div>

          {/* Budget Limit (in USD) */}
          <div className="grid grid-cols-2 gap-3">
            <div>
              <label className="block text-xs font-semibold text-slate-700 uppercase tracking-wider mb-1">
                Budget Limit *
              </label>
              <input
                type="number"
                step="0.01"
                min="0.01"
                placeholder="0.00"
                value={formData.budgetAmount}
                onChange={(e) => setFormData({ ...formData, budgetAmount: e.target.value })}
                className={`w-full px-3 py-2 text-sm border rounded-lg focus:outline-none focus:ring-2 font-mono ${
                  errors.budgetAmount ? 'border-rose-400 focus:ring-rose-300' : 'border-slate-300 focus:ring-blue-500'
                }`}
              />
              {errors.budgetAmount && <p className="text-xs text-rose-600 mt-1">{errors.budgetAmount}</p>}
            </div>

            <div>
              <label className="block text-xs font-semibold text-slate-700 uppercase tracking-wider mb-1">
                Base Currency
              </label>
              <input
                type="text"
                disabled
                value="USD"
                className="w-full px-3 py-2 text-sm border border-slate-200 bg-slate-100 text-slate-500 rounded-lg font-mono font-semibold"
              />
            </div>
          </div>

          <div className="pt-2 flex items-center justify-end gap-2">
            <button
              type="button"
              onClick={onClose}
              className="px-4 py-2 text-sm font-medium text-slate-600 hover:text-slate-800 hover:bg-slate-100 rounded-lg transition-colors"
            >
              Cancel
            </button>
            <button
              type="submit"
              disabled={submitting}
              className="flex items-center gap-1.5 px-4 py-2 text-sm font-semibold text-white bg-blue-600 hover:bg-blue-700 rounded-lg shadow-sm transition-colors disabled:opacity-50"
            >
              <Save className="w-4 h-4" />
              {submitting ? 'Saving...' : (editingQuote ? 'Update Quote' : 'Create Quote')}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}
