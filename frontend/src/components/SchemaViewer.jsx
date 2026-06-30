export default function SchemaViewer({ schema }) {
  if (!schema) {
    return (
      <section className="panel">
        <h3>Tabla de símbolos</h3>
        <p className="muted">Cargando esquema...</p>
      </section>
    )
  }

  return (
    <section className="panel">
      <h3>Tabla de símbolos</h3>
      <div className="schema-grid">
        {Object.entries(schema).map(([table, info]) => (
          <article key={table} className="schema-card">
            <h4>{table}</h4>
            <ul>
              {Object.entries(info.columnas).map(([column, type]) => (
                <li key={column}>
                  <span>{column}</span>
                  <span className="type-badge">{type}</span>
                </li>
              ))}
            </ul>
          </article>
        ))}
      </div>
    </section>
  )
}
