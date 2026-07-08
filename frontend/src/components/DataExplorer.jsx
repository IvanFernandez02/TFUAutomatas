import { useEffect, useState } from 'react'
import { fetchTableData } from '../api/client'

/**
 * DataExplorer: Permite explorar los datos actuales de las tablas en la base de datos.
 * El usuario puede seleccionar una tabla y ver sus datos en tiempo real.
 * Se refresca automáticamente después de operaciones INSERT/UPDATE/DELETE.
 */
export default function DataExplorer({ schema, refreshTrigger }) {
  const [selectedTable, setSelectedTable] = useState(null)
  const [data, setData] = useState([])
  const [loading, setLoading] = useState(false)

  const tables = schema ? Object.keys(schema) : []

  useEffect(() => {
    if (tables.length > 0 && !selectedTable) {
      setSelectedTable(tables[0])
    }
  }, [schema])

  useEffect(() => {
    if (selectedTable) {
      loadData()
    }
  }, [selectedTable, refreshTrigger])

  async function loadData() {
    setLoading(true)
    try {
      const rows = await fetchTableData(selectedTable)
      setData(rows)
    } catch {
      setData([])
    } finally {
      setLoading(false)
    }
  }

  const columns = data.length > 0 ? Object.keys(data[0]) : []

  return (
    <section className="panel">
      <div className="panel-header">
        <h2>📊 Explorador de Datos</h2>
        <button className="refresh-btn" onClick={loadData} disabled={loading || !selectedTable}>
          {loading ? '⏳' : '🔄'} Refrescar
        </button>
      </div>

      <div className="table-selector">
        {tables.map((table) => (
          <button
            key={table}
            className={`chip ${selectedTable === table ? 'chip-active' : ''}`}
            onClick={() => setSelectedTable(table)}
          >
            🗃️ {table}
          </button>
        ))}
      </div>

      {loading && <p className="muted">Cargando datos...</p>}

      {!loading && data.length === 0 && (
        <p className="muted" style={{ marginTop: '1rem' }}>
          La tabla <strong>{selectedTable}</strong> está vacía. Ejecuta un <code>INSERTAR</code> para agregar datos.
        </p>
      )}

      {!loading && data.length > 0 && (
        <div className="data-table-wrapper">
          <table className="data-table">
            <thead>
              <tr>
                {columns.map((col) => (
                  <th key={col}>{col}</th>
                ))}
              </tr>
            </thead>
            <tbody>
              {data.map((row, i) => (
                <tr key={i}>
                  {columns.map((col) => (
                    <td key={col}>
                      {row[col] !== null && row[col] !== undefined
                        ? String(row[col])
                        : <span className="null-value">NULL</span>}
                    </td>
                  ))}
                </tr>
              ))}
            </tbody>
          </table>
          <p className="muted" style={{ marginTop: '0.5rem', fontSize: '0.8rem' }}>
            {data.length} {data.length === 1 ? 'registro' : 'registros'} en <strong>{selectedTable}</strong>
          </p>
        </div>
      )}
    </section>
  )
}
