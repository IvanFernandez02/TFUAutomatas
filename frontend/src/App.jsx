import { useEffect, useState } from 'react'
import { analyzeQuery, fetchHealth, fetchSchema } from './api/client'
import QueryEditor from './components/QueryEditor'
import TokenPanel from './components/TokenPanel'
import SyntaxTreePanel from './components/SyntaxTreePanel'
import SemanticPanel from './components/SemanticPanel'
import ResultPanel from './components/ResultPanel'
import SchemaViewer from './components/SchemaViewer'
import DataExplorer from './components/DataExplorer'

const DEFAULT_QUERY = 'SELECCIONAR nombre, edad DESDE usuarios CUANDO edad > 18'

export default function App() {
  const [query, setQuery] = useState(DEFAULT_QUERY)
  const [result, setResult] = useState(null)
  const [schema, setSchema] = useState(null)
  const [health, setHealth] = useState(null)
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState(null)
  const [activeTab, setActiveTab] = useState('tokens')
  const [refreshTrigger, setRefreshTrigger] = useState(0)

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

      // Si se ejecutó exitosamente una operación que modifica datos, refrescar el DataExplorer
      if (
        data.execution_result?.success &&
        ['INSERT', 'UPDATE', 'DELETE'].includes(data.execution_result.query_type)
      ) {
        setRefreshTrigger((prev) => prev + 1)
      }

      // Si la ejecución fue exitosa, cambiar a la pestaña de resultado
      if (data.execution_result) {
        setActiveTab('result')
      }
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
            Análisis léxico con regex + Ollama, sintáctico con parser formal + Ollama, semántico con tabla de símbolos, y ejecución en PostgreSQL.
          </p>
        </div>
        <div className="status-card">
          <span>Backend</span>
          <strong className="success">Activo</strong>
          <span>Ollama</span>
          <strong className={health?.ollama_available ? 'success' : 'warning'}>
            {health?.ollama_available ? 'Disponible' : 'No disponible'}
          </strong>
          <span>PostgreSQL</span>
          <strong className="success">Conectado</strong>
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
          <DataExplorer schema={schema} refreshTrigger={refreshTrigger} />
        </div>

        <div className="right-column">
          {error && <div className="alert error">{error}</div>}

          <div className="tabs">
            {['tokens', 'syntax', 'semantic', 'result'].map((tab) => (
              <button
                key={tab}
                className={activeTab === tab ? 'tab active' : 'tab'}
                onClick={() => setActiveTab(tab)}
              >
                {tab === 'tokens' && '🔤 Léxico'}
                {tab === 'syntax' && '🌳 Sintáctico'}
                {tab === 'semantic' && '🔍 Semántico'}
                {tab === 'result' && '🗄️ Resultado BD'}
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
              executionResult={result?.execution_result}
            />
          )}
          {activeTab === 'result' && (
            <ResultPanel executionResult={result?.execution_result} />
          )}
        </div>
      </main>
    </div>
  )
}
