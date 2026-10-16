import React, { useEffect, useMemo, useState } from 'react';

const STARTER_BOOKS = [
  { id: 1, title: 'Atomic Habits', author: 'James Clear', category: 'Personal Growth', year: '2018', isbn: '9780735211292', copies: 3, loans: [], status: 'Available', description: 'A practical guide to building good habits and breaking bad ones.' },
  { id: 2, title: 'Clean Code', author: 'Robert C. Martin', category: 'Computer Science', year: '2008', isbn: '9780132350884', copies: 4, loans: [], status: 'Available', description: 'Principles and practices for writing readable, maintainable software.' },
  { id: 3, title: 'The Design of Everyday Things', author: 'Don Norman', category: 'Design', year: '2013', isbn: '9780465050659', copies: 2, loans: [], status: 'Available', description: 'A thoughtful introduction to human-centred design.' },
  { id: 4, title: 'Deep Work', author: 'Cal Newport', category: 'Productivity', year: '2016', isbn: '9781455586691', copies: 2, loans: [{ id: 401, name: 'Sample Borrower', contact: '', dueDate: '' }], status: 'Borrowed', description: 'Strategies for focused work in a distracted world.' },
];

const emptyForm = { title: '', author: '', category: 'Computer Science', year: '', isbn: '', copies: '1', status: 'Available', description: '' };
const colors = ['cover-blue', 'cover-plum', 'cover-green', 'cover-gold', 'cover-rose'];
const emptyTask = { title: '', duration: '1', deadline: '', resources: '1' };

