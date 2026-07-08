export default function SyntaxTreePanel({ astPly, astLlm, astLlmAvailable, astLlmMessage, syntaxError }) {
  // Si hay error sintáctico, estructurarlo como JSON
  const errorJson = syntaxError ? {
    status: "error",
    phase: "sintactico",
    message: syntaxError,
    suggestion: syntaxError.includes("DESDE") 
      ? "Agregue la palabra clave 'DESDE' antes de la tabla."
      : syntaxError.includes("IDENTIFICADOR")
      ? "Verifique los nombres de las columnas o tablas."
      : "Revise la sintaxis de la consulta SQL."
  } : null;

  return (
    <section className="panel">
      <h3>Árbol sintáctico (Parser formal)</h3>
      {astPly ? (
        <pre style={{
          background: '#010409',
          border: '1px solid #30363d',
          borderRadius: '12px',
          padding: '1rem',
          overflow: 'auto',
          maxHeight: '300px',
          color: '#79c0ff',
          fontFamily: 'JetBrains Mono, monospace',
          fontSize: '0.85rem'
        }}>
          <code>{JSON.stringify(astPly, null, 2)}</code>
        </pre>
      ) : errorJson ? (
        <div style={{ marginTop: '0.5rem' }}>
          <p style={{ color: '#ff7b72', margin: '0 0 0.5rem 0', fontSize: '0.9rem', fontWeight: '600' }}>
            ❌ Error Sintáctico Detectado (Formato JSON):
          </p>
          <pre style={{
            background: 'rgba(248, 81, 73, 0.05)',
            border: '1px solid rgba(248, 81, 73, 0.3)',
            borderRadius: '12px',
            padding: '1rem',
            overflow: 'auto',
            maxHeight: '300px',
            color: '#ff7b72',
            fontFamily: 'JetBrains Mono, monospace',
            fontSize: '0.85rem'
          }}>
            <code>{JSON.stringify(errorJson, null, 2)}</code>
          </pre>
        </div>
      ) : (
        <p className="muted">No se generó AST con PLY (ingrese una consulta).</p>
      )}

      <h3 className="section-gap">Árbol asistido (LLM / Ollama)</h3>
      {!astLlmAvailable && <p className="warning">{astLlmMessage || 'Ollama no disponible'}</p>}
      {astLlm ? (
        <pre style={{
          background: '#010409',
          border: '1px solid #30363d',
          borderRadius: '12px',
          padding: '1rem',
          overflow: 'auto',
          maxHeight: '300px',
          color: '#a371f7',
          fontFamily: 'JetBrains Mono, monospace',
          fontSize: '0.85rem'
        }}>
          <code>{JSON.stringify(astLlm, null, 2)}</code>
        </pre>
      ) : (
        <p className="muted">{astLlmMessage || 'Sin árbol LLM generado.'}</p>
      )}
    </section>
  )
}

