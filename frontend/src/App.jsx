import { useEffect, useState } from 'react'
import { analyzeQuery, fetchHealth, fetchSchema } from './api/client'
import QueryEditor from './components/QueryEditor'
import TokenPanel from './components/TokenPanel'
import SyntaxTreePanel from './components/SyntaxTreePanel'
import SemanticPanel from './components/SemanticPanel'
import SchemaViewer from './components/SchemaViewer'

const DEFAULT_QUERY = 'SELECCIONAR nombre, edad DESDE usuarios CUANDO edad > 18'

export default function App() {
  const [query, setQuery] = useState(DEFAULT_QUERY)
  const [result, setResult] = useState(null)
  const [schema, setSchema] = useState(null)
  const [health, setHealth] = useState(null)
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState(null)
  const [activeTab, setActiveTab] = useState('tokens')

  useEffect(() => {
    fetchSchema().then(setSchema).catch(() => setSchema(null))
    fetchHealth().then(setHealth).catch(() => setHealth(null))
  }, [])

  async function handleAnalyze() {
    setLoading(true)
    setError(null)
    try {
      const data = await analyzeQuery(query)
      setResult(data)
    } catch (err) {
      setError(err.response?.data?.detail || 'No se pudo conectar con el backend')
      setResult(null)
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="app">
      <header className="hero">
        <div>
          <p className="eyebrow">Compiladores · SQL en español</p>
          <h1>Mini-Compilador de Consultas SQL</h1>
          <p className="subtitle">
            Análisis léxico con regex + Ollama, sintáctico con parser formal + Ollama, y semántico con tabla de símbolos.
          </p>
        </div>
        <div className="status-card">
          <span>Backend</span>
          <strong className="success">Activo</strong>
          <span>Ollama</span>
          <strong className={health?.ollama_available ? 'success' : 'warning'}>
            {health?.ollama_available ? 'Disponible' : 'No disponible'}
          </strong>
        </div>
      </header>

      <main className="layout">
        <div className="left-column">
          <QueryEditor
            query={query}
            onChange={setQuery}
            onAnalyze={handleAnalyze}
            loading={loading}
          />
          <SchemaViewer schema={schema} />
        </div>

        <div className="right-column">
          {error && <div className="alert error">{error}</div>}

          <div className="tabs">
            {['tokens', 'syntax', 'semantic'].map((tab) => (
              <button
                key={tab}
                className={activeTab === tab ? 'tab active' : 'tab'}
                onClick={() => setActiveTab(tab)}
              >
                {tab === 'tokens' && 'Léxico'}
                {tab === 'syntax' && 'Sintáctico'}
                {tab === 'semantic' && 'Semántico'}
              </button>
            ))}
          </div>

          {activeTab === 'tokens' && (
            <TokenPanel tokens={result?.tokens || []} nlpSegments={result?.nlp_segments || []} />
          )}
          {activeTab === 'syntax' && (
            <SyntaxTreePanel
              astPly={result?.ast_ply}
              astLlm={result?.ast_llm}
              astLlmAvailable={result?.ast_llm_available}
              astLlmMessage={result?.ast_llm_message}
            />
          )}
          {activeTab === 'semantic' && (
            <SemanticPanel
              valid={result?.valid}
              semanticErrors={result?.semantic_errors || []}
              phaseErrors={result?.phase_errors || []}
              normalizedQuery={result?.normalized_query}
            />
          )}
        </div>
      </main>
    </div>
  )
}
