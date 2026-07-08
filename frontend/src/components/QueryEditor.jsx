const EXAMPLES_GROUPS = [
  {
    category: '👤 Usuarios (Válidas)',
    items: [
      { label: 'Ver todos', query: 'SELECCIONAR * DESDE usuarios' },
      { label: 'Filtrar por edad', query: 'SELECCIONAR nombre, edad DESDE usuarios CUANDO edad > 18' },
      { label: 'Insertar usuario', query: "INSERTAR EN usuarios (nombre, edad, email) VALORES ('Cesar', 21, 'cesarramos@gmail.com')" },
      { label: 'Modificar edad', query: "MODIFICAR usuarios ESTABLECER edad = 22 CUANDO nombre = 'Cesar'" },
      { label: 'Eliminar joven', query: 'ELIMINAR DESDE usuarios CUANDO edad < 18' },
    ]
  },
  {
    category: '👤 Usuarios (Con Errores)',
    items: [
      { label: 'Columna inválida', query: 'SELECCIONAR salario DESDE usuarios' },
      { label: 'Tabla inexistente', query: 'SELECCIONAR * DESDE clientes' },
      { label: 'Error de sintaxis', query: 'SELECCIONAR nombre DESDE usuarios DONDE edad = 21' },
    ]
  },
  {
    category: '📦 Productos (Válidas)',
    items: [
      { label: 'Ver todos', query: 'SELECCIONAR * DESDE productos' },
      { label: 'Filtrar por precio', query: 'SELECCIONAR nombre, precio DESDE productos CUANDO precio > 500' },
      { label: 'Insertar producto', query: "INSERTAR EN productos (nombre, precio) VALORES ('Mouse Gamer', 49.99)" },
      { label: 'Modificar precio', query: "MODIFICAR productos ESTABLECER precio = 45.00 CUANDO nombre = 'Mouse Gamer'" },
    ]
  },
  {
    category: '📦 Productos (Con Errores)',
    items: [
      { label: 'Columna inválida', query: 'SELECCIONAR precio DESDE productos CUANDO stock > 10' },
      { label: 'Establecer inválido', query: 'MODIFICAR productos ESTABLECER stock = 5 CUANDO id = 1' },
    ]
  }
]

export default function QueryEditor({ query, onChange, onAnalyze, loading }) {
  return (
    <section className="panel">
      <div className="panel-header">
        <h2>Consulta SQL en español</h2>
        <button className="primary-btn" onClick={onAnalyze} disabled={loading || !query.trim()}>
          {loading ? 'LLM Analizando...' : 'Analizar'}
        </button>
      </div>
      <textarea
        value={query}
        onChange={(e) => onChange(e.target.value)}
        rows={5}
        placeholder="Escribe una consulta, por ejemplo: SELECCIONAR nombre DESDE usuarios CUANDO edad > 18"
      />
      <div className="examples-container" style={{ marginTop: '1.25rem', display: 'flex', flexDirection: 'column', gap: '1rem' }}>
        {EXAMPLES_GROUPS.map((group) => (
          <div key={group.category} className="example-group">
            <h4 style={{ margin: '0 0 0.4rem 0', fontSize: '0.85rem', color: group.category.includes('Errores') ? '#ff7b72' : '#79c0ff' }}>
              {group.category}
            </h4>
            <div className="examples" style={{ display: 'flex', flexWrap: 'wrap', gap: '0.4rem', marginTop: 0 }}>
              {group.items.map((example) => (
                <button
                  key={example.label}
                  className="chip"
                  style={{
                    fontSize: '0.8rem',
                    padding: '0.35rem 0.75rem',
                    borderColor: group.category.includes('Errores') ? 'rgba(248, 81, 73, 0.3)' : 'rgba(56, 139, 253, 0.3)',
                    background: group.category.includes('Errores') ? 'rgba(248, 81, 73, 0.05)' : 'rgba(56, 139, 253, 0.05)'
                  }}
                  onClick={() => onChange(example.query)}
                >
                  {example.label}
                </button>
              ))}
            </div>
          </div>
        ))}
      </div>
    </section>
  )
}