export default function App() {
  const [books, setBooks] = useState(() => {
    try {
      const saved = localStorage.getItem('smartlibrary-books-v1');
      return saved ? JSON.parse(saved).map(b => ({ ...b, copies: Math.max(1, Number(b.copies) || 1), loans: b.loans || (b.status === 'Borrowed' ? [{ id: `legacy-${b.id}`, name: b.borrowerName || '', contact: b.borrowerContact || '', dueDate: b.dueDate || '' }] : []) })) : STARTER_BOOKS;
    } catch { return STARTER_BOOKS; }
  });
  const [query, setQuery] = useState('');
  const [category, setCategory] = useState('All categories');
  const [showForm, setShowForm] = useState(false);
  const [form, setForm] = useState(emptyForm);
  const [notice, setNotice] = useState('');
  const [confirmDelete, setConfirmDelete] = useState(null);
  const [view, setView] = useState('collection');
  const [borrowerBook, setBorrowerBook] = useState(null);
  const [borrowerForm, setBorrowerForm] = useState({ name: '', contact: '', dueDate: '' });
  const [activeLoan, setActiveLoan] = useState(null);
  const [tasks, setTasks] = useState(() => { try { return JSON.parse(localStorage.getItem('smartlibrary-planner-tasks-v1') || '[]'); } catch { return []; } });
  const [taskForm, setTaskForm] = useState(emptyTask);
  const [availableTime, setAvailableTime] = useState(() => localStorage.getItem('smartlibrary-planner-time-v1') || '8');
  const [scheduleResult, setScheduleResult] = useState(null);

  useEffect(() => { localStorage.setItem('smartlibrary-books-v1', JSON.stringify(books)); }, [books]);
  useEffect(() => { localStorage.setItem('smartlibrary-planner-tasks-v1', JSON.stringify(tasks)); }, [tasks]);
  useEffect(() => { localStorage.setItem('smartlibrary-planner-time-v1', availableTime); }, [availableTime]);
  useEffect(() => {
    if (!notice) return;
    const timer = setTimeout(() => setNotice(''), 2600);
    return () => clearTimeout(timer);
  }, [notice]);

  const categories = useMemo(() => ['All categories', ...new Set(books.map(b => b.category).filter(Boolean).sort())], [books]);
  const filteredBooks = useMemo(() => books.filter(book => {
    const text = `${book.title} ${book.author} ${book.category} ${book.isbn}`.toLowerCase();
    return text.includes(query.toLowerCase().trim()) && (category === 'All categories' || book.category === category);
  }), [books, query, category]);
  const borrowed = books.reduce((sum, b) => sum + (b.loans?.length || (b.status === 'Borrowed' ? 1 : 0)), 0);
  const totalCopies = books.reduce((sum, b) => sum + Math.max(1, Number(b.copies) || 1), 0);
  const availableCopies = books.reduce((sum, b) => sum + Math.max(0, (Number(b.copies) || 1) - (b.loans?.length || (b.status === 'Borrowed' ? 1 : 0))), 0);
  const allLoans = books.flatMap(book => (book.loans || (book.status === 'Borrowed' ? [{ id: `legacy-${book.id}`, name: book.borrowerName || '', contact: book.borrowerContact || '', dueDate: book.dueDate || '' }] : [])).map(loan => ({ ...loan, bookId: book.id, bookTitle: book.title, author: book.author })));

  function addBook(event) {
    event.preventDefault();
    if (!form.title.trim() || !form.author.trim()) return;
    const book = { ...form, id: Date.now(), title: form.title.trim(), author: form.author.trim(), year: form.year.trim(), copies: Math.max(1, parseInt(form.copies, 10) || 1), loans: [], status: 'Available' };
    setBooks(current => [book, ...current]);
    setForm(emptyForm);
    setShowForm(false);
    setNotice(`“${book.title}” was added to your library.`);
  }

  function removeBook(id) {
    const book = books.find(b => b.id === id);
    setBooks(current => current.filter(b => b.id !== id));
    setConfirmDelete(null);
    setNotice(`“${book?.title || 'Book'}” was removed.`);
  }

  function toggleStatus(book) {
    const loanCount = book.loans?.length || (book.status === 'Borrowed' ? 1 : 0);
    if ((Number(book.copies) || 1) - loanCount <= 0) { setNotice('No copies available. Return a copy first.'); return; }
    setBorrowerBook(book); setActiveLoan(null);
    setBorrowerForm({ name: '', contact: '', dueDate: '' });
  }

  function returnLoan(bookId, loanId) {
    setBooks(current => current.map(b => { if (b.id !== bookId) return b; const loans = (b.loans || []).filter(l => l.id !== loanId); return { ...b, loans, status: loans.length ? 'Borrowed' : 'Available' }; }));
    setNotice('Book returned to the available stock.');
  }

  function saveBorrower(event) {
    event.preventDefault();
    if (!borrowerBook || !borrowerForm.name.trim()) return;
    setBooks(current => current.map(b => { if (b.id !== borrowerBook.id) return b; const existing = b.loans || (b.status === 'Borrowed' ? [{ id: `legacy-${b.id}`, name: b.borrowerName || '', contact: b.borrowerContact || '', dueDate: b.dueDate || '' }] : []); const loans = activeLoan ? existing.map(l => l.id === activeLoan.id ? { ...l, name: borrowerForm.name.trim(), contact: borrowerForm.contact.trim(), dueDate: borrowerForm.dueDate } : l) : [...existing, { id: Date.now(), name: borrowerForm.name.trim(), contact: borrowerForm.contact.trim(), dueDate: borrowerForm.dueDate }]; return { ...b, status: loans.length ? 'Borrowed' : 'Available', loans }; }));
    setNotice(`Borrower recorded for ${borrowerBook.title}.`);
    setBorrowerBook(null); setActiveLoan(null);
  }

  function addTask(event) {
    event.preventDefault();
    const duration = Number(taskForm.duration);
    const resources = Number(taskForm.resources);
    if (!taskForm.title.trim() || !Number.isFinite(duration) || duration <= 0 || !Number.isFinite(resources) || resources < 0) {
      setNotice('Enter a task name, a positive duration, and valid resource usage.'); return;
    }
    setTasks(current => [...current, { ...taskForm, id: Date.now(), title: taskForm.title.trim(), duration, resources }]);
    setTaskForm(emptyTask); setScheduleResult(null); setNotice('Task added to your planner.');
  }

  function generateSchedule() {
    const budget = Number(availableTime);
    if (!Number.isFinite(budget) || budget <= 0) { setNotice('Enter available time greater than zero.'); return; }
    const today = new Date(); today.setHours(0,0,0,0);
    const ordered = [...tasks].sort((a,b) => {
      if (!a.deadline && !b.deadline) return a.duration-b.duration;
      if (!a.deadline) return 1; if (!b.deadline) return -1;
      return a.deadline.localeCompare(b.deadline) || a.duration-b.duration;
    });
    let used = 0; const scheduled = []; const conflicts = [];
    ordered.forEach(task => {
      const deadlineDate = task.deadline ? new Date(`${task.deadline}T00:00:00`) : null;
      if (deadlineDate && deadlineDate < today) conflicts.push({ ...task, reason: 'Deadline has passed' });
      else if (used + Number(task.duration) <= budget) { scheduled.push(task); used += Number(task.duration); }
      else conflicts.push({ ...task, reason: 'Not enough available time' });
    });
    setScheduleResult({ scheduled, conflicts, used, budget, resources: scheduled.reduce((sum,t) => sum + Number(t.resources || 0), 0) });
  }

  return <div className="sl-app">
    <aside className="sl-sidebar">
      <div className="sl-brand"><div className="sl-brand-mark">S<span>✦</span></div><div><div className="sl-brand-name">SmartLibrary</div><div className="sl-brand-sub">YOUR PERSONAL COLLECTION</div></div></div>
      <div className="sl-side-label">LIBRARY</div>
      <div className="sl-side-item active"><span>▤</span> My Collection <b>{books.length}</b></div>
      <button className={`sl-side-item sl-side-button ${view === 'collection' ? 'active' : ''}`} onClick={() => setView('collection')}><span>⌕</span> Browse all books</button>
      <button className={`sl-side-item sl-side-button ${view === 'borrowers' ? 'active' : ''}`} onClick={() => setView('borrowers')}><span>♙</span> Borrowers <b>{borrowed}</b></button>
      <button className={`sl-side-item sl-side-button ${view === 'planner' ? 'active' : ''}`} onClick={() => setView('planner')}><span>◷</span> Task Planner</button>
      <div className="sl-side-label sl-side-label-spaced">MANAGE</div>
      <button className="sl-side-item sl-side-button" onClick={() => setShowForm(true)}><span>＋</span> Add a book</button>
      <div className="sl-sidebar-bottom"><div className="sl-mini-emblem">✧</div><p>A little more knowledge, one book at a time.</p><span>SMARTLIBRARY · PERSONAL EDITION</span></div>
    </aside>

    <main className="sl-main">
      <header className="sl-topbar"><div className="sl-breadcrumb">Library <span>/</span> <strong>{view === 'borrowers' ? 'Borrowers' : view === 'planner' ? 'Task Planner' : 'My Collection'}</strong></div><div className="sl-top-right"><span className="sl-online-dot" /> Local collection <div className="sl-avatar">SL</div></div></header>
      <section className="sl-content">
        <div className="sl-welcome-row"><div><div className="sl-eyebrow">YOUR READING SPACE</div><h1>{view === 'borrowers' ? <>People who <em>borrowed.</em></> : view === 'planner' ? <>Plan work <em>with clarity.</em></> : <>A library of <em>your own.</em></>}</h1><p className="sl-subheading">{view === 'borrowers' ? 'Track who has each borrowed book and when it is due.' : view === 'planner' ? 'Add tasks, set deadlines, and create a practical schedule within your available time.' : 'Keep your favourite reads organised, all in one place.'}</p></div>{view === 'collection' && <button className="sl-primary-btn" onClick={() => setShowForm(true)}><span>＋</span> Add a book</button>}</div>

        {view !== 'planner' && <div className="sl-stats-grid">
          <div className="sl-stat-card"><div className="sl-stat-top"><span>Total books</span><span className="sl-stat-icon">▤</span></div><div className="sl-stat-number">{totalCopies.toString().padStart(2, '0')}</div><div className="sl-stat-foot">Total physical copies</div></div>
          <div className="sl-stat-card"><div className="sl-stat-top"><span>Available</span><span className="sl-stat-icon green">✓</span></div><div className="sl-stat-number">{availableCopies.toString().padStart(2, '0')}</div><div className="sl-stat-foot">Copies ready to borrow</div></div>
          <div className="sl-stat-card"><div className="sl-stat-top"><span>Borrowed</span><span className="sl-stat-icon gold">↗</span></div><div className="sl-stat-number">{borrowed.toString().padStart(2, '0')}</div><div className="sl-stat-foot">Currently checked out</div></div>
        </div>}

        {view === 'planner' ? <section className="sl-planner-panel">
          <div className="sl-collection-heading"><div><div className="sl-eyebrow">PERSONAL WORKSPACE</div><h2>Task planner <span>{tasks.length}</span></h2></div></div>
          <div className="sl-planner-layout"><form className="sl-planner-form" onSubmit={addTask}><h3>Add a task</h3><label className="sl-field sl-field-full">Task name <span>*</span><input required value={taskForm.title} onChange={e=>setTaskForm({...taskForm,title:e.target.value})} placeholder="e.g. Prepare project report" /></label><div className="sl-form-grid"><label className="sl-field">Duration (hours) <span>*</span><input type="number" min="0.25" step="0.25" required value={taskForm.duration} onChange={e=>setTaskForm({...taskForm,duration:e.target.value})}/></label><label className="sl-field">Deadline<input type="date" value={taskForm.deadline} onChange={e=>setTaskForm({...taskForm,deadline:e.target.value})}/></label><label className="sl-field sl-field-full">Estimated resource units<input type="number" min="0" step="1" value={taskForm.resources} onChange={e=>setTaskForm({...taskForm,resources:e.target.value})}/><small>Use a consistent unit, such as people, devices, or resource points.</small></label></div><button className="sl-primary-btn" type="submit">＋ Add task</button></form>
          <div className="sl-planner-settings"><h3>Schedule settings</h3><label className="sl-field">Total available time (hours)<input type="number" min="0.25" step="0.25" value={availableTime} onChange={e=>{setAvailableTime(e.target.value);setScheduleResult(null)}}/></label><p>The planner prioritises earlier deadlines, then fits tasks into the available time. Past-deadline and over-budget tasks are listed as conflicts.</p><button className="sl-primary-btn" onClick={generateSchedule} type="button">Generate recommended schedule</button></div></div>
          <div className="sl-task-list"><div className="sl-collection-heading"><div><div className="sl-eyebrow">YOUR TASKS</div><h2>Task list <span>{tasks.length}</span></h2></div></div>{tasks.length ? <div className="sl-task-table-wrap"><table className="sl-task-table"><thead><tr><th>Task</th><th>Duration</th><th>Deadline</th><th>Resources</th><th></th></tr></thead><tbody>{tasks.map(task=><tr key={task.id}><td>{task.title}</td><td>{task.duration} h</td><td>{task.deadline || 'Not set'}</td><td>{task.resources}</td><td><button className="sl-remove-btn" aria-label={`Remove ${task.title}`} onClick={()=>{setTasks(cur=>cur.filter(t=>t.id!==task.id));setScheduleResult(null)}}>Remove</button></td></tr>)}</tbody></table></div> : <div className="sl-empty sl-planner-empty"><h3>No tasks added yet</h3><p>Add tasks above to generate a schedule.</p></div>}</div>
          {scheduleResult && <div className="sl-schedule-results"><div className="sl-collection-heading"><div><div className="sl-eyebrow">RECOMMENDATION</div><h2>Suggested schedule</h2></div><span className="sl-schedule-summary">{scheduleResult.used} / {scheduleResult.budget} hours used</span></div><div className="sl-schedule-metrics"><div><strong>{scheduleResult.scheduled.length}</strong><span>Scheduled tasks</span></div><div><strong>{scheduleResult.resources}</strong><span>Estimated resource units</span></div><div><strong>{scheduleResult.conflicts.length}</strong><span>Conflicts / unselected</span></div></div><div className="sl-planner-results-grid"><section><h3>Recommended tasks</h3>{scheduleResult.scheduled.length ? <ol className="sl-result-list">{scheduleResult.scheduled.map((task,i)=><li key={task.id}><span className="sl-result-number">{i+1}</span><div><strong>{task.title}</strong><small>{task.duration} hours · {task.resources} resource units{task.deadline ? ` · Due ${task.deadline}` : ''}</small></div></li>)}</ol> : <p className="sl-muted">No tasks fit the current time and deadline constraints.</p>}</section><section><h3>Conflicting or unselected</h3>{scheduleResult.conflicts.length ? <ul className="sl-conflict-list">{scheduleResult.conflicts.map(task=><li key={task.id}><strong>{task.title}</strong><small>{task.reason} · {task.duration} hours</small></li>)}</ul> : <p className="sl-muted">All tasks fit the current constraints.</p>}</section></div><p className="sl-planner-note">This is a heuristic recommendation, not a guaranteed mathematically optimal schedule. Review task dependencies and real resource limits before finalising.</p></div>}
        </section> : view === 'borrowers' ? <section className="sl-borrowers-panel"><div className="sl-collection-heading"><div><div className="sl-eyebrow">LOAN REGISTER</div><h2>Borrower records <span>{allLoans.length}</span></h2></div></div>{allLoans.length ? <div className="sl-borrower-list">{allLoans.map(loan => <article className="sl-borrower-row" key={loan.id}><div className="sl-borrower-avatar">{(loan.name || '?').slice(0,1).toUpperCase()}</div><div className="sl-borrower-main"><strong>{loan.name || 'Borrower not recorded'}</strong><span>{loan.bookTitle} · by {loan.author}</span><small>{loan.contact || 'No contact details'}{loan.dueDate ? ` · Due ${loan.dueDate}` : ' · No due date set'}</small></div><button className="sl-secondary-btn" onClick={() => { const book = books.find(b => b.id === loan.bookId); setBorrowerBook(book); setActiveLoan(loan); setBorrowerForm({ name: loan.name || '', contact: loan.contact || '', dueDate: loan.dueDate || '' }); }}>Edit details</button><button className="sl-secondary-btn" onClick={() => returnLoan(loan.bookId, loan.id)}>Return book</button></article>)}</div> : <div className="sl-empty"><div className="sl-empty-icon">♙</div><h3>No borrowed books yet</h3><p>Mark a book as borrowed and add the person's details to show them here.</p><button className="sl-primary-btn" onClick={() => setView('collection')}>Browse books</button></div>}</section> : view === 'collection' ? <>
        <div className="sl-collection-heading"><div><div className="sl-eyebrow">THE BOOKSHELF</div><h2>Your collection <span>{books.length}</span></h2></div><div className="sl-filters"><label className="sl-search"><span>⌕</span><input aria-label="Search books" placeholder="Search title, author, ISBN..." value={query} onChange={e => setQuery(e.target.value)} /><kbd>⌘ K</kbd></label><select aria-label="Filter by category" value={category} onChange={e => setCategory(e.target.value)}>{categories.map(c => <option key={c}>{c}</option>)}</select></div></div>

        {filteredBooks.length ? <div className="sl-book-grid">{filteredBooks.map((book, index) => <article className="sl-book-card" key={book.id}>
          <div className={`sl-book-cover ${colors[index % colors.length]}`}><div className="sl-cover-top">SMARTLIBRARY EDITION</div><div className="sl-cover-symbol">{book.category?.toLowerCase().includes('computer') ? '⌘' : book.category?.toLowerCase().includes('design') ? '◈' : '✧'}</div><div className="sl-cover-title">{book.title}</div><div className="sl-cover-rule"/><div className="sl-cover-author">{book.author}</div><div className="sl-cover-bottom">{book.year || 'COLLECTION'}</div></div>
          <div className="sl-book-info"><div className="sl-book-category">{book.category || 'Uncategorised'}</div><h3>{book.title}</h3><p className="sl-book-author">by {book.author}</p>{book.isbn && <div className="sl-isbn">ISBN · {book.isbn}</div>}<div className="sl-copy-count"><strong>{Math.max(0, (Number(book.copies)||1) - (book.loans?.length || (book.status === 'Borrowed' ? 1 : 0)))}</strong> available <span>of {Number(book.copies)||1} copies</span></div><div className="sl-book-actions"><button className={`sl-status-pill ${((Number(book.copies)||1) - (book.loans?.length || (book.status === 'Borrowed' ? 1 : 0))) <= 0 ? 'borrowed' : ''}`} onClick={() => toggleStatus(book)} title="Record a loan for one available copy"><span />{((Number(book.copies)||1) - (book.loans?.length || (book.status === 'Borrowed' ? 1 : 0))) > 0 ? 'Borrow a copy' : 'All borrowed'}</button><button className="sl-remove-btn" onClick={() => setConfirmDelete(book.id)} aria-label={`Remove ${book.title}`} title="Remove book">⌫</button></div></div>
        </article>)}</div> : <div className="sl-empty"><div className="sl-empty-icon">▤</div><h3>{books.length ? 'No books found' : 'Your bookshelf is waiting.'}</h3><p>{books.length ? 'Try another title, author, ISBN or category.' : 'Add your first book and start building your personal collection.'}</p>{!books.length && <button className="sl-primary-btn" onClick={() => setShowForm(true)}>＋ Add your first book</button>}</div>}
        </> : null}



        <footer className="sl-footer"><span>© {new Date().getFullYear()} SmartLibrary</span><span>Thoughtfully organised. Personally yours.</span></footer>
      </section>
    </main>

    {showForm && <div className="sl-modal-backdrop" onMouseDown={e => { if (e.target === e.currentTarget) setShowForm(false); }}><section className="sl-modal" role="dialog" aria-modal="true" aria-labelledby="sl-form-title"><div className="sl-modal-heading"><div><div className="sl-eyebrow">GROW YOUR COLLECTION</div><h2 id="sl-form-title">Add a new book</h2><p>Enter the details below to add it to your bookshelf.</p></div><button className="sl-close-btn" onClick={() => setShowForm(false)} aria-label="Close">×</button></div><form onSubmit={addBook}>
      <div className="sl-form-grid"><label className="sl-field sl-field-full">Book title <span>*</span><input autoFocus required value={form.title} onChange={e => setForm({ ...form, title: e.target.value })} placeholder="e.g. The Pragmatic Programmer" /></label><label className="sl-field">Author <span>*</span><input required value={form.author} onChange={e => setForm({ ...form, author: e.target.value })} placeholder="Author name" /></label><label className="sl-field">Category<select value={form.category} onChange={e => setForm({ ...form, category: e.target.value })}>{['Computer Science', 'Literature', 'Personal Growth', 'Science', 'Design', 'Productivity', 'History', 'Business', 'Other'].map(c => <option key={c}>{c}</option>)}</select></label><label className="sl-field">Publication year<input inputMode="numeric" value={form.year} onChange={e => setForm({ ...form, year: e.target.value })} placeholder="e.g. 2024" /></label><label className="sl-field">ISBN (optional)<input value={form.isbn} onChange={e => setForm({ ...form, isbn: e.target.value })} placeholder="ISBN number" /></label><label className="sl-field">Number of copies <span>*</span><input type="number" min="1" required value={form.copies} onChange={e => setForm({ ...form, copies: e.target.value })} /></label><label className="sl-field sl-field-full">Short description (optional)<textarea rows="3" value={form.description} onChange={e => setForm({ ...form, description: e.target.value })} placeholder="A short note about this book..." /></label></div><div className="sl-modal-actions"><button type="button" className="sl-secondary-btn" onClick={() => setShowForm(false)}>Cancel</button><button type="submit" className="sl-primary-btn">＋ Save book</button></div>
    </form></section></div>}

    {borrowerBook && <div className="sl-modal-backdrop" onMouseDown={e => { if (e.target === e.currentTarget) setBorrowerBook(null); }}><section className="sl-modal" role="dialog" aria-modal="true" aria-labelledby="sl-borrower-title"><div className="sl-modal-heading"><div><div className="sl-eyebrow">LOAN DETAILS</div><h2 id="sl-borrower-title">Record borrower</h2><p>{borrowerBook.title} — {borrowerBook.author}</p></div><button className="sl-close-btn" onClick={() => setBorrowerBook(null)} aria-label="Close">×</button></div><form onSubmit={saveBorrower}><div className="sl-form-grid"><label className="sl-field sl-field-full">Borrower's full name <span>*</span><input autoFocus required value={borrowerForm.name} onChange={e => setBorrowerForm({ ...borrowerForm, name: e.target.value })} placeholder="Enter borrower name" /></label><label className="sl-field sl-field-full">Contact details (optional)<input value={borrowerForm.contact} onChange={e => setBorrowerForm({ ...borrowerForm, contact: e.target.value })} placeholder="Phone number or email" /></label><label className="sl-field sl-field-full">Return due date (optional)<input type="date" value={borrowerForm.dueDate} onChange={e => setBorrowerForm({ ...borrowerForm, dueDate: e.target.value })} /></label></div><div className="sl-modal-actions"><button type="button" className="sl-secondary-btn" onClick={() => setBorrowerBook(null)}>Cancel</button><button type="submit" className="sl-primary-btn">Save borrower</button></div></form></section></div>}

    {confirmDelete !== null && <div className="sl-modal-backdrop"><section className="sl-confirm-modal" role="alertdialog" aria-modal="true"><div className="sl-confirm-icon">⌫</div><h2>Remove this book?</h2><p>It will be removed from your SmartLibrary collection. This action cannot be undone.</p><div className="sl-modal-actions"><button className="sl-secondary-btn" onClick={() => setConfirmDelete(null)}>Keep book</button><button className="sl-danger-btn" onClick={() => removeBook(confirmDelete)}>Remove book</button></div></section></div>}
    {notice && <div className="sl-toast"><span>✓</span>{notice}</div>}
  </div>;
}
