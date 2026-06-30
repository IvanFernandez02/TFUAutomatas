import { JSONTree } from 'react-json-tree'

const theme = {
  scheme: 'monokai',
  author: 'wimer hazenberg',
  base00: '#1f2430',
  base01: '#383830',
  base02: '#49483e',
  base03: '#75715e',
  base04: '#a59f85',
  base05: '#f8f8f2',
  base06: '#f5f4f1',
  base07: '#f9f8f5',
  base08: '#f92672',
  base09: '#fd971f',
  base0A: '#f4bf75',
  base0B: '#a6e22e',
  base0C: '#a1efe4',
  base0D: '#66d9ef',
  base0E: '#ae81ff',
  base0F: '#cc6633',
}

export default function SyntaxTreePanel({ astPly, astLlm, astLlmAvailable, astLlmMessage }) {
  return (
    <section className="panel">
      <h3>Árbol sintáctico (Parser formal)</h3>
      {astPly ? (
        <div className="json-tree">
          <JSONTree data={astPly} theme={theme} invertTheme={false} />
        </div>
      ) : (
        <p className="muted">No se generó AST con PLY.</p>
      )}

      <h3 className="section-gap">Árbol asistido (LLM / Ollama)</h3>
      {!astLlmAvailable && <p className="warning">{astLlmMessage || 'Ollama no disponible'}</p>}
      {astLlm ? (
        <div className="json-tree">
          <JSONTree data={astLlm} theme={theme} invertTheme={false} />
        </div>
      ) : (
        <p className="muted">{astLlmMessage || 'Sin árbol LLM generado.'}</p>
      )}
    </section>
  )
}
