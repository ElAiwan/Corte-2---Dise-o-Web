import axiosClient from '../api/axiosClient';

// GET /equipos (protegido): el interceptor agrega el token automáticamente.
export async function listarEquipos() {
  const response = await axiosClient.get('/equipos');
  return response.data;
}
