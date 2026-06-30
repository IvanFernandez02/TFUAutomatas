export default function SemanticPanel({ valid, semanticErrors, phaseErrors, normalizedQuery }) {
  return (
    <section className="panel">
      <h3>Resultado semántico</h3>
      <p>
        Consulta normalizada: <code>{normalizedQuery || '-'}</code>
      </p>
      <p className={valid ? 'success' : 'error'}>
        {valid ? 'Consulta válida en todas las fases.' : 'La consulta contiene errores.'}
      </p>

      {phaseErrors.length > 0 && (
        <div>
          <h4>Errores por fase</h4>
          <ul className="error-list">
            {phaseErrors.map((error, index) => (
              <li key={`${error.phase}-${index}`}>
                <strong>{error.phase}</strong>: {error.message}
              </li>
            ))}
          </ul>
        </div>
      )}

      {semanticErrors.length > 0 && (
        <div>
          <h4>Errores semánticos</h4>
          <ul className="error-list">
            {semanticErrors.map((error, index) => (
              <li key={`${error}-${index}`}>{error}</li>
            ))}
          </ul>
        </div>
      )}
    </section>
  )
}
