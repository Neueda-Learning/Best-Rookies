// 此文件是投资组合相关接口封装，用于调用 portfolio 和 position 的后端接口。
import api from './client'

export function listPortfolios() {
  return api.get('/portfolios')
}

export function createPortfolio(payload) {
  return api.post('/portfolios', payload)
}

export function getPortfolio(id) {
  return api.get(`/portfolios/${id}`)
}

export function getPortfolioSummary(id) {
  return api.get(`/portfolios/${id}/summary`)
}

export function listPositions(portfolioId) {
  return api.get('/positions', { params: { portfolioId } })
}

export function createPosition(payload) {
  return api.post('/positions', payload)
}

export function deletePosition(id) {
  return api.delete(`/positions/${id}`)
}

export function getSupportedAssetTypes() {
  return api.get('/positions/asset-types/supported')
}

export function getSupportedTickersByAssetType(assetType) {
  return api.get('/positions/tickers/supported', { params: { assetType } })
}

export function searchSupportedTickers(assetType, q, limit = 20) {
  return api.get('/positions/tickers/search', { params: { assetType, q, limit } })
}
