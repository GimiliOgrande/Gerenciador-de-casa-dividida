import { useCallback, useEffect, useMemo, useState } from 'react'
import { api } from './services/api'
import './App.css'

const today = new Date().toISOString().slice(0, 10)

function App() {
  const [user, setUser] = useState(null)
  const [houses, setHouses] = useState([])
  const [selectedHouseId, setSelectedHouseId] = useState(null)
  const [dashboard, setDashboard] = useState(null)
  const [authMode, setAuthMode] = useState('login')
  const [loading, setLoading] = useState(true)
  const [message, setMessage] = useState('')

  const loadDashboard = useCallback(async (houseId) => {
    if (!houseId) {
      return
    }
    const nextDashboard = await api.getDashboard(houseId)
    setDashboard(nextDashboard)
  }, [])

  useEffect(() => {
    api
      .me()
      .then((currentUser) => {
        setUser(currentUser)
        return loadHouses()
      })
      .catch(() => setUser(null))
      .finally(() => setLoading(false))
    // The initial boot should run once; later house changes are handled explicitly.
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [])

  async function loadHouses() {
    const nextHouses = await api.listHouses()
    setHouses(nextHouses)
    const nextSelectedHouseId = selectedHouseId ?? nextHouses[0]?.id ?? null
    setSelectedHouseId(nextSelectedHouseId)
    if (nextSelectedHouseId) {
      await loadDashboard(nextSelectedHouseId)
    } else {
      setDashboard(null)
    }
    return nextHouses
  }

  async function handleSelectHouse(houseId) {
    setSelectedHouseId(houseId)
    await loadDashboard(houseId)
  }

  async function refreshDashboard() {
    await loadDashboard(selectedHouseId)
  }

  async function handleAuth(event) {
    event.preventDefault()
    setMessage('')
    const form = new FormData(event.currentTarget)
    const payload = Object.fromEntries(form.entries())

    try {
      const currentUser =
        authMode === 'login' ? await api.login(payload) : await api.register(payload)
      setUser(currentUser)
      await loadHouses()
    } catch (error) {
      setMessage(error.message)
    }
  }

  async function handleLogout() {
    await api.logout()
    setUser(null)
    setHouses([])
    setSelectedHouseId(null)
    setDashboard(null)
  }

  async function handleCreateHouse(event) {
    event.preventDefault()
    setMessage('')
    const form = new FormData(event.currentTarget)
    const createdHouse = await api.createHouse(Object.fromEntries(form.entries()))
    event.currentTarget.reset()
    const nextHouses = await api.listHouses()
    setHouses(nextHouses)
    setSelectedHouseId(createdHouse.id)
    await loadDashboard(createdHouse.id)
  }

  async function handleAddMember(event) {
    event.preventDefault()
    setMessage('')
    const form = new FormData(event.currentTarget)
    try {
      await api.addMember(selectedHouseId, Object.fromEntries(form.entries()))
      event.currentTarget.reset()
      await refreshDashboard()
    } catch (error) {
      setMessage(error.message)
    }
  }

  async function handleCreateExpense(event) {
    event.preventDefault()
    const form = new FormData(event.currentTarget)
    await api.createExpense(selectedHouseId, Object.fromEntries(form.entries()))
    event.currentTarget.reset()
    await refreshDashboard()
  }

  async function handleCreateTask(event) {
    event.preventDefault()
    const form = new FormData(event.currentTarget)
    const payload = Object.fromEntries(form.entries())
    payload.assignedToUserId = payload.assignedToUserId ? Number(payload.assignedToUserId) : null
    await api.createTask(selectedHouseId, payload)
    event.currentTarget.reset()
    await refreshDashboard()
  }

  const totals = useMemo(() => {
    const expenses = dashboard?.expenses ?? []
    const pendingPayments = expenses.flatMap((expense) =>
      expense.payments.filter((payment) => payment.status === 'PENDING'),
    )
    const pendingAmount = pendingPayments.reduce((sum, payment) => sum + Number(payment.amount), 0)
    const openTasks = (dashboard?.tasks ?? []).filter((task) => task.status === 'PENDING')
    return {
      expenseCount: expenses.length,
      pendingAmount,
      openTaskCount: openTasks.length,
    }
  }, [dashboard])

  if (loading) {
    return <main className="center-screen">Carregando...</main>
  }

  if (!user) {
    return (
      <main className="auth-shell">
        <section className="auth-panel">
          <div>
            <p className="eyebrow">Casa dividida</p>
            <h1>Organize aluguel, contas e tarefas com quem mora com voce.</h1>
          </div>

          <div className="segmented-control" aria-label="Modo de acesso">
            <button
              type="button"
              className={authMode === 'login' ? 'active' : ''}
              onClick={() => setAuthMode('login')}
            >
              Entrar
            </button>
            <button
              type="button"
              className={authMode === 'register' ? 'active' : ''}
              onClick={() => setAuthMode('register')}
            >
              Cadastrar
            </button>
          </div>

          <form className="form-stack" onSubmit={handleAuth}>
            {authMode === 'register' && (
              <label>
                Nome
                <input name="name" type="text" minLength="2" required />
              </label>
            )}
            <label>
              Email
              <input name="email" type="email" required />
            </label>
            <label>
              Senha
              <input name="password" type="password" minLength="6" required />
            </label>
            {message && <p className="form-message">{message}</p>}
            <button className="primary-button" type="submit">
              {authMode === 'login' ? 'Entrar' : 'Criar conta'}
            </button>
          </form>
        </section>
      </main>
    )
  }

  return (
    <main className="app-shell">
      <aside className="sidebar">
        <div>
          <p className="eyebrow">House Manager</p>
          <h1>Gestor da casa</h1>
        </div>

        <section className="sidebar-section">
          <h2>Casas</h2>
          <div className="house-list">
            {houses.map((house) => (
              <button
                key={house.id}
                type="button"
                className={house.id === selectedHouseId ? 'active' : ''}
                onClick={() => handleSelectHouse(house.id)}
              >
                <span>{house.name}</span>
                <small>{house.address || 'Sem endereco'}</small>
              </button>
            ))}
          </div>
        </section>

        <form className="compact-form" onSubmit={handleCreateHouse}>
          <h2>Nova casa</h2>
          <input name="name" placeholder="Nome da casa" required />
          <input name="address" placeholder="Endereco" />
          <button type="submit">Criar</button>
        </form>

        <button className="ghost-button" type="button" onClick={handleLogout}>
          Sair
        </button>
      </aside>

      <section className="workspace">
        <header className="topbar">
          <div>
            <p className="eyebrow">Ola, {user.name}</p>
            <h2>{dashboard?.house?.name ?? 'Crie uma casa para comecar'}</h2>
          </div>
          {selectedHouseId && (
            <form className="inline-form" onSubmit={handleAddMember}>
              <input name="email" type="email" placeholder="email do morador" required />
              <button type="submit">Adicionar</button>
            </form>
          )}
        </header>

        {message && <p className="form-message">{message}</p>}

        {!selectedHouseId ? (
          <section className="empty-state">
            <h2>Nenhuma casa cadastrada ainda</h2>
            <p>Crie a primeira casa pela barra lateral para liberar despesas, tarefas e moradores.</p>
          </section>
        ) : (
          <>
            <section className="metrics-grid">
              <Metric label="Moradores" value={dashboard?.members?.length ?? 0} />
              <Metric label="Despesas" value={totals.expenseCount} />
              <Metric label="Pendente" value={formatCurrency(totals.pendingAmount)} />
              <Metric label="Tarefas abertas" value={totals.openTaskCount} />
            </section>

            <section className="content-grid">
              <Panel title="Moradores">
                <div className="member-list">
                  {(dashboard?.members ?? []).map((member) => (
                    <div className="member-row" key={member.userId}>
                      <strong>{member.name}</strong>
                      <span>{member.role === 'OWNER' ? 'Responsavel' : 'Morador'}</span>
                    </div>
                  ))}
                </div>
              </Panel>

              <Panel title="Nova despesa">
                <form className="form-stack" onSubmit={handleCreateExpense}>
                  <input name="title" placeholder="Ex: Aluguel de maio" required />
                  <input name="amount" type="number" min="0.01" step="0.01" placeholder="Valor" required />
                  <input name="dueDate" type="date" min={today} required />
                  <button type="submit">Adicionar despesa</button>
                </form>
              </Panel>

              <Panel title="Despesas">
                <div className="item-list">
                  {(dashboard?.expenses ?? []).map((expense) => (
                    <article className="list-item" key={expense.id}>
                      <div>
                        <strong>{expense.title}</strong>
                        <span>{formatCurrency(expense.amount)} ate {formatDate(expense.dueDate)}</span>
                      </div>
                      <div className="payment-list">
                        {expense.payments.map((payment) => (
                          <button
                            key={payment.id}
                            type="button"
                            className={`status-pill ${payment.status.toLowerCase()}`}
                            onClick={() => api.markPaymentAsPaid(expense.id).then(() => refreshDashboard())}
                          >
                            {payment.userName}: {payment.status === 'PAID' ? 'pago' : 'pendente'}
                          </button>
                        ))}
                      </div>
                    </article>
                  ))}
                </div>
              </Panel>

              <Panel title="Nova tarefa">
                <form className="form-stack" onSubmit={handleCreateTask}>
                  <input name="title" placeholder="Ex: Limpar cozinha" required />
                  <textarea name="description" placeholder="Detalhes" rows="3" />
                  <select name="assignedToUserId" defaultValue="">
                    <option value="">Sem responsavel</option>
                    {(dashboard?.members ?? []).map((member) => (
                      <option key={member.userId} value={member.userId}>
                        {member.name}
                      </option>
                    ))}
                  </select>
                  <input name="dueDate" type="date" min={today} required />
                  <button type="submit">Adicionar tarefa</button>
                </form>
              </Panel>

              <Panel title="Tarefas">
                <div className="item-list">
                  {(dashboard?.tasks ?? []).map((task) => (
                    <article className="list-item horizontal" key={task.id}>
                      <div>
                        <strong>{task.title}</strong>
                        <span>
                          {task.assignedToName || 'Sem responsavel'} ate {formatDate(task.dueDate)}
                        </span>
                      </div>
                      <button
                        type="button"
                        className={`status-pill ${task.status.toLowerCase()}`}
                        onClick={() => api.markTaskAsDone(task.id).then(() => refreshDashboard())}
                      >
                        {task.status === 'DONE' ? 'feita' : 'concluir'}
                      </button>
                    </article>
                  ))}
                </div>
              </Panel>
            </section>
          </>
        )}
      </section>
    </main>
  )
}

function Metric({ label, value }) {
  return (
    <article className="metric">
      <span>{label}</span>
      <strong>{value}</strong>
    </article>
  )
}

function Panel({ title, children }) {
  return (
    <section className="panel">
      <h2>{title}</h2>
      {children}
    </section>
  )
}

function formatCurrency(value) {
  return new Intl.NumberFormat('pt-BR', {
    style: 'currency',
    currency: 'BRL',
  }).format(value)
}

function formatDate(value) {
  return new Intl.DateTimeFormat('pt-BR', { timeZone: 'UTC' }).format(new Date(value))
}

export default App
