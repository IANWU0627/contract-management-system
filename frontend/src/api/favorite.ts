import { get, post, del } from './index'

export interface Favorite {
  id: number
  contractNo: string
  title: string
  type: string
  status: string
  amount: number
  endDate: string
  favoritedAt: string
}

export interface FavoriteQuery {
  page?: number
  pageSize?: number
  userId?: number
}

export const getFavorites = (params?: FavoriteQuery) =>
  get('/favorites', { params })

export const addFavorite = (contractId: number, userId?: number) =>
  post(`/favorites/${contractId}`, {}, { params: userId ? { userId } : {} })

export const removeFavorite = (contractId: number, userId?: number) =>
  del(`/favorites/${contractId}`, { params: userId ? { userId } : {} })

export const checkFavorite = (contractId: number, userId?: number) =>
  get(`/favorites/check/${contractId}`, { params: userId ? { userId } : {} })
