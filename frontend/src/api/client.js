import axios from 'axios'

const client = axios.create({
  baseURL: '/api',
  headers: { 'Content-Type': 'application/json' },
})

export async function analyzeQuery(query) {
  const { data } = await client.post('/analyze', { query })
  return data
}

export async function fetchSchema() {
  const { data } = await client.get('/schema')
  return data
}

export async function fetchHealth() {
  const { data } = await client.get('/health')
  return data
}

export async function fetchTableData(tableName) {
  const { data } = await client.get(`/tables/${tableName}/data`)
  return data
}
