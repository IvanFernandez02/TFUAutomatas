/**
 * ResultPanel: Muestra los resultados de la ejecución de la consulta en PostgreSQL.
 * Para SELECT: muestra una tabla de datos con las filas retornadas.
 * Para INSERT/UPDATE/DELETE: muestra un mensaje de éxito con filas afectadas.
 */
export default function ResultPanel({ executionResult }) {
  if (!executionResult) {
    return (
      <section className="panel">
        <h3>Resultado de Ejecución</h3>
        <p className="muted">
          Ejecuta una consulta válida para ver los resultados en la base de datos.
        </p>
      </section>
    )
  }

  const { success, message, column_names, rows, affected_rows, query_type, executed_sql } =
    executionResult

  return (
    <section className="panel">
      <div className="result-header">
        <h3>Resultado de Ejecución</h3>
        <span className={`result-badge ${success ? 'badge-success' : 'badge-error'}`}>
          {success ? '✓ Ejecutado' : '✗ Error'}
        </span>
      </div>

      <div className={`result-message ${success ? 'success' : 'error'}`}>
        <span className="result-icon">{success ? '🟢' : '🔴'}</span>
        {message}
      </div>

      {executed_sql && (
        <div className="executed-sql">
          <span className="sql-label">SQL ejecutado:</span>
          <code>{executed_sql}</code>
        </div>
      )}

      {query_type === 'SELECT' && rows && rows.length > 0 && (
        <div className="result-table-wrapper">
          <table className="result-table">
            <thead>
              <tr>
                {column_names.map((col) => (
                  <th key={col}>{col}</th>
                ))}
              </tr>
            </thead>
            <tbody>
              {rows.map((row, i) => (
                <tr key={i}>
                  {column_names.map((col) => (
                    <td key={col}>{row[col] !== null && row[col] !== undefined ? String(row[col]) : <span className="null-value">NULL</span>}</td>
                  ))}
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      {query_type === 'SELECT' && rows && rows.length === 0 && (
        <p className="muted" style={{ marginTop: '1rem' }}>
          La consulta no retornó resultados.
        </p>
      )}

      {query_type !== 'SELECT' && success && (
        <div className="affected-count">
          <span className="affected-number">{affected_rows}</span>
          <span>{affected_rows === 1 ? 'fila afectada' : 'filas afectadas'}</span>
        </div>
      )}
    </section>
  )
}
