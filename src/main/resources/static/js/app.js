document.addEventListener('DOMContentLoaded', () => {
    const form = document.getElementById('expenseForm');
    const submitBtn = document.getElementById('submitBtn');
    const formMessage = document.getElementById('formMessage');
    const expenseList = document.getElementById('expenseList');
    const totalAmountEl = document.getElementById('totalAmount');
    const filterCategory = document.getElementById('filterCategory');
    const sortDate = document.getElementById('sortDate');
    const loadingExpenses = document.getElementById('loadingExpenses');
    const categorySummaryEl = document.getElementById('categorySummary');

    // State
    let isSubmitting = false;

    // Load initial expenses
    fetchExpenses();

    // Event Listeners
    form.addEventListener('submit', handleFormSubmit);
    filterCategory.addEventListener('change', fetchExpenses);
    sortDate.addEventListener('change', fetchExpenses);

    async function handleFormSubmit(e) {
        e.preventDefault();
        if (isSubmitting) return;

        // Clear previous errors
        clearErrors();
        
        const amount = document.getElementById('amount').value;
        const category = document.getElementById('category').value;
        const date = document.getElementById('date').value;
        const description = document.getElementById('description').value;

        // Basic client-side validation
        if (amount <= 0) {
            showError('amountError', 'Amount must be greater than zero.');
            return;
        }
        if (!category) {
            showError('categoryError', 'Please select a category.');
            return;
        }
        if (!date) {
            showError('dateError', 'Please select a date.');
            return;
        }

        const expenseData = {
            amount: parseFloat(amount),
            category,
            date,
            description
        };

        setLoading(true);

        try {
            // Include idempotency key for retries due to network issues
            const idempotencyKey = crypto.randomUUID ? crypto.randomUUID() : Date.now().toString();
            
            const response = await fetch('/expenses', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'Idempotency-Key': idempotencyKey
                },
                body: JSON.stringify(expenseData)
            });

            if (!response.ok) {
                const errorData = await response.json();
                handleServerErrors(errorData);
                return;
            }

            // Success
            form.reset();
            showMessage('Expense added successfully!', 'success');
            fetchExpenses(); // Refresh list

        } catch (error) {
            showMessage('Network error or server is down. Please try again.', 'error');
        } finally {
            setLoading(false);
        }
    }

    async function fetchExpenses() {
        const category = filterCategory.value;
        const sort = sortDate.value;

        let url = `/expenses?sort=${sort}`;
        if (category) {
            url += `&category=${encodeURIComponent(category)}`;
        }

        expenseList.innerHTML = '';
        loadingExpenses.classList.remove('hidden');
        filterCategory.disabled = true;
        sortDate.disabled = true;

        try {
            const response = await fetch(url);
            if (!response.ok) throw new Error('Failed to fetch expenses');

            const expenses = await response.json();
            renderExpenses(expenses);
        } catch (error) {
            expenseList.innerHTML = `<div class="message error">Failed to load expenses.</div>`;
            categorySummaryEl.innerHTML = '';
        } finally {
            loadingExpenses.classList.add('hidden');
            filterCategory.disabled = false;
            sortDate.disabled = false;
        }
    }

    function renderExpenses(expenses) {
        if (expenses.length === 0) {
            expenseList.innerHTML = '<p style="text-align:center; color:var(--text-muted); padding: 1rem;">No expenses found.</p>';
            totalAmountEl.textContent = '₹0.00';
            categorySummaryEl.innerHTML = '';
            return;
        }

        let total = 0;
        const categoryTotals = {};

        const html = expenses.map(expense => {
            total += expense.amount;
            
            if (!categoryTotals[expense.category]) {
                categoryTotals[expense.category] = 0;
            }
            categoryTotals[expense.category] += expense.amount;

            const dateObj = new Date(expense.date);
            const formattedDate = dateObj.toLocaleDateString(undefined, { year: 'numeric', month: 'short', day: 'numeric' });
            
            return `
                <div class="expense-item">
                    <div class="expense-details">
                        <h4>${escapeHtml(expense.category)}</h4>
                        <div class="expense-meta">
                            ${formattedDate} ${expense.description ? `• ${escapeHtml(expense.description)}` : ''}
                        </div>
                    </div>
                    <div class="expense-amount">₹${expense.amount.toFixed(2)}</div>
                </div>
            `;
        }).join('');

        expenseList.innerHTML = html;
        totalAmountEl.textContent = `₹${total.toFixed(2)}`;

        const summaryHtml = Object.entries(categoryTotals)
            .map(([cat, amt]) => `<div class="summary-item"><span>${escapeHtml(cat)}</span> <span>₹${amt.toFixed(2)}</span></div>`)
            .join('');
        categorySummaryEl.innerHTML = `<h4>Summary by Category</h4><div class="summary-grid">${summaryHtml}</div>`;
    }

    function setLoading(isLoading) {
        isSubmitting = isLoading;
        submitBtn.disabled = isLoading;
        submitBtn.querySelector('.btn-text').textContent = isLoading ? 'Adding...' : 'Add Expense';
        if (isLoading) {
            submitBtn.querySelector('.spinner').classList.remove('hidden');
        } else {
            submitBtn.querySelector('.spinner').classList.add('hidden');
        }
    }

    function showError(elementId, message) {
        const errorEl = document.getElementById(elementId);
        if(errorEl) errorEl.textContent = message;
    }

    function clearErrors() {
        document.querySelectorAll('.error-text').forEach(el => el.textContent = '');
        formMessage.classList.add('hidden');
    }

    function handleServerErrors(errors) {
        if (errors && typeof errors === 'object' && !Array.isArray(errors)) {
            for (const [field, message] of Object.entries(errors)) {
                const errorEl = document.getElementById(`${field}Error`);
                if (errorEl) {
                    errorEl.textContent = message;
                }
            }
        } else {
            showMessage('An unexpected error occurred.', 'error');
        }
    }

    function showMessage(text, type) {
        formMessage.textContent = text;
        formMessage.className = `message ${type}`;
        formMessage.classList.remove('hidden');
        
        if (type === 'success') {
            setTimeout(() => {
                formMessage.classList.add('hidden');
            }, 3000);
        }
    }

    // Basic HTML escaping to prevent XSS
    function escapeHtml(unsafe) {
        if (!unsafe) return '';
        return unsafe.toString()
             .replace(/&/g, "&amp;")
             .replace(/</g, "&lt;")
             .replace(/>/g, "&gt;")
             .replace(/"/g, "&quot;")
             .replace(/'/g, "&#039;");
    }
});
