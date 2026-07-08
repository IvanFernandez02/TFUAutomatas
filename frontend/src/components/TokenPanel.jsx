export default function TokenPanel({ tokens, nlpSegments, lexicalExplanation }) {
  const lexicalErrors = lexicalExplanation?.errors || [];

  return (
    <section className="panel">
      {lexicalErrors.length > 0 && (
        <div className="lexical-explanation-box" style={{
          background: 'rgba(248, 81, 73, 0.1)',
          border: '1px solid rgba(248, 81, 73, 0.3)',
          borderRadius: '14px',
          padding: '1rem',
          marginBottom: '1.5rem'
        }}>
          <h4 style={{ color: '#ff7b72', margin: '0 0 0.75rem 0', display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
            ⚠️ Explicación de Errores Léxicos (Ollama / Compilador)
          </h4>
          <table style={{ width: '100%', borderCollapse: 'collapse' }}>
            <thead>
              <tr>
                <th style={{ color: '#ff7b72', borderBottom: '1px solid rgba(248, 81, 73, 0.2)' }}>Token</th>
                <th style={{ color: '#ff7b72', borderBottom: '1px solid rgba(248, 81, 73, 0.2)' }}>Causa</th>
                <th style={{ color: '#ff7b72', borderBottom: '1px solid rgba(248, 81, 73, 0.2)' }}>Sugerencia</th>
              </tr>
            </thead>
            <tbody>
              {lexicalErrors.map((err, idx) => (
                <tr key={idx}>
                  <td style={{ fontFamily: 'JetBrains Mono, monospace', fontWeight: 'bold', padding: '0.5rem 0' }}>
                    <code>{err.token}</code>
                  </td>
                  <td style={{ padding: '0.5rem 0', fontSize: '0.9rem' }}>{err.cause}</td>
                  <td style={{ padding: '0.5rem 0', fontSize: '0.9rem', color: '#58a6ff' }}>{err.suggestion}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      <h3>Tokens (PLY)</h3>
      {tokens.length === 0 ? (
        <p className="muted">Sin tokens generados.</p>
      ) : (
        <table>
          <thead>
            <tr>
              <th>Tipo</th>
              <th>Valor</th>
              <th>Línea</th>
              <th>Columna</th>
            </tr>
          </thead>
          <tbody>
            {tokens.map((token, index) => (
              <tr key={`${token.type}-${index}`}>
                <td style={{ color: token.type === 'UNKNOWN' ? '#ff7b72' : 'inherit' }}>{token.type}</td>
                <td style={{
                  fontFamily: 'JetBrains Mono, monospace',
                  color: token.type === 'UNKNOWN' ? '#ff7b72' : 'inherit'
                }}>{token.value}</td>
                <td>{token.line}</td>
                <td>{token.column}</td>
              </tr>
            ))}
          </tbody>
        </table>
      )}

      <h3 className="section-gap">Segmentos PLN</h3>
      {nlpSegments.length === 0 ? (
        <p className="muted">Sin segmentos PLN.</p>
      ) : (
        <div className="chip-row">
          {nlpSegments.map((segment, index) => (
            <span key={`${segment.text}-${index}`} className="segment-chip">
              <strong>{segment.label}</strong>: {segment.text}
            </span>
          ))}
        </div>
      )}
    </section>
  )
}

