import axiosClient from '../api/axiosClient';

// GET /prestamos (protegido).
export async function listarPrestamos() {
  const response = await axiosClient.get('/prestamos');
  return response.data;
}
