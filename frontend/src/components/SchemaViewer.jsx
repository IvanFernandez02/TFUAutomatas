/**
 * SchemaViewer: Visualización de la Tabla de Símbolos del compilador.
 * 
 * En teoría de compiladores, la Tabla de Símbolos es una estructura de datos
 * que almacena información sobre los identificadores (tablas, columnas, tipos).
 * El analizador semántico usa esta tabla para validar que las columnas y tablas
 * referenciadas en la consulta realmente existan y tengan tipos compatibles.
 * 
 * Esta tabla se carga desde schema.json y se valida contra la base de datos PostgreSQL.
 */
export default function SchemaViewer({ schema }) {
  if (!schema) {
    return (
      <section className="panel">
        <h2>📋 Tabla de Símbolos</h2>
        <p className="muted">Cargando esquema...</p>
      </section>
    )
  }

  const tableCount = Object.keys(schema).length
  const totalColumns = Object.values(schema).reduce(
    (acc, table) => acc + Object.keys(table.columnas).length,
    0
  )

  return (
    <section className="panel symbol-table-panel">
      <div className="panel-header">
        <h2>📋 Tabla de Símbolos</h2>
        <div className="symbol-stats">
          <span className="stat-badge">{tableCount} tablas</span>
          <span className="stat-badge">{totalColumns} columnas</span>
        </div>
      </div>

      <p className="symbol-description">
        Estructura de datos usada por el <strong>analizador semántico</strong> para validar
        que las tablas y columnas referenciadas en la consulta existan y tengan tipos compatibles.
      </p>

      <div className="schema-grid">
        {Object.entries(schema).map(([table, info]) => (
          <article key={table} className="schema-card">
            <div className="schema-card-header">
              <span className="table-icon">🗃️</span>
              <h4>{table}</h4>
            </div>
            <ul>
              {Object.entries(info.columnas).map(([column, type]) => (
                <li key={column}>
                  <span className="column-name">{column}</span>
                  <span className={`type-badge type-${type}`}>{type}</span>
                </li>
              ))}
            </ul>
          </article>
        ))}
      </div>

      <div className="symbol-legend">
        <span className="legend-title">Tipos de datos:</span>
        <span className="type-badge type-entero">entero</span>
        <span className="type-badge type-texto">texto</span>
        <span className="type-badge type-decimal">decimal</span>
      </div>
    </section>
  )
}
