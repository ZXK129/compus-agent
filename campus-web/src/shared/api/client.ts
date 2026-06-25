// API 客户端 — 前端唯一的数据入口，所有请求通过此模块
const BASE_URL = '/api';

async function request<T>(endpoint: string, options: RequestInit = {}): Promise<T> {
  const url = `${BASE_URL}${endpoint}`;
  const config: RequestInit = {
    headers: { 'Content-Type': 'application/json', ...options.headers },
    ...options,
  };
  const response = await fetch(url, config);
  if (!response.ok) throw new Error(`API Error ${response.status}`);
  const json: { code: number; message: string; data: T } = await response.json();
  if (json.code !== 200) throw new Error(json.message || '请求失败');
  return json.data;
}

export function get<T>(endpoint: string): Promise<T> { return request<T>(endpoint, { method: 'GET' }); }
export function post<T>(endpoint: string, body?: unknown): Promise<T> { return request<T>(endpoint, { method: 'POST', body: body ? JSON.stringify(body) : undefined }); }
export function del<T>(endpoint: string): Promise<T> { return request<T>(endpoint, { method: 'DELETE' }); }
