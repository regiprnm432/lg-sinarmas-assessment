const API_BASE = '/api/quotes';

export async function fetchQuotes(filters = {}) {
  const params = new URLSearchParams();
  if (filters.supplier) params.append('supplier', filters.supplier);
  if (filters.currency) params.append('currency', filters.currency);
  if (filters.budgetFlag) params.append('budgetFlag', filters.budgetFlag);
  if (filters.itemCode) params.append('itemCode', filters.itemCode);

  const query = params.toString() ? `?${params.toString()}` : '';
  const response = await fetch(`${API_BASE}${query}`);
  if (!response.ok) {
    const errData = await response.json().catch(() => ({}));
    throw new Error(errData.message || `Failed to fetch quotes (${response.status})`);
  }
  return response.json();
}

export async function createQuote(payload) {
  const response = await fetch(API_BASE, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(payload)
  });
  if (!response.ok) {
    const errData = await response.json().catch(() => ({}));
    const error = new Error(errData.message || 'Failed to create quote');
    error.validationErrors = errData.validationErrors;
    throw error;
  }
  return response.json();
}

export async function updateQuote(id, payload) {
  const response = await fetch(`${API_BASE}/${id}`, {
    method: 'PUT',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(payload)
  });
  if (!response.ok) {
    const errData = await response.json().catch(() => ({}));
    const error = new Error(errData.message || 'Failed to update quote');
    error.validationErrors = errData.validationErrors;
    throw error;
  }
  return response.json();
}

export async function deleteQuote(id) {
  const response = await fetch(`${API_BASE}/${id}`, {
    method: 'DELETE'
  });
  if (!response.ok) {
    const errData = await response.json().catch(() => ({}));
    throw new Error(errData.message || 'Failed to delete quote');
  }
  return true;
}

export async function recalculateQuotes() {
  const response = await fetch(`${API_BASE}/recalculate`, {
    method: 'POST'
  });
  if (!response.ok) {
    throw new Error('Failed to recalculate quotes');
  }
  return response.json();
}

export async function fetchCachedRates() {
  const response = await fetch(`${API_BASE}/rates`);
  if (!response.ok) {
    throw new Error('Failed to fetch cached rates');
  }
  return response.json();
}

export async function exportQuotesExcel(filters = {}) {
  const params = new URLSearchParams();
  if (filters.supplier) params.append('supplier', filters.supplier);
  if (filters.currency) params.append('currency', filters.currency);
  if (filters.budgetFlag) params.append('budgetFlag', filters.budgetFlag);
  if (filters.itemCode) params.append('itemCode', filters.itemCode);

  const query = params.toString() ? `?${params.toString()}` : '';
  const response = await fetch(`${API_BASE}/export/excel${query}`);
  if (!response.ok) {
    throw new Error('Failed to export quotes to Excel');
  }
  const blob = await response.blob();
  const downloadUrl = window.URL.createObjectURL(blob);
  const link = document.createElement('a');
  link.href = downloadUrl;
  link.download = `quotes_comparison_${new Date().toISOString().slice(0, 10)}.xlsx`;
  document.body.appendChild(link);
  link.click();
  link.remove();
  window.URL.revokeObjectURL(downloadUrl);
}

