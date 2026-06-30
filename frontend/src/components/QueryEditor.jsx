const EXAMPLES = [
  {
    label: 'SELECT válido',
    query: 'SELECCIONAR nombre, edad DESDE usuarios CUANDO edad > 18',
  },
  {
    label: 'Error semántico',
    query: 'SELECCIONAR salario DESDE usuarios',
  },
  {
    label: 'INSERT válido',
    query: "INSERTAR EN usuarios (nombre, edad) VALORES ('Ana', 25)",
  },
  {
    label: 'UPDATE válido',
    query: "MODIFICAR usuarios ESTABLECER edad = 30 CUANDO nombre = 'Ana'",
  },
  {
    label: 'DELETE válido',
    query: 'ELIMINAR DESDE usuarios CUANDO edad < 18',
  },
  {
    label: 'SELECT con *',
    query: 'SELECCIONAR * DESDE usuarios',
  },
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
      <div className="examples">
        {EXAMPLES.map((example) => (
          <button key={example.label} className="chip" onClick={() => onChange(example.query)}>
            {example.label}
          </button>
        ))}
      </div>
    </section>
  )
}
