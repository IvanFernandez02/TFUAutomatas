export default function TokenPanel({ tokens, nlpSegments }) {
  return (
    <section className="panel">
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
                <td>{token.type}</td>
                <td>{token.value}</td>
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
